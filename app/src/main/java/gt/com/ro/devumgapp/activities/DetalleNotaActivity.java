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
import gt.com.ro.devumgapp.network.model.Nota;
import gt.com.ro.devumgapp.utils.ApiErrorHandler;
import gt.com.ro.devumgapp.utils.NotaJsonMapper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetalleNotaActivity extends AppCompatActivity {

    public static final String EXTRA_NOTA_ID = "nota_id";

    private long notaId;
    private ProgressBar progressBar;
    private TextView estudiante, curso, valor, estado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_nota);

        notaId = getIntent().getLongExtra(EXTRA_NOTA_ID, -1);
        if (notaId < 0) {
            Toast.makeText(this, "Nota no válida.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        progressBar = findViewById(R.id.progressDetalleNota);
        estudiante = findViewById(R.id.txtDetalleEstudianteNota);
        curso = findViewById(R.id.txtDetalleCursoNota);
        valor = findViewById(R.id.txtDetalleValorNota);
        estado = findViewById(R.id.txtDetalleEstadoNota);

        findViewById(R.id.btnEditarNota).setOnClickListener(v -> {
            Intent intent = new Intent(this, FormNotaActivity.class);
            intent.putExtra(FormNotaActivity.EXTRA_NOTA_ID, notaId);
            startActivity(intent);
        });

        findViewById(R.id.btnEliminarNota).setOnClickListener(v -> confirmarEliminacion());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (notaId >= 0) cargarNota();
    }

    private void cargarNota() {
        setLoading(true);
        RetrofitClient.getInstance(this).getApiService().obtenerNota(notaId).enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(@NonNull Call<JsonElement> call, @NonNull Response<JsonElement> response) {
                setLoading(false);
                if (response.code() == 401) {
                    ApiErrorHandler.handleUnauthorized(DetalleNotaActivity.this);
                    return;
                }
                if (!response.isSuccessful() || response.body() == null) {
                    showMessage(ApiErrorHandler.getMessage(response));
                    return;
                }
                try {
                    bind(NotaJsonMapper.toNota(response.body()));
                } catch (IllegalArgumentException error) {
                    showMessage("El servidor devolvió una nota inválida.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonElement> call, @NonNull Throwable error) {
                setLoading(false);
                showMessage(ApiErrorHandler.getNetworkMessage(error));
            }
        });
    }

    private void bind(Nota nota) {
        estudiante.setText(nota.getEstudianteNombre() != null ? nota.getEstudianteNombre() : "ID: " + nota.getEstudianteId());
        curso.setText(nota.getCursoNombre() != null ? nota.getCursoNombre() : "ID: " + nota.getCursoId());
        valor.setText(String.valueOf(nota.getNota()));
        estado.setText(Boolean.TRUE.equals(nota.getActivo()) ? "Activo" : "Inactivo");
    }

    private void confirmarEliminacion() {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar nota")
                .setMessage("¿Desea eliminar este registro de nota?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Confirmar", (dialog, which) -> eliminar())
                .show();
    }

    private void eliminar() {
        setLoading(true);
        RetrofitClient.getInstance(this).getApiService().eliminarNota(notaId).enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(@NonNull Call<JsonElement> call, @NonNull Response<JsonElement> response) {
                setLoading(false);
                if (response.code() == 401) {
                    ApiErrorHandler.handleUnauthorized(DetalleNotaActivity.this);
                } else if (response.isSuccessful()) {
                    showMessage("Nota eliminada correctamente.");
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
