package gt.com.ro.devumgapp.network.model;

import com.google.gson.annotations.SerializedName;

public class CursoRequest {

        @SerializedName("codigo") private final String codigo;

        @SerializedName("nombre") private final String nombre;

        @SerializedName("creditos") private final Integer creditos;

        @SerializedName("docenteId") private final Long docenteId;

        @SerializedName("carreraId") private final Long carreraId;

        public CursoRequest(String codigo, String nombre, Integer creditos,
                            Long docenteId, Long carreraId) {
            this.codigo = codigo;
            this.nombre = nombre;
            this.creditos = creditos;
            this.docenteId = docenteId;
            this.carreraId = carreraId;
        }
}
