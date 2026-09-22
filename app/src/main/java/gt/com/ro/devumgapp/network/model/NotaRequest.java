package gt.com.ro.devumgapp.network.model;

import com.google.gson.annotations.SerializedName;

public class NotaRequest {

    @SerializedName("nota")
    private final Double nota;

    @SerializedName("estudianteId")
    private final Long estudianteId;

    @SerializedName("cursoId")
    private final Long cursoId;

    @SerializedName("activo")
    private final Boolean activo;

    public NotaRequest(Double nota, Long estudianteId, Long cursoId, Boolean activo) {
        this.nota = nota;
        this.estudianteId = estudianteId;
        this.cursoId = cursoId;
        this.activo = activo;
    }
}
