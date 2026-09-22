package gt.com.ro.devumgapp.network.model;

import com.google.gson.annotations.SerializedName;

/**
 * Campos que el backend devuelve para una inscripción.*/
public class Inscripcion {

    @SerializedName("id") private long id;

    @SerializedName("estudianteId") private Long estudianteId;

    @SerializedName("codigoEstudiante") private String codigoEstudiante;

    @SerializedName("nombreEstudiante") private String nombreEstudiante;

    @SerializedName("cursoId") private Long cursoId;

    @SerializedName("nombreCurso") private String nombreCurso;

    @SerializedName("fechaInscripcion") private String fechaInscripcion;

    @SerializedName("activo") private Boolean activo;

    @SerializedName("fechaCreacion") private String fechaCreacion;

    @SerializedName("fechaActualizacion") private String fechaActualizacion;

    public long getId() {return id;}

    public Long getEstudianteId() {return estudianteId;}

    public String getCodigoEstudiante() {return codigoEstudiante;}

    public String getNombreEstudiante() {return nombreEstudiante;}

    public Long getCursoId() {return cursoId;}

    public String getNombreCurso() {return nombreCurso;}

    public String getFechaInscripcion() {return fechaInscripcion;}

    public Boolean getActivo() {return activo;}

    public String getFechaCreacion() {return fechaCreacion;}

    public String getFechaActualizacion() {return fechaActualizacion;}
}


