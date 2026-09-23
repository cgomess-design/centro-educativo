package gt.com.ro.devumgapp.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Utilitario para descargar el APK de actualización en segundo plano e
 * iniciar la instalación mediante el Package Installer de Android con FileProvider.
 */
public class ApkInstallerHelper {

    private static final String TAG = "ApkInstallerHelper";
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface DownloadProgressListener {
        void onProgress(int percent);
        void onComplete(File apkFile);
        void onError(Exception e);
    }

    /**
     * Descarga el archivo APK reportando el progreso en porcentaje.
     */
    public static void downloadApk(@NonNull Context context, @NonNull String apkUrl, @NonNull DownloadProgressListener listener) {
        executor.execute(() -> {
            try {
                OkHttpClient client = new OkHttpClient.Builder()
                        .followRedirects(true)
                        .followSslRedirects(true)
                        .build();

                Request request = new Request.Builder()
                        .url(apkUrl)
                        .build();

                Response response = client.newCall(request).execute();
                if (!response.isSuccessful() || response.body() == null) {
                    throw new Exception("Error en la descarga del servidor HTTP: " + response.code());
                }

                ResponseBody body = response.body();
                long totalBytes = body.contentLength();

                File downloadDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
                if (downloadDir != null && !downloadDir.exists()) {
                    downloadDir.mkdirs();
                }

                File apkFile = new File(downloadDir, "update.apk");
                if (apkFile.exists()) {
                    apkFile.delete();
                }

                try (InputStream inputStream = body.byteStream();
                     FileOutputStream outputStream = new FileOutputStream(apkFile)) {

                    byte[] buffer = new byte[8192];
                    long downloadedBytes = 0;
                    int read;
                    int lastReportedPercent = -1;

                    while ((read = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, read);
                        downloadedBytes += read;

                        if (totalBytes > 0) {
                            int percent = (int) ((downloadedBytes * 100) / totalBytes);
                            if (percent != lastReportedPercent) {
                                lastReportedPercent = percent;
                                mainHandler.post(() -> listener.onProgress(percent));
                            }
                        }
                    }
                    outputStream.flush();
                }

                mainHandler.post(() -> listener.onComplete(apkFile));

            } catch (Exception e) {
                Log.e(TAG, "Error durante la descarga del APK", e);
                mainHandler.post(() -> listener.onError(e));
            }
        });
    }

    /**
     * Valida permisos y ejecuta el instalador del paquete de Android.
     * @return true si se lanzó el instalador, false si se redirigió a ajustes de permisos o hubo error.
     */
    public static boolean installApk(@NonNull Context context, @NonNull File apkFile) {
        if (!apkFile.exists()) {
            Log.e(TAG, "El archivo APK a instalar no existe: " + apkFile.getAbsolutePath());
            return false;
        }

        // Si es Android 8.0 (API 26) o superior, validar si la app puede solicitar instalaciones
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!context.getPackageManager().canRequestPackageInstalls()) {
                Intent settingsIntent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
                settingsIntent.setData(Uri.parse("package:" + context.getPackageName()));
                settingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(settingsIntent);
                return false;
            }
        }

        // Obtener URI segura mediante FileProvider
        Uri apkUri = FileProvider.getUriForFile(
                context,
                context.getPackageName() + ".provider",
                apkFile
        );

        Intent installIntent = new Intent(Intent.ACTION_VIEW);
        installIntent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        installIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        installIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        context.startActivity(installIntent);
        return true;
    }
}
