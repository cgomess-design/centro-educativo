package gt.com.ro.devumgapp.network.model;

import com.google.gson.annotations.SerializedName;

public class Nota {

    private Long id;
    private Double nota;
    private Long estudianteId;
    private String estudianteNombre;
    private Long cursoId;
    private String cursoNombre;
    @SerializedName("inscripcionId")
    private Long inscripcionId;
    @SerializedName("cicloAcademico")
    private String cicloAcademico;
    private Double zona;
    private Double examenFinal;
    private Double notaFinal;
    private String estado;
    private Boolean activo;
    private String fechaRegistro;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getNota() {
        return nota;
    }

    public void setNota(Double nota) {
        this.nota = nota;
    }

    public Long getEstudianteId() {
        return estudianteId;
    }

    public void setEstudianteId(Long estudianteId) {
        this.estudianteId = estudianteId;
    }

    public String getEstudianteNombre() {
        return estudianteNombre;
    }

    public void setEstudianteNombre(String estudianteNombre) {
        this.estudianteNombre = estudianteNombre;
    }

    public Long getCursoId() {
        return cursoId;
    }

    public void setCursoId(Long cursoId) {
        this.cursoId = cursoId;
    }

    public String getCursoNombre() {
        return cursoNombre;
    }

    public void setCursoNombre(String cursoNombre) {
        this.cursoNombre = cursoNombre;
    }

    public Long getInscripcionId() {
        return inscripcionId;
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

    public Double getNotaFinal() {
        return notaFinal;
    }

    public String getEstado() {
        return estado;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public String getFechaRegistro() {
        return fechaRegistro;
    }
}
