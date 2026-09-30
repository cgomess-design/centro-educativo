package gt.com.ro.devumgapp.network.model;

import com.google.gson.annotations.SerializedName;

/**
 * DTO para la creación y actualización de notas en el backend.
 * Serializa exactamente el formato JSON esperado por el servicio /api/notas.
 */
public class NotaRequest {

    @SerializedName("cicloAcademico")
    private final String cicloAcademico;

    @SerializedName("zona")
    private final Double zona;

    @SerializedName("examenFinal")
    private final Double examenFinal;

    @SerializedName("estado")
    private final String estado;

    @SerializedName("activo")
    private final Boolean activo;

    @SerializedName("fechaRegistro")
    private final String fechaRegistro;

    @SerializedName("inscripcionId")
    private final Long inscripcionId;

    public NotaRequest(String cicloAcademico, Double zona, Double examenFinal,
                       String estado, Boolean activo, String fechaRegistro, Long inscripcionId) {
        this.cicloAcademico = cicloAcademico;
        this.zona = zona;
        this.examenFinal = examenFinal;
        this.estado = estado;
        this.activo = activo;
        this.fechaRegistro = fechaRegistro;
        this.inscripcionId = inscripcionId;
    }

    public String getCicloAcademico() {
        return cicloAcademico;
    }

    public Double getZona() {
        return zona;
    }

    public Double getExamenFinal() {
        return examenFinal;
    }

    public String getEstado() {
        return estado;
    }

    public Boolean getActivo() {
        return activo;
    }

    public String getFechaRegistro() {
        return fechaRegistro;
    }

    public Long getInscripcionId() {
        return inscripcionId;
    }
}
