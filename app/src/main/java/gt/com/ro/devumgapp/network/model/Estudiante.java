package gt.com.ro.devumgapp.network.model;

import com.google.gson.annotations.SerializedName;

/** Campos que el backend devuelve para un estudiante. */
public class Estudiante {
    @SerializedName("id") private long id;
    @SerializedName("codigoEstudiante") private String codigoEstudiante;
    @SerializedName("carnet") private String carnet;
    @SerializedName("email") private String email;
    @SerializedName("nombre") private String nombre;
    @SerializedName("apellido") private String apellido;
    @SerializedName("activo") private Boolean activo;

    public long getId() { return id; }
    public String getCodigoEstudiante() { return codigoEstudiante; }
    public String getCarnet() { return carnet; }
    public String getEmail() { return email; }
    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }
    public Boolean getActivo() { return activo; }
    public String getNombreCompleto() {
        return ((nombre == null ? "" : nombre) + " " + (apellido == null ? "" : apellido)).trim();
    }
}
