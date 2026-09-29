package gt.com.ro.devumgapp.network.model;

import com.google.gson.annotations.SerializedName;

/** Campos que el backend devuelve para una carrera. */
public class Carrera {
    @SerializedName("id") private long id;
    @SerializedName("codigo") private String codigo;
    @SerializedName("nombre") private String nombre;
    @SerializedName("activo") private Boolean activo;

    public long getId() { return id; }
    public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public Boolean getActivo() { return activo; }
}
