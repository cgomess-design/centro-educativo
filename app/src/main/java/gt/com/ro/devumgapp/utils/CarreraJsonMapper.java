package gt.com.ro.devumgapp.utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import gt.com.ro.devumgapp.network.model.Carrera;

/** Convierte las respuestas reales de carreras sin agregar datos locales. */
public final class CarreraJsonMapper {
    private static final Gson GSON = new Gson();

    private CarreraJsonMapper() { }

    public static List<Carrera> toList(JsonElement response) {
        JsonArray array = findArray(response);
        List<Carrera> carreras = new ArrayList<>();
        for (JsonElement item : array) {
            if (!item.isJsonObject()) {
                throw new IllegalArgumentException("Elemento de carrera inválido");
            }
            carreras.add(GSON.fromJson(item, Carrera.class));
        }
        return carreras;
    }

    public static Carrera toCarrera(JsonElement response) {
        JsonElement value = unwrapObject(response);
        if (value == null || !value.isJsonObject()) {
            throw new IllegalArgumentException("Respuesta de carrera inválida");
        }
        return GSON.fromJson(value, Carrera.class);
    }

    private static JsonArray findArray(JsonElement response) {
        if (response == null || response.isJsonNull()) {
            throw new IllegalArgumentException("Respuesta vacía");
        }
        if (response.isJsonArray()) return response.getAsJsonArray();
        if (response.isJsonObject()) {
            JsonObject object = response.getAsJsonObject();
            for (String key : new String[]{"content", "data", "carreras"}) {
                JsonElement candidate = object.get(key);
                if (candidate != null && candidate.isJsonArray()) {
                    return candidate.getAsJsonArray();
                }
            }
        }
        throw new IllegalArgumentException("Lista de carreras inválida");
    }

    private static JsonElement unwrapObject(JsonElement response) {
        if (response == null || response.isJsonNull()) return null;
        if (!response.isJsonObject()) return response;
        JsonObject object = response.getAsJsonObject();
        JsonElement data = object.get("data");
        return data != null && data.isJsonObject() ? data : response;
    }
}
