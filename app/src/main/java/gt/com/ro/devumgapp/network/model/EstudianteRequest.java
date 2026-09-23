package gt.com.ro.devumgapp.network.model;

import com.google.gson.annotations.SerializedName;

/** Equivale exactamente a EstudianteRequestDTO del backend. */
public class EstudianteRequest {
    @SerializedName("codigoEstudiante") private final String codigoEstudiante;
    @SerializedName("carnet") private final String carnet;
    @SerializedName("email") private final String email;
    @SerializedName("nombre") private final String nombre;
    @SerializedName("apellido") private final String apellido;

    public EstudianteRequest(String codigoEstudiante, String carnet, String email,
                             String nombre, String apellido) {
        this.codigoEstudiante = codigoEstudiante;
        this.carnet = carnet;
        this.email = email;
        this.nombre = nombre;
        this.apellido = apellido;
    }
}
