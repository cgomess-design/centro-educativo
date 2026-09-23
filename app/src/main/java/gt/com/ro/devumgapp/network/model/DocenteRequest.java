package gt.com.ro.devumgapp.network.model;

import com.google.gson.annotations.SerializedName;

public class DocenteRequest {

    @SerializedName("nombres")
    private final String nombres;

    @SerializedName("apellidos")
    private final String apellidos;

    @SerializedName("correo")
    private final String correo;

    @SerializedName("telefono")
    private final String telefono;

    @SerializedName("activo")
    private final Boolean activo;

    public DocenteRequest(String nombres, String apellidos, String correo, String telefono, Boolean activo) {
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.correo = correo;
        this.telefono = telefono;
        this.activo = activo;
    }
}
