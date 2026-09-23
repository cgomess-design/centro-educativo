package gt.com.ro.devumgapp.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.pm.PackageInfoCompat;

import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Servicio encargado de verificar si existe una nueva versión de la aplicación
 * consultando la colección "versiones", documento "actual" en Cloud Firestore.
 */
public class AppUpdateChecker {

    private static final String TAG = "AppUpdateChecker";
    private static final String COLLECTION_VERSIONES = "versiones";
    private static final String DOCUMENT_ACTUAL = "actual";

    public interface OnUpdateCheckListener {
        void onUpdateAvailable(String newVersionName, long newVersionCode, String apkUrl);
        void onNoUpdateNeeded();
        void onError(Exception e);
    }

    public static void checkForUpdate(@NonNull Context context, @NonNull OnUpdateCheckListener listener) {
        long localVersionCode = getLocalVersionCode(context);
        String localVersionName = getLocalVersionName(context);

        Log.d(TAG, "Versión local actual: code=" + localVersionCode + ", name=" + localVersionName);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(COLLECTION_VERSIONES).document(DOCUMENT_ACTUAL)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot == null || !documentSnapshot.exists()) {
                        Log.d(TAG, "El documento versiones/actual no existe en Firestore.");
                        listener.onNoUpdateNeeded();
                        return;
                    }

                    Long remoteCodeObj = documentSnapshot.getLong("versionCode");
                    long remoteVersionCode = remoteCodeObj != null ? remoteCodeObj : 0;
                    String remoteVersionName = documentSnapshot.getString("versionName");
                    String apkUrl = documentSnapshot.getString("apkUrl");

                    Log.d(TAG, "Versión remota Firestore: code=" + remoteVersionCode +
                            ", name=" + remoteVersionName + ", apkUrl=" + apkUrl);

                    boolean isUpdateAvailable = false;
                    if (remoteVersionCode > localVersionCode) {
                        isUpdateAvailable = true;
                    } else if (remoteVersionCode == localVersionCode && remoteVersionName != null) {
                        if (compareVersions(remoteVersionName, localVersionName) > 0) {
                            isUpdateAvailable = true;
                        }
                    }

                    if (isUpdateAvailable && apkUrl != null && !apkUrl.trim().isEmpty()) {
                        String displayVersion = remoteVersionName != null ? remoteVersionName : String.valueOf(remoteVersionCode);
                        listener.onUpdateAvailable(displayVersion, remoteVersionCode, apkUrl.trim());
                    } else {
                        listener.onNoUpdateNeeded();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al consultar Firestore para actualización", e);
                    listener.onError(e);
                });
    }

    public static long getLocalVersionCode(Context context) {
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return PackageInfoCompat.getLongVersionCode(pInfo);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "No se pudo obtener local versionCode", e);
            return 0;
        }
    }

    public static String getLocalVersionName(Context context) {
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return pInfo.versionName != null ? pInfo.versionName : "0.0.0";
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "No se pudo obtener local versionName", e);
            return "0.0.0";
        }
    }

    /**
     * Compara dos cadenas de versión semántica (ej. "1.1.0" vs "1.0").
     * @return 1 si v1 > v2, -1 si v1 < v2, 0 si son equivalentes.
     */
    public static int compareVersions(String v1, String v2) {
        if (v1 == null) v1 = "0";
        if (v2 == null) v2 = "0";

        String[] parts1 = v1.replaceAll("[^0-9.]", "").split("\\.");
        String[] parts2 = v2.replaceAll("[^0-9.]", "").split("\\.");

        int length = Math.max(parts1.length, parts2.length);
        for (int i = 0; i < length; i++) {
            long num1 = 0;
            if (i < parts1.length && !parts1[i].trim().isEmpty()) {
                try {
                    num1 = Long.parseLong(parts1[i].trim());
                } catch (NumberFormatException ignored) {}
            }
            long num2 = 0;
            if (i < parts2.length && !parts2[i].trim().isEmpty()) {
                try {
                    num2 = Long.parseLong(parts2[i].trim());
                } catch (NumberFormatException ignored) {}
            }

            if (num1 < num2) return -1;
            if (num1 > num2) return 1;
        }
        return 0;
    }
}
