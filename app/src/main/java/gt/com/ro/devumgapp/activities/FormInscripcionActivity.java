package gt.com.ro.devumgapp.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.JsonElement;

import gt.com.ro.devumgapp.R;
import gt.com.ro.devumgapp.network.RetrofitClient;
import gt.com.ro.devumgapp.network.model.Inscripcion;
import gt.com.ro.devumgapp.network.model.InscripcionRequest;
import gt.com.ro.devumgapp.utils.ApiErrorHandler;
import gt.com.ro.devumgapp.utils.InscripcionJsonMapper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Formulario utilizado para crear y actualizar inscripciones.
 */
public class FormInscripcionActivity extends AppCompatActivity {

    public static final String EXTRA_INSCRIPCION_ID =
            "inscripcion_id";

    private long inscripcionId = -1;

    private EditText estudianteId;
    private EditText cursoId;
    private EditText fechaInscripcion;

    private Button guardar;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_form_inscripcion
        );

        inscripcionId = getIntent().getLongExtra(
                EXTRA_INSCRIPCION_ID,
                -1
        );

        estudianteId = findViewById(
                R.id.edtEstudianteIdInscripcion
        );

        cursoId = findViewById(
                R.id.edtCursoIdInscripcion
        );

        fechaInscripcion = findViewById(
                R.id.edtFechaInscripcion
        );

        guardar = findViewById(
                R.id.btnGuardarInscripcion
        );

        progressBar = findViewById(
                R.id.progressFormInscripcion
        );

        guardar.setText(
                inscripcionId < 0
                        ? "CREAR INSCRIPCIÓN"
                        : "GUARDAR CAMBIOS"
        );

        guardar.setOnClickListener(
                v -> guardar()
        );

        if (inscripcionId >= 0) {
            cargarInscripcion();
        }
    }

    /**
     * Carga la inscripción cuando estamos editando.
     */
    private void cargarInscripcion() {

        setLoading(true);

        RetrofitClient.getInstance(this)
                .getApiService()
                .obtenerInscripcion(inscripcionId)
                .enqueue(new Callback<JsonElement>() {

                    @Override
                    public void onResponse(
                            Call<JsonElement> call,
                            Response<JsonElement> response
                    ) {

                        setLoading(false);

                        if (response.code() == 401) {

                            ApiErrorHandler.handleUnauthorized(
                                    FormInscripcionActivity.this
                            );

                            return;
                        }

                        if (!response.isSuccessful()
                                || response.body() == null) {

                            showMessage(
                                    ApiErrorHandler.getMessage(
                                            response
                                    )
                            );

                            return;
                        }

                        try {

                            Inscripcion inscripcion =
                                    InscripcionJsonMapper.toInscripcion(
                                            response.body()
                                    );

                            populate(inscripcion);

                        } catch (IllegalArgumentException error) {

                            showMessage(
                                    "El servidor devolvió una inscripción inválida."
                            );
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<JsonElement> call,
                            Throwable error
                    ) {

                        setLoading(false);

                        showMessage(
                                ApiErrorHandler.getNetworkMessage(
                                        error
                                )
                        );
                    }
                });
    }

    /**
     * Coloca los datos de la inscripción en el formulario.
     */
    private void populate(
            Inscripcion inscripcion
    ) {

        if (inscripcion.getEstudianteId() != null) {

            estudianteId.setText(
                    String.valueOf(
                            inscripcion.getEstudianteId()
                    )
            );
        }

        if (inscripcion.getCursoId() != null) {

            cursoId.setText(
                    String.valueOf(
                            inscripcion.getCursoId()
                    )
            );
        }

        if (inscripcion.getFechaInscripcion() != null) {

            fechaInscripcion.setText(
                    inscripcion.getFechaInscripcion()
            );
        }
    }

    /**
     * Crea o actualiza una inscripción.
     */
    private void guardar() {

        InscripcionRequest request =
                createRequest();

        if (request == null) {
            return;
        }

        setLoading(true);

        Call<JsonElement> call;

        if (inscripcionId < 0) {

            call = RetrofitClient.getInstance(this)
                    .getApiService()
                    .crearInscripcion(request);

        } else {

            call = RetrofitClient.getInstance(this)
                    .getApiService()
                    .actualizarInscripcion(
                            inscripcionId,
                            request
                    );
        }

        call.enqueue(new Callback<JsonElement>() {

            @Override
            public void onResponse(
                    Call<JsonElement> call,
                    Response<JsonElement> response
            ) {

                setLoading(false);

                if (response.code() == 401) {

                    ApiErrorHandler.handleUnauthorized(
                            FormInscripcionActivity.this
                    );

                } else if (response.isSuccessful()) {

                    showMessage(
                            inscripcionId < 0
                                    ? "Inscripción creada correctamente."
                                    : "Inscripción actualizada correctamente."
                    );

                    finish();

                } else {

                    showMessage(
                            ApiErrorHandler.getMessage(
                                    response
                            )
                    );
                }
            }

            @Override
            public void onFailure(
                    Call<JsonElement> call,
                    Throwable error
            ) {

                setLoading(false);

                showMessage(
                        ApiErrorHandler.getNetworkMessage(
                                error
                        )
                );
            }
        });
    }

    /**
     * Valida los datos antes de enviarlos al backend.
     */
    private InscripcionRequest createRequest() {

        String estudianteIdValue =
                estudianteId.getText()
                        .toString()
                        .trim();

        String cursoIdValue =
                cursoId.getText()
                        .toString()
                        .trim();

        String fechaValue =
                fechaInscripcion.getText()
                        .toString()
                        .trim();

        if (estudianteIdValue.isEmpty()
                || cursoIdValue.isEmpty()) {

            showMessage(
                    "Ingrese el ID del estudiante y del curso."
            );

            return null;
        }

        long estudianteIdNumber;
        long cursoIdNumber;

        try {

            estudianteIdNumber =
                    Long.parseLong(
                            estudianteIdValue
                    );

            cursoIdNumber =
                    Long.parseLong(
                            cursoIdValue
                    );

        } catch (NumberFormatException error) {

            showMessage(
                    "Los IDs deben ser números válidos."
            );

            return null;
        }

        if (estudianteIdNumber <= 0
                || cursoIdNumber <= 0) {

            showMessage(
                    "Los IDs deben ser mayores que 0."
            );

            return null;
        }

        if (!fechaValue.isEmpty()
                && !fechaValue.matches(
                "\\d{4}-\\d{2}-\\d{2}"
        )) {

            showMessage(
                    "La fecha debe tener el formato AAAA-MM-DD."
            );

            return null;
        }

        return new InscripcionRequest(
                estudianteIdNumber,
                cursoIdNumber,
                fechaValue.isEmpty()
                        ? null
                        : fechaValue
        );
    }

    /**
     * Controla el indicador de carga.
     */
    private void setLoading(
            boolean loading
    ) {

        progressBar.setVisibility(
                loading
                        ? View.VISIBLE
                        : View.GONE
        );

        guardar.setEnabled(!loading);
    }

    /**
     * Muestra mensajes al usuario.
     */
    private void showMessage(
            String message
    ) {

        Toast.makeText(
                this,
                message,
                Toast.LENGTH_LONG
        ).show();
    }
}
