package gt.com.ro.devumgapp.network.model;

import com.google.gson.annotations.SerializedName;

public class DocenteRequest {

    @SerializedName("nombre")
    private final String nombre;

    @SerializedName("apellido")
    private final String apellido;

    @SerializedName("emailInstitucional")
    private final String emailInstitucional;

    @SerializedName("emailPersonal")
    private final String emailPersonal;

    @SerializedName("dpi")
    private final String dpi;

    @SerializedName("telefono")
    private final String telefono;

    @SerializedName("especialidad")
    private final String especialidad;

    @SerializedName("fechaContratacion")
    private final String fechaContratacion;

    @SerializedName("activo")
    private final Boolean activo;

    public DocenteRequest(String nombre,
                          String apellido,
                          String emailInstitucional,
                          String emailPersonal,
                          String dpi,
                          String telefono,
                          String especialidad,
                          String fechaContratacion,
                          Boolean activo) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.emailInstitucional = emailInstitucional;
        this.emailPersonal = emailPersonal;
        this.dpi = dpi;
        this.telefono = telefono;
        this.especialidad = especialidad;
        this.fechaContratacion = fechaContratacion;
        this.activo = activo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public String getEmailInstitucional() {
        return emailInstitucional;
    }

    public String getEmailPersonal() {
        return emailPersonal;
    }

    public String getDpi() {
        return dpi;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public String getFechaContratacion() {
        return fechaContratacion;
    }

    public Boolean getActivo() {
        return activo;
    }
}
