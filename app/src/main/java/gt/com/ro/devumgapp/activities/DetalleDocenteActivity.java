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
import gt.com.ro.devumgapp.network.model.Docente;
import gt.com.ro.devumgapp.utils.ApiErrorHandler;
import gt.com.ro.devumgapp.utils.DocenteJsonMapper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetalleDocenteActivity extends AppCompatActivity {

    public static final String EXTRA_DOCENTE_ID = "docente_id";

    private long docenteId;
    private ProgressBar progressBar;
    private TextView nombre, apellido, emailInstitucional, emailPersonal, dpi,  telefono, especialidad, fechaContratacion, estado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_docente);

        docenteId = getIntent().getLongExtra(EXTRA_DOCENTE_ID, -1);
        if (docenteId < 0) {
            Toast.makeText(this, "Docente no válido.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        progressBar = findViewById(R.id.progressDetalleDocente);
        nombre = findViewById(R.id.txtDetalleNombresDocente);
        apellido = findViewById(R.id.txtDetalleApellidosDocente);
        emailInstitucional = findViewById(R.id.txtDetalleEmailInstitucionalDocente);
        emailPersonal = findViewById(R.id.txtDetalleEmailPersonalDocente);
        dpi = findViewById(R.id.txtDetalleDpiDocente);
        telefono = findViewById(R.id.txtDetalleTelefonoDocente);
        especialidad = findViewById(R.id.txtDetalleEspecialidadDocente);
        fechaContratacion = findViewById(R.id.txtDetalleFechaContratacionDocente);
        estado = findViewById(R.id.txtDetalleEstadoDocente);

        findViewById(R.id.btnEditarDocente).setOnClickListener(v -> {
            Intent intent = new Intent(this, FormDocenteActivity.class);
            intent.putExtra(FormDocenteActivity.EXTRA_DOCENTE_ID, docenteId);
            startActivity(intent);
        });

        findViewById(R.id.btnEliminarDocente).setOnClickListener(v -> confirmarEliminacion());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (docenteId >= 0) cargarDocente();
    }

    private void cargarDocente() {
        setLoading(true);
        RetrofitClient.getInstance(this).getApiService().obtenerDocente(docenteId).enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(@NonNull Call<JsonElement> call, @NonNull Response<JsonElement> response) {
                setLoading(false);
                if (response.code() == 401) {
                    ApiErrorHandler.handleUnauthorized(DetalleDocenteActivity.this);
                    return;
                }
                if (!response.isSuccessful() || response.body() == null) {
                    showMessage(ApiErrorHandler.getMessage(response));
                    return;
                }
                try {
                    bind(DocenteJsonMapper.toDocente(response.body()));
                } catch (Exception error) {
                    showMessage("El servidor devolvió un docente inválido.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonElement> call, @NonNull Throwable error) {
                setLoading(false);
                showMessage(ApiErrorHandler.getNetworkMessage(error));
            }
        });
    }

    private void bind(Docente docente) {
        nombre.setText(docente.getNombre());
        apellido.setText(docente.getApellido());
        emailInstitucional.setText(docente.getemailInstitucional());
        emailPersonal.setText(docente.getEmailPersonal());
        dpi.setText(docente.getDpi());
        telefono.setText(docente.getTelefono());
        especialidad.setText(docente.getEspecialidad());
        fechaContratacion.setText(docente.getFechaContratacion() != null ? docente.getFechaContratacion().toString() : "");
        estado.setText(Boolean.TRUE.equals(docente.getActivo()) ? "Activo" : "Inactivo");
    }

    private void confirmarEliminacion() {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar docente")
                .setMessage("¿Desea eliminar o desactivar este docente?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Confirmar", (dialog, which) -> eliminar())
                .show();
    }

    private void eliminar() {
        setLoading(true);
        RetrofitClient.getInstance(this).getApiService().eliminarDocente(docenteId).enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(@NonNull Call<JsonElement> call, @NonNull Response<JsonElement> response) {
                setLoading(false);
                if (response.code() == 401) {
                    ApiErrorHandler.handleUnauthorized(DetalleDocenteActivity.this);
                } else if (response.isSuccessful()) {
                    showMessage("Docente actualizado correctamente.");
                    finish();
                } else {
                    showMessage(ApiErrorHandler.getMessage(response));
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonElement> call, @NonNull Throwable error) {
                setLoading(false);
                showMessage(ApiErrorHandler.getNetworkMessage(error));
            }
        });
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}

