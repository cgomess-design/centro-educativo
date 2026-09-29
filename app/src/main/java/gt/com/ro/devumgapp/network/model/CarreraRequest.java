package gt.com.ro.devumgapp.network.model;

import com.google.gson.annotations.SerializedName;

/** Equivale a CarreraRequestDTO del backend. */
public class CarreraRequest {
    @SerializedName("codigo") private final String codigo;
    @SerializedName("nombre") private final String nombre;

    public CarreraRequest(String codigo, String nombre) {
        this.codigo = codigo;
        this.nombre = nombre;
    }
}
