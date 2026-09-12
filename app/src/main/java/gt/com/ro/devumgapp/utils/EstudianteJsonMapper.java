package gt.com.ro.devumgapp.utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import gt.com.ro.devumgapp.network.model.Estudiante;

/** Aísla la variación de envoltorios de respuesta sin usar datos estáticos. */
public final class EstudianteJsonMapper {
    private static final Gson GSON = new Gson();

    private EstudianteJsonMapper() { }

    public static List<Estudiante> toList(JsonElement response) {
        JsonArray array = findArray(response);
        List<Estudiante> estudiantes = new ArrayList<>();
        for (JsonElement item : array) {
            if (!item.isJsonObject()) {
                throw new IllegalArgumentException("Elemento de estudiante inválido");
            }
            estudiantes.add(GSON.fromJson(item, Estudiante.class));
        }
        return estudiantes;
    }

    public static Estudiante toEstudiante(JsonElement response) {
        JsonElement value = unwrapObject(response);
        if (value == null || !value.isJsonObject()) {
            throw new IllegalArgumentException("Respuesta de estudiante inválida");
        }
        return GSON.fromJson(value, Estudiante.class);
    }

    private static JsonArray findArray(JsonElement response) {
        if (response == null || response.isJsonNull()) {
            throw new IllegalArgumentException("Respuesta vacía");
        }
        if (response.isJsonArray()) return response.getAsJsonArray();
        if (response.isJsonObject()) {
            JsonObject object = response.getAsJsonObject();
            for (String key : new String[]{"content", "data", "estudiantes"}) {
                JsonElement candidate = object.get(key);
                if (candidate != null && candidate.isJsonArray()) return candidate.getAsJsonArray();
            }
        }
        throw new IllegalArgumentException("Lista de estudiantes inválida");
    }

    private static JsonElement unwrapObject(JsonElement response) {
        if (response == null || response.isJsonNull()) return null;
        if (!response.isJsonObject()) return response;
        JsonObject object = response.getAsJsonObject();
        JsonElement data = object.get("data");
        return data != null && data.isJsonObject() ? data : response;
    }
}
