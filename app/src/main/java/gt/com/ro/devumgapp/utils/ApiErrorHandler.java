package gt.com.ro.devumgapp.utils;

import android.app.Activity;
import android.content.Intent;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Map;

import gt.com.ro.devumgapp.LoginActivity;
import okhttp3.ResponseBody;
import retrofit2.Response;

/** Traduce errores de red y HTTP a mensajes seguros y legibles para la interfaz. */
public final class ApiErrorHandler {
    private ApiErrorHandler() { }

    public static String getMessage(Response<?> response) {
        if (response == null) return "Respuesta inválida del servidor.";

        String serverMessage = extractErrorMessage(response);
        if (serverMessage != null && !serverMessage.trim().isEmpty()) {
            return serverMessage;
        }

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

    /** Extrae el mensaje de detalle o validación devuelto por el backend en el errorBody. */
    private static String extractErrorMessage(Response<?> response) {
        ResponseBody errorBody = response.errorBody();
        if (errorBody == null) return null;
        try {
            String bodyString = errorBody.string();
            if (bodyString == null || bodyString.trim().isEmpty()) return null;

            JsonElement element = JsonParser.parseString(bodyString);
            if (element.isJsonObject()) {
                JsonObject obj = element.getAsJsonObject();

                // 1. Mensajes directos devueltos por el backend (message, mensaje, detail, error, etc.)
                for (String key : new String[]{"message", "mensaje", "detail", "detalle", "error", "descripcion"}) {
                    if (obj.has(key) && !obj.get(key).isJsonNull()) {
                        String msg = obj.get(key).getAsString();
                        if (msg != null && !msg.trim().isEmpty()) {
                            return msg.trim();
                        }
                    }
                }

                // 2. Errores de validación estructurados (Bean Validation)
                if (obj.has("errors") && !obj.get("errors").isJsonNull()) {
                    JsonElement errorsEl = obj.get("errors");
                    if (errorsEl.isJsonObject()) {
                        StringBuilder sb = new StringBuilder();
                        for (Map.Entry<String, JsonElement> entry : errorsEl.getAsJsonObject().entrySet()) {
                            if (sb.length() > 0) sb.append(". ");
                            sb.append(entry.getKey()).append(": ").append(entry.getValue().getAsString());
                        }
                        if (sb.length() > 0) return sb.toString();
                    } else if (errorsEl.isJsonArray()) {
                        StringBuilder sb = new StringBuilder();
                        for (JsonElement item : errorsEl.getAsJsonArray()) {
                            if (item.isJsonPrimitive()) {
                                if (sb.length() > 0) sb.append(". ");
                                sb.append(item.getAsString());
                            } else if (item.isJsonObject() && item.getAsJsonObject().has("defaultMessage")) {
                                if (sb.length() > 0) sb.append(". ");
                                sb.append(item.getAsJsonObject().get("defaultMessage").getAsString());
                            }
                        }
                        if (sb.length() > 0) return sb.toString();
                    }
                }
            } else if (element.isJsonPrimitive()) {
                return element.getAsString();
            }
        } catch (Exception ignored) {
        }
        return null;
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
