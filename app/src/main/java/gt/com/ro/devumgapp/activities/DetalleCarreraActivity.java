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
import gt.com.ro.devumgapp.network.model.Carrera;
import gt.com.ro.devumgapp.utils.ApiErrorHandler;
import gt.com.ro.devumgapp.utils.CarreraJsonMapper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetalleCarreraActivity extends AppCompatActivity {
    public static final String EXTRA_CARRERA_ID = "carrera_id";

    private long carreraId;
    private ProgressBar progressBar;
    private TextView codigo;
    private TextView nombre;
    private TextView estado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_carrera);

        carreraId = getIntent().getLongExtra(EXTRA_CARRERA_ID, -1);
        if (carreraId < 0) {
            Toast.makeText(this, "Carrera no válida.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        progressBar = findViewById(R.id.progressDetalleCarrera);
        codigo = findViewById(R.id.txtDetalleCodigoCarrera);
        nombre = findViewById(R.id.txtDetalleNombreCarrera);
        estado = findViewById(R.id.txtDetalleEstadoCarrera);

        Button edit = findViewById(R.id.btnEditarCarrera);
        Button delete = findViewById(R.id.btnEliminarCarrera);
        edit.setOnClickListener(v -> {
            Intent intent = new Intent(this, FormCarreraActivity.class);
            intent.putExtra(FormCarreraActivity.EXTRA_CARRERA_ID, carreraId);
            startActivity(intent);
        });
        delete.setOnClickListener(v -> confirmarEliminacion());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (carreraId >= 0) cargarCarrera();
    }

    private void cargarCarrera() {
        setLoading(true);
        RetrofitClient.getInstance(this).getApiService().obtenerCarrera(carreraId)
                .enqueue(new Callback<JsonElement>() {
                    @Override
                    public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                        setLoading(false);
                        if (response.code() == 401) {
                            ApiErrorHandler.handleUnauthorized(DetalleCarreraActivity.this);
                            return;
                        }
                        if (!response.isSuccessful() || response.body() == null) {
                            showMessage(ApiErrorHandler.getMessage(response));
                            return;
                        }
                        try {
                            bind(CarreraJsonMapper.toCarrera(response.body()));
                        } catch (IllegalArgumentException error) {
                            showMessage("El servidor devolvió una carrera inválida.");
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonElement> call, Throwable error) {
                        setLoading(false);
                        showMessage(ApiErrorHandler.getNetworkMessage(error));
                    }
                });
    }

    private void bind(Carrera carrera) {
        codigo.setText(carrera.getCodigo());
        nombre.setText(carrera.getNombre());
        estado.setText(Boolean.TRUE.equals(carrera.getActivo()) ? "Activa" : "Inactiva");
    }

    private void confirmarEliminacion() {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar carrera")
                .setMessage("¿Desea eliminar o desactivar esta carrera?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Confirmar", (dialog, which) -> eliminar())
                .show();
    }

    private void eliminar() {
        setLoading(true);
        RetrofitClient.getInstance(this).getApiService().eliminarCarrera(carreraId)
                .enqueue(new Callback<JsonElement>() {
                    @Override
                    public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                        setLoading(false);
                        if (response.code() == 401) {
                            ApiErrorHandler.handleUnauthorized(DetalleCarreraActivity.this);
                        } else if (response.isSuccessful()) {
                            showMessage("Carrera eliminada correctamente.");
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

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
