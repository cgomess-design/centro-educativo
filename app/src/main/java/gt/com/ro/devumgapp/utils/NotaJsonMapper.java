package gt.com.ro.devumgapp.utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import gt.com.ro.devumgapp.network.model.Nota;

public class NotaJsonMapper {

    private static final Gson GSON = new Gson();

    private NotaJsonMapper() {
    }

    public static List<Nota> toList(JsonElement response) {
        JsonArray array = findArray(response);

        List<Nota> notas = new ArrayList<>();

        for (JsonElement item : array) {
            if (!item.isJsonObject()) {
                throw new IllegalArgumentException("Elemento de nota inválido");
            }

            notas.add(GSON.fromJson(item, Nota.class));
        }

        return notas;
    }

    public static Nota toNota(JsonElement response) {
        JsonElement value = unwrapObject(response);

        if (value == null || !value.isJsonObject()) {
            throw new IllegalArgumentException("Respuesta de nota inválida");
        }

        return GSON.fromJson(value, Nota.class);
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

            for (String key : new String[]{"content", "data", "notas"}) {

                JsonElement candidate = object.get(key);

                if (candidate != null && candidate.isJsonArray()) {
                    return candidate.getAsJsonArray();
                }
            }
        }

        throw new IllegalArgumentException("Lista de notas inválida");
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
