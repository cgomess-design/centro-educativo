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
import gt.com.ro.devumgapp.network.model.Curso;
import gt.com.ro.devumgapp.utils.ApiErrorHandler;
import gt.com.ro.devumgapp.utils.CursoJsonMapper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetalleCursoActivity extends AppCompatActivity {

    public static final String EXTRA_CURSO_ID = "curso_id";

    private long cursoId;

    private ProgressBar progressBar;

    private TextView codigo;
    private TextView nombre;
    private TextView creditos;
    private TextView carrera;
    private TextView docente;
    private TextView estado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_detalle_curso);

        cursoId = getIntent().getLongExtra(
                EXTRA_CURSO_ID,
                -1
        );

        if (cursoId < 0) {

            Toast.makeText(
                    this,
                    "Curso no válido.",
                    Toast.LENGTH_LONG
            ).show();

            finish();
            return;
        }

        progressBar = findViewById(
                R.id.progressDetalleCurso
        );

        codigo = findViewById(
                R.id.txtDetalleCodigoCurso
        );

        nombre = findViewById(
                R.id.txtDetalleNombreCurso
        );

        creditos = findViewById(
                R.id.txtDetalleCreditosCurso
        );

        carrera = findViewById(
                R.id.txtDetalleCarreraCurso
        );

        docente = findViewById(
                R.id.txtDetalleDocenteCurso
        );

        estado = findViewById(
                R.id.txtDetalleEstadoCurso
        );

        Button edit = findViewById(
                R.id.btnEditarCurso
        );

        Button delete = findViewById(
                R.id.btnEliminarCurso
        );

        edit.setOnClickListener(v -> {

            Intent intent = new Intent(
                    this,
                    FormCursoActivity.class
            );

            intent.putExtra(
                    FormCursoActivity.EXTRA_CURSO_ID,
                    cursoId
            );

            startActivity(intent);
        });

        delete.setOnClickListener(
                v -> confirmarEliminacion()
        );
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (cursoId >= 0) {
            cargarCurso();
        }
    }

    private void cargarCurso() {

        setLoading(true);

        RetrofitClient.getInstance(this)
                .getApiService()
                .obtenerCurso(cursoId)
                .enqueue(new Callback<JsonElement>() {

                    @Override
                    public void onResponse(
                            @NonNull Call<JsonElement> call,
                            @NonNull Response<JsonElement> response
                    ) {

                        setLoading(false);

                        if (response.code() == 401) {

                            ApiErrorHandler.handleUnauthorized(
                                    DetalleCursoActivity.this
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

                            Curso curso =
                                    CursoJsonMapper.toCurso(
                                            response.body()
                                    );

                            bind(curso);

                        } catch (IllegalArgumentException error) {

                            showMessage(
                                    "El servidor devolvió un curso inválido."
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

    private void bind(Curso curso) {

        codigo.setText(
                curso.getCodigo()
        );

        nombre.setText(
                curso.getNombre()
        );

        creditos.setText(
                String.valueOf(
                        curso.getCreditos()
                )
        );

        carrera.setText(
                curso.getCarreraNombre() != null
                        ? curso.getCarreraNombre()
                        : "No disponible"
        );

        docente.setText(
                curso.getDocenteNombre() != null
                        ? curso.getDocenteNombre()
                        : "No asignado"
        );

        estado.setText(
                Boolean.TRUE.equals(
                        curso.getActivo()
                )
                        ? "Activo"
                        : "Inactivo"
        );
    }

    private void confirmarEliminacion() {

        new AlertDialog.Builder(this)
                .setTitle("Eliminar curso")
                .setMessage(
                        "¿Desea eliminar o desactivar este curso?"
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

    private void eliminar() {

        setLoading(true);

        RetrofitClient.getInstance(this)
                .getApiService()
                .eliminarCurso(cursoId)
                .enqueue(new Callback<JsonElement>() {

                    @Override
                    public void onResponse(
                            @NonNull Call<JsonElement> call,
                            @NonNull Response<JsonElement> response
                    ) {

                        setLoading(false);

                        if (response.code() == 401) {

                            ApiErrorHandler.handleUnauthorized(
                                    DetalleCursoActivity.this
                            );

                        } else if (response.isSuccessful()) {

                            showMessage(
                                    "Curso actualizado correctamente."
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

    private void setLoading(boolean loading) {

        progressBar.setVisibility(
                loading
                        ? View.VISIBLE
                        : View.GONE
        );
    }

    private void showMessage(String message) {

        Toast.makeText(
                this,
                message,
                Toast.LENGTH_LONG
        ).show();
    }
}


