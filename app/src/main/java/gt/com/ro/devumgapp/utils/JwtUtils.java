package gt.com.ro.devumgapp.utils;

import android.util.Base64;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.nio.charset.StandardCharsets;

/** Utilidad para decodificar y extraer claims como el rol o el usuario del token JWT. */
public final class JwtUtils {

    private JwtUtils() {}

    public static String extractRole(String jwt) {
        JsonObject payload = getPayload(jwt);
        if (payload == null) return null;

        if (payload.has("rol") && !payload.get("rol").isJsonNull()) {
            return payload.get("rol").getAsString();
        }
        if (payload.has("role") && !payload.get("role").isJsonNull()) {
            return payload.get("role").getAsString();
        }

        if (payload.has("roles") && !payload.get("roles").isJsonNull()) {
            JsonElement elem = payload.get("roles");
            if (elem.isJsonPrimitive()) return elem.getAsString();
            if (elem.isJsonArray()) {
                JsonArray arr = elem.getAsJsonArray();
                if (arr.size() > 0) return arr.get(0).getAsString();
            }
        }

        if (payload.has("authorities") && !payload.get("authorities").isJsonNull()) {
            JsonElement elem = payload.get("authorities");
            if (elem.isJsonPrimitive()) return elem.getAsString();
            if (elem.isJsonArray()) {
                JsonArray arr = elem.getAsJsonArray();
                if (arr.size() > 0) {
                    JsonElement first = arr.get(0);
                    if (first.isJsonPrimitive()) return first.getAsString();
                    if (first.isJsonObject() && first.getAsJsonObject().has("authority")) {
                        return first.getAsJsonObject().get("authority").getAsString();
                    }
                }
            }
        }

        return null;
    }

    public static String extractUsername(String jwt) {
        JsonObject payload = getPayload(jwt);
        if (payload == null) return null;

        if (payload.has("sub") && !payload.get("sub").isJsonNull()) {
            return payload.get("sub").getAsString();
        }
        if (payload.has("username") && !payload.get("username").isJsonNull()) {
            return payload.get("username").getAsString();
        }
        return null;
    }

    private static JsonObject getPayload(String jwt) {
        if (jwt == null || jwt.trim().isEmpty()) return null;
        String[] parts = jwt.split("\\.");
        if (parts.length < 2) return null;
        try {
            byte[] decoded = Base64.decode(parts[1], Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING);
            String jsonStr = new String(decoded, StandardCharsets.UTF_8);
            return JsonParser.parseString(jsonStr).getAsJsonObject();
        } catch (Exception ignored) {
            return null;
        }
    }
}
