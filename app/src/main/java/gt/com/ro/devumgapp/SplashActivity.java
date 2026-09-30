package gt.com.ro.devumgapp;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;

import gt.com.ro.devumgapp.utils.ApkInstallerHelper;
import gt.com.ro.devumgapp.utils.AppUpdateChecker;
import gt.com.ro.devumgapp.utils.TokenManager;

/**
 * Pantalla de bienvenida (Splash).
 * Valida si existe una versión superior en Firebase Firestore.
 * Si existe, ofrece descargar e instalar el APK automáticamente con barra de progreso.
 * Si no, enruta hacia MainActivity o LoginActivity tras la espera mínima.
 */
public class SplashActivity extends AppCompatActivity {

    private static final long MIN_SPLASH_DURATION_MS = 1500L;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private boolean minSplashElapsed = false;
    private boolean updateCheckFinished = false;
    private boolean updateFound = false;
    private boolean isNavigating = false;
    private boolean installIntentLaunched = false;

    private AlertDialog updateDialog;
    private AlertDialog progressDialog;
    private File pendingApkToInstall = null;
    private AppUpdateChecker.UpdateType currentUpdateType = AppUpdateChecker.UpdateType.NONE;
    private String currentApkUrl = null;
    private String currentNewVersionName = null;

    private final Runnable minSplashRunnable = () -> {
        minSplashElapsed = true;
        if (updateCheckFinished && !updateFound) {
            checkSessionAndNavigate();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        handler.postDelayed(minSplashRunnable, MIN_SPLASH_DURATION_MS);
        checkForAppUpdates();
    }

    private void checkForAppUpdates() {
        AppUpdateChecker.checkForUpdate(this, new AppUpdateChecker.OnUpdateCheckListener() {
            @Override
            public void onUpdateAvailable(AppUpdateChecker.UpdateType type, String newVersionName, long newVersionCode, String apkUrl) {
                if (isFinishing() || isDestroyed()) return;

                updateCheckFinished = true;
                updateFound = true;
                currentUpdateType = type;
                currentApkUrl = apkUrl;
                currentNewVersionName = newVersionName;
                handler.removeCallbacks(minSplashRunnable);

                showUpdateDialog(type, newVersionName, apkUrl);
            }

            @Override
            public void onNoUpdateNeeded() {
                if (isFinishing() || isDestroyed()) return;

                updateCheckFinished = true;
                if (minSplashElapsed) {
                    checkSessionAndNavigate();
                }
            }

            @Override
            public void onError(Exception e) {
                if (isFinishing() || isDestroyed()) return;

                updateCheckFinished = true;
                if (minSplashElapsed) {
                    checkSessionAndNavigate();
                }
            }
        });
    }

    private void showUpdateDialog(AppUpdateChecker.UpdateType type, String newVersionName, String apkUrl) {
        if (isFinishing() || isDestroyed()) return;

        if (updateDialog != null && updateDialog.isShowing()) {
            updateDialog.dismiss();
        }

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this)
                .setCancelable(false);

        if (type == AppUpdateChecker.UpdateType.MANDATORY) {
            // Escenario 10.2: Actualización Obligatoria
            builder.setTitle(R.string.update_mandatory_title)
                    .setMessage(R.string.update_mandatory_message)
                    .setPositiveButton(R.string.update_btn_confirm, (dialog, which) -> {
                        startDownloadAndInstall(apkUrl);
                    })
                    .setNegativeButton(R.string.update_btn_exit, (dialog, which) -> {
                        finishAffinity();
                    });
        } else {
            // Escenario 10.1: Actualización Recomendada
            builder.setTitle(R.string.update_recommended_title)
                    .setMessage(R.string.update_recommended_message)
                    .setPositiveButton(R.string.update_btn_confirm, (dialog, which) -> {
                        startDownloadAndInstall(apkUrl);
                    })
                    .setNegativeButton(R.string.update_btn_continue, (dialog, which) -> {
                        checkSessionAndNavigate();
                    });
        }

        updateDialog = builder.create();
        updateDialog.show();
    }

    private void startDownloadAndInstall(String apkUrl) {
        if (isFinishing() || isDestroyed()) return;

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_download_progress, null);
        ProgressBar progressBar = dialogView.findViewById(R.id.progressDownload);
        TextView txtPercent = dialogView.findViewById(R.id.txtDownloadPercent);

        progressDialog = new MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        progressDialog.show();

        ApkInstallerHelper.downloadApk(this, apkUrl, new ApkInstallerHelper.DownloadProgressListener() {
            @Override
            public void onProgress(int percent) {
                if (isFinishing() || isDestroyed()) return;
                progressBar.setProgress(percent);
                txtPercent.setText(getString(R.string.update_downloading_message, percent));
            }

            @Override
            public void onComplete(File apkFile) {
                if (isFinishing() || isDestroyed()) return;

                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }

                pendingApkToInstall = apkFile;
                boolean launched = ApkInstallerHelper.installApk(SplashActivity.this, apkFile);
                if (launched) {
                    installIntentLaunched = true;
                }
            }

            @Override
            public void onError(Exception e) {
                if (isFinishing() || isDestroyed()) return;

                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }

                if (currentUpdateType == AppUpdateChecker.UpdateType.MANDATORY) {
                    Toast.makeText(SplashActivity.this, R.string.update_download_mandatory_error, Toast.LENGTH_LONG).show();
                    // Al ser obligatoria, no se puede continuar; se vuelve a mostrar el diálogo
                    showUpdateDialog(currentUpdateType, currentNewVersionName, currentApkUrl);
                } else {
                    Toast.makeText(SplashActivity.this, R.string.update_download_error, Toast.LENGTH_LONG).show();
                    checkSessionAndNavigate();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Si el usuario regresa a la app después de lanzar el instalador (por ejemplo, canceló la instalación)
        if (installIntentLaunched) {
            installIntentLaunched = false;
            if (currentUpdateType == AppUpdateChecker.UpdateType.MANDATORY) {
                // En actualización obligatoria, si no se completó la instalación, no debe continuar a la app
                showUpdateDialog(currentUpdateType, currentNewVersionName, currentApkUrl);
            } else {
                checkSessionAndNavigate();
            }
            return;
        }

        // Si regresó de la pantalla de Ajustes ("Permitir instalar aplicaciones desconocidas")
        if (pendingApkToInstall != null && pendingApkToInstall.exists()) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O || getPackageManager().canRequestPackageInstalls()) {
                File apkToInstall = pendingApkToInstall;
                pendingApkToInstall = null;
                boolean launched = ApkInstallerHelper.installApk(this, apkToInstall);
                if (launched) {
                    installIntentLaunched = true;
                }
            }
        }
    }

    private synchronized void checkSessionAndNavigate() {
        if (isNavigating || isFinishing() || isDestroyed()) {
            return;
        }
        isNavigating = true;

        boolean hasSession = TokenManager.getInstance(this).hasToken();
        Intent intent;
        if (hasSession) {
            intent = new Intent(this, MainActivity.class);
        } else {
            intent = new Intent(this, LoginActivity.class);
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(minSplashRunnable);
        if (updateDialog != null && updateDialog.isShowing()) {
            updateDialog.dismiss();
        }
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}
