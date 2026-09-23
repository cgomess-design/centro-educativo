package gt.com.ro.devumgapp.utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import gt.com.ro.devumgapp.network.model.Curso;

public class CursoJsonMapper {

        private static final Gson GSON = new Gson();

        private CursoJsonMapper() {
        }

        public static List<Curso> toList(JsonElement response) {
            JsonArray array = findArray(response);

            List<Curso> cursos = new ArrayList<>();

            for (JsonElement item : array) {
                if (!item.isJsonObject()) {
                    throw new IllegalArgumentException("Elemento de curso inválido");
                }

                cursos.add(GSON.fromJson(item, Curso.class));
            }

            return cursos;
        }

        public static Curso toCurso(JsonElement response) {
            JsonElement value = unwrapObject(response);

            if (value == null || !value.isJsonObject()) {
                throw new IllegalArgumentException("Respuesta de curso inválida");
            }

            return GSON.fromJson(value, Curso.class);
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

                for (String key : new String[]{"content", "data", "cursos"}) {

                    JsonElement candidate = object.get(key);

                    if (candidate != null && candidate.isJsonArray()) {
                        return candidate.getAsJsonArray();
                    }
                }
            }

            throw new IllegalArgumentException("Lista de cursos inválida");
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
