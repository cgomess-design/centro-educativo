package gt.com.ro.devumgapp.network.model;

import com.google.gson.annotations.SerializedName;

public class Curso {
        @SerializedName("id") private long id;

        @SerializedName("codigo") private String codigo;

        @SerializedName("nombre") private String nombre;

        @SerializedName("creditos") private Integer creditos;

        @SerializedName("carreraId") private Long carreraId;

        @SerializedName("carreraCodigo") private String carreraCodigo;

        @SerializedName("carreraNombre") private String carreraNombre;

        @SerializedName("docenteId") private Long docenteId;

        @SerializedName("docenteNombre") private String docenteNombre;

        @SerializedName("activo") private Boolean activo;


        public long getId() {
            return id;
        }

        public String getCodigo() {
            return codigo;
        }

        public String getNombre() {
            return nombre;
        }

        public Integer getCreditos() {
            return creditos;
        }

        public Long getCarreraId() {
            return carreraId;
        }

        public String getCarreraCodigo() {
            return carreraCodigo;
        }

        public String getCarreraNombre() {
            return carreraNombre;
        }

        public Long getDocenteId() {
            return docenteId;
        }

        public String getDocenteNombre() {
            return docenteNombre;
        }

        public Boolean getActivo() {
            return activo;
        }
}
