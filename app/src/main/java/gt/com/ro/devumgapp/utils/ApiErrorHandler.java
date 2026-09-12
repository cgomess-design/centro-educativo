package gt.com.ro.devumgapp.utils;

import android.app.Activity;
import android.content.Intent;

import java.io.IOException;
import java.net.SocketTimeoutException;

import gt.com.ro.devumgapp.LoginActivity;
import retrofit2.Response;

/** Traduce errores de red y HTTP a mensajes seguros para la interfaz. */
public final class ApiErrorHandler {
    private ApiErrorHandler() { }

    public static String getMessage(Response<?> response) {
        if (response == null) return "Respuesta inválida del servidor.";
        switch (response.code()) {
            case 400: return "La información enviada no es válida.";
            case 401: return "La sesión expiró. Inicie sesión nuevamente.";
            case 403: return "No tiene permisos para realizar esta operación.";
            case 404: return "El recurso solicitado no fue encontrado.";
            case 409: return "Existe un conflicto con la información enviada.";
            default:
                return response.code() >= 500
                        ? "El servidor presentó un error. Intente más tarde."
                        : "No se pudo completar la operación.";
        }
    }

    public static String getNetworkMessage(Throwable error) {
        if (error instanceof SocketTimeoutException) {
            return "La solicitud tardó demasiado. Verifique su conexión e intente de nuevo.";
        }
        if (error instanceof IOException) {
            return "No fue posible conectar con el servidor. Verifique su conexión.";
        }
        return "Se recibió una respuesta inválida del servidor.";
    }

    public static void handleUnauthorized(Activity activity) {
        TokenManager.getInstance(activity).clearToken();
        Intent intent = new Intent(activity, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
        activity.finish();
    }
}
