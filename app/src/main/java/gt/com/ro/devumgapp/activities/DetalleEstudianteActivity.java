package gt.com.ro.devumgapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.JsonElement;

import gt.com.ro.devumgapp.R;
import gt.com.ro.devumgapp.network.RetrofitClient;
import gt.com.ro.devumgapp.network.model.Estudiante;
import gt.com.ro.devumgapp.utils.ApiErrorHandler;
import gt.com.ro.devumgapp.utils.EstudianteJsonMapper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetalleEstudianteActivity extends AppCompatActivity {
    public static final String EXTRA_ESTUDIANTE_ID = "estudiante_id";
    private long estudianteId;
    private ProgressBar progressBar;
    private TextView codigo;
    private TextView carnet;
    private TextView nombre;
    private TextView email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_estudiante);
        estudianteId = getIntent().getLongExtra(EXTRA_ESTUDIANTE_ID, -1);
        if (estudianteId < 0) {
            Toast.makeText(this, "Estudiante no válido.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        progressBar = findViewById(R.id.progressDetalleEstudiante);
        codigo = findViewById(R.id.txtDetalleCodigo);
        carnet = findViewById(R.id.txtDetalleCarnet);
        nombre = findViewById(R.id.txtDetalleNombre);
        email = findViewById(R.id.txtDetalleEmail);
        Button edit = findViewById(R.id.btnEditarEstudiante);
        Button delete = findViewById(R.id.btnEliminarEstudiante);
        edit.setOnClickListener(v -> {
            Intent intent = new Intent(this, FormEstudianteActivity.class);
            intent.putExtra(FormEstudianteActivity.EXTRA_ESTUDIANTE_ID, estudianteId);
            startActivity(intent);
        });
        delete.setOnClickListener(v -> confirmarEliminacion());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (estudianteId >= 0) cargarEstudiante();
    }

    private void cargarEstudiante() {
        setLoading(true);
        RetrofitClient.getInstance(this).getApiService().obtenerEstudiante(estudianteId)
                .enqueue(new Callback<JsonElement>() {
                    @Override
                    public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                        setLoading(false);
                        if (response.code() == 401) {
                            ApiErrorHandler.handleUnauthorized(DetalleEstudianteActivity.this);
                            return;
                        }
                        if (!response.isSuccessful() || response.body() == null) {
                            showMessage(ApiErrorHandler.getMessage(response));
                            return;
                        }
                        try { bind(EstudianteJsonMapper.toEstudiante(response.body())); }
                        catch (IllegalArgumentException error) {
                            showMessage("El servidor devolvió un estudiante inválido.");
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonElement> call, Throwable error) {
                        setLoading(false);
                        showMessage(ApiErrorHandler.getNetworkMessage(error));
                    }
                });
    }

    private void bind(Estudiante estudiante) {
        codigo.setText(estudiante.getCodigoEstudiante());
        carnet.setText(estudiante.getCarnet());
        nombre.setText(estudiante.getNombreCompleto());
        email.setText(estudiante.getEmail());
    }

    private void confirmarEliminacion() {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar estudiante")
                .setMessage("¿Desea eliminar o desactivar este estudiante?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Confirmar", (dialog, which) -> eliminar())
                .show();
    }

    private void eliminar() {
        setLoading(true);
        RetrofitClient.getInstance(this).getApiService().eliminarEstudiante(estudianteId)
                .enqueue(new Callback<JsonElement>() {
                    @Override
                    public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                        setLoading(false);
                        if (response.code() == 401) {
                            ApiErrorHandler.handleUnauthorized(DetalleEstudianteActivity.this);
                        } else if (response.isSuccessful()) {
                            showMessage("Estudiante actualizado correctamente.");
                            finish();
                        } else {
                            showMessage(ApiErrorHandler.getMessage(response));
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonElement> call, Throwable error) {
                        setLoading(false);
                        showMessage(ApiErrorHandler.getNetworkMessage(error));
                    }
                });
    }

    private void setLoading(boolean loading) { progressBar.setVisibility(loading ? View.VISIBLE : View.GONE); }
    private void showMessage(String message) { Toast.makeText(this, message, Toast.LENGTH_LONG).show(); }
}
