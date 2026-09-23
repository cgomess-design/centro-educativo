package gt.com.ro.devumgapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.JsonElement;

import gt.com.ro.devumgapp.R;
import gt.com.ro.devumgapp.network.RetrofitClient;
import gt.com.ro.devumgapp.network.model.Inscripcion;
import gt.com.ro.devumgapp.utils.ApiErrorHandler;
import gt.com.ro.devumgapp.utils.InscripcionJsonMapper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Muestra el detalle de una inscripción.
 */
public class DetalleInscripcionActivity extends AppCompatActivity {

    public static final String EXTRA_INSCRIPCION_ID =
            "inscripcion_id";

    private long inscripcionId;

    private ProgressBar progressBar;

    private TextView estudiante;
    private TextView codigoEstudiante;
    private TextView curso;
    private TextView fechaInscripcion;
    private TextView estado;
    private TextView fechaCreacion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_detalle_inscripcion
        );

        inscripcionId = getIntent().getLongExtra(
                EXTRA_INSCRIPCION_ID,
                -1
        );

        if (inscripcionId < 0) {

            Toast.makeText(
                    this,
                    "Inscripción no válida.",
                    Toast.LENGTH_LONG
            ).show();

            finish();
            return;
        }

        progressBar = findViewById(
                R.id.progressDetalleInscripcion
        );

        estudiante = findViewById(
                R.id.txtDetalleEstudianteInscripcion
        );

        codigoEstudiante = findViewById(
                R.id.txtDetalleCodigoEstudiante
        );

        curso = findViewById(
                R.id.txtDetalleCursoInscripcion
        );

        fechaInscripcion = findViewById(
                R.id.txtDetalleFechaInscripcion
        );

        estado = findViewById(
                R.id.txtDetalleEstadoInscripcion
        );

        fechaCreacion = findViewById(
                R.id.txtDetalleFechaCreacionInscripcion
        );

        Button editar = findViewById(
                R.id.btnEditarInscripcion
        );

        Button eliminar = findViewById(
                R.id.btnEliminarInscripcion
        );

        editar.setOnClickListener(v -> {

            Intent intent = new Intent(
                    this,
                    FormInscripcionActivity.class
            );

            intent.putExtra(
                    FormInscripcionActivity.EXTRA_INSCRIPCION_ID,
                    inscripcionId
            );

            startActivity(intent);
        });

        eliminar.setOnClickListener(
                v -> confirmarEliminacion()
        );
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (inscripcionId >= 0) {
            cargarInscripcion();
        }
    }

    /**
     * Obtiene una inscripción específica desde el backend.
     */
    private void cargarInscripcion() {

        setLoading(true);

        RetrofitClient.getInstance(this)
                .getApiService()
                .obtenerInscripcion(inscripcionId)
                .enqueue(new Callback<JsonElement>() {

                    @Override
                    public void onResponse(
                            @NonNull Call<JsonElement> call,
                            @NonNull Response<JsonElement> response
                    ) {

                        setLoading(false);

                        if (response.code() == 401) {

                            ApiErrorHandler.handleUnauthorized(
                                    DetalleInscripcionActivity.this
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

                            bind(inscripcion);

                        } catch (IllegalArgumentException error) {

                            showMessage(
                                    "El servidor devolvió una inscripción inválida."
                            );
                        }
                    }

                    @Override
                    public void onFailure(
                            @NonNull Call<JsonElement> call,
                            @NonNull Throwable error
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
     * Coloca los datos recibidos en la pantalla.
     */
    private void bind(
            Inscripcion inscripcion
    ) {

        estudiante.setText(
                inscripcion.getNombreEstudiante() != null
                        ? inscripcion.getNombreEstudiante()
                        : "No disponible"
        );

        codigoEstudiante.setText(
                inscripcion.getCodigoEstudiante() != null
                        ? inscripcion.getCodigoEstudiante()
                        : "No disponible"
        );

        curso.setText(
                inscripcion.getNombreCurso() != null
                        ? inscripcion.getNombreCurso()
                        : "No disponible"
        );

        fechaInscripcion.setText(
                inscripcion.getFechaInscripcion() != null
                        ? inscripcion.getFechaInscripcion()
                        : "No disponible"
        );

        estado.setText(
                Boolean.TRUE.equals(
                        inscripcion.getActivo()
                )
                        ? "Activo"
                        : "Inactivo"
        );

        fechaCreacion.setText(
                inscripcion.getFechaCreacion() != null
                        ? inscripcion.getFechaCreacion()
                        : "No disponible"
        );
    }

    /**
     * Confirma antes de eliminar/desactivar.
     */
    private void confirmarEliminacion() {

        new AlertDialog.Builder(this)
                .setTitle("Eliminar inscripción")
                .setMessage(
                        "¿Desea eliminar o desactivar esta inscripción?"
                )
                .setNegativeButton(
                        "Cancelar",
                        null
                )
                .setPositiveButton(
                        "Confirmar",
                        (dialog, which) -> eliminar()
                )
                .show();
    }

    /**
     * Elimina/desactiva la inscripción mediante el backend.
     */
    private void eliminar() {

        setLoading(true);

        RetrofitClient.getInstance(this)
                .getApiService()
                .eliminarInscripcion(inscripcionId)
                .enqueue(new Callback<JsonElement>() {

                    @Override
                    public void onResponse(
                            @NonNull Call<JsonElement> call,
                            @NonNull Response<JsonElement> response
                    ) {

                        setLoading(false);

                        if (response.code() == 401) {

                            ApiErrorHandler.handleUnauthorized(
                                    DetalleInscripcionActivity.this
                            );

                        } else if (response.isSuccessful()) {

                            showMessage(
                                    "Inscripción eliminada correctamente."
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
                            @NonNull Call<JsonElement> call,
                            @NonNull Throwable error
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

    private void setLoading(
            boolean loading
    ) {

        progressBar.setVisibility(
                loading
                        ? View.VISIBLE
                        : View.GONE
        );
    }

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
