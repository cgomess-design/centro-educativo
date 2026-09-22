package gt.com.ro.devumgapp.utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import gt.com.ro.devumgapp.network.model.Docente;

public class DocenteJsonMapper {

    private static final Gson GSON = new Gson();

    private DocenteJsonMapper() {
    }

    public static List<Docente> toList(JsonElement response) {
        JsonArray array = findArray(response);

        List<Docente> docentes = new ArrayList<>();

        for (JsonElement item : array) {
            if (!item.isJsonObject()) {
                throw new IllegalArgumentException("Elemento de docente inválido");
            }

            docentes.add(GSON.fromJson(item, Docente.class));
        }

        return docentes;
    }

    public static Docente toDocente(JsonElement response) {
        JsonElement value = unwrapObject(response);

        if (value == null || !value.isJsonObject()) {
            throw new IllegalArgumentException("Respuesta de docente inválida");
        }

        return GSON.fromJson(value, Docente.class);
    }

    private static JsonArray findArray(JsonElement response) {

        if (response == null || response.isJsonNull()) {
            throw new IllegalArgumentException("Respuesta vacía");
        }

        if (response.isJsonArray()) {
            return response.getAsJsonArray();
        }

        if (response.isJsonObject()) {

            JsonObject object = response.getAsJsonObject();

            for (String key : new String[]{"content", "data", "docentes"}) {

                JsonElement candidate = object.get(key);

                if (candidate != null && candidate.isJsonArray()) {
                    return candidate.getAsJsonArray();
                }
            }
        }

        throw new IllegalArgumentException("Lista de docentes inválida");
    }

    private static JsonElement unwrapObject(JsonElement response) {

        if (response == null || response.isJsonNull()) {
            return null;
        }

        if (!response.isJsonObject()) {
            return response;
        }

        JsonObject object = response.getAsJsonObject();

        JsonElement data = object.get("data");

        return data != null && data.isJsonObject()
                ? data
                : response;
    }
}
