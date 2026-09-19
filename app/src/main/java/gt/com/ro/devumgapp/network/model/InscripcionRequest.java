package gt.com.ro.devumgapp.network.model;

import com.google.gson.annotations.SerializedName;

/**
 * Datos que Android envía al backend para crear o actualizar
 * una inscripción.
 */
public class InscripcionRequest {

    @SerializedName("estudianteId")
    private final Long estudianteId;

    @SerializedName("cursoId")
    private final Long cursoId;

    @SerializedName("fechaInscripcion")
    private final String fechaInscripcion;

    public InscripcionRequest(
            Long estudianteId,
            Long cursoId,
            String fechaInscripcion
    ) {
        this.estudianteId = estudianteId;
        this.cursoId = cursoId;
        this.fechaInscripcion = fechaInscripcion;
    }
}

