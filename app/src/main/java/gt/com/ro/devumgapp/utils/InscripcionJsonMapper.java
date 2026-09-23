package gt.com.ro.devumgapp.utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import gt.com.ro.devumgapp.network.model.Inscripcion;

/**
 * Convierte las respuestas JSON del backend en objetos Inscripcion.
 */
public final class InscripcionJsonMapper {

    private static final Gson GSON = new Gson();

    private InscripcionJsonMapper() {
    }

    /**
     * Convierte una respuesta JSON en una lista de inscripciones.
     */
    public static List<Inscripcion> toList(JsonElement response) {

        JsonArray array = findArray(response);

        List<Inscripcion> inscripciones = new ArrayList<>();

        for (JsonElement item : array) {

            if (!item.isJsonObject()) {
                throw new IllegalArgumentException(
                        "Elemento de inscripción inválido"
                );
            }

            inscripciones.add(
                    GSON.fromJson(item, Inscripcion.class)
            );
        }

        return inscripciones;
    }

    /**
     * Convierte una respuesta JSON en una inscripción.
     */
    public static Inscripcion toInscripcion(JsonElement response) {

        JsonElement value = unwrapObject(response);

        if (value == null || !value.isJsonObject()) {
            throw new IllegalArgumentException(
                    "Respuesta de inscripción inválida"
            );
        }

        return GSON.fromJson(
                value,
                Inscripcion.class
        );
    }

    /**
     * Busca el arreglo de inscripciones dentro de la respuesta.
     * Acepta:
     * [
     *   {...}
     * ]
     * o:
     * {
     *   "content": [...]
     * }
     * o:
     * {
     *   "data": [...]
     * }
     * o:
     * {
     *   "inscripciones": [...]
     * }
     */
    private static JsonArray findArray(JsonElement response) {

        if (response == null || response.isJsonNull()) {
            throw new IllegalArgumentException(
                    "Respuesta vacía"
            );
        }

        if (response.isJsonArray()) {
            return response.getAsJsonArray();
        }

        if (response.isJsonObject()) {

            JsonObject object = response.getAsJsonObject();

            for (String key : new String[]{
                    "content",
                    "data",
                    "inscripciones"
            }) {

                JsonElement candidate = object.get(key);

                if (candidate != null
                        && candidate.isJsonArray()) {

                    return candidate.getAsJsonArray();
                }
            }
        }

        throw new IllegalArgumentException(
                "Lista de inscripciones inválida"
        );
    }

    /**
     * Obtiene el objeto de inscripción de la respuesta.
     */
    private static JsonElement unwrapObject(
            JsonElement response
    ) {

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
