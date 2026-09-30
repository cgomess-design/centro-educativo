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
import gt.com.ro.devumgapp.network.model.Carrera;
import gt.com.ro.devumgapp.network.model.CarreraRequest;
import gt.com.ro.devumgapp.utils.ApiErrorHandler;
import gt.com.ro.devumgapp.utils.CarreraJsonMapper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Usa el mismo formulario para POST y PUT de carreras. */
public class FormCarreraActivity extends AppCompatActivity {
    public static final String EXTRA_CARRERA_ID = "carrera_id";

    private long carreraId = -1;
    private EditText codigo;
    private EditText nombre;
    private Button guardar;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_carrera);
        gt.com.ro.devumgapp.utils.KeyboardAdjustHelper.setupKeyboardAdjustment(this);

        carreraId = getIntent().getLongExtra(EXTRA_CARRERA_ID, -1);
        codigo = findViewById(R.id.edtCodigoCarrera);
        nombre = findViewById(R.id.edtNombreCarrera);
        guardar = findViewById(R.id.btnGuardarCarrera);
        progressBar = findViewById(R.id.progressFormCarrera);

        guardar.setText(carreraId < 0 ? "CREAR CARRERA" : "GUARDAR CAMBIOS");
        guardar.setOnClickListener(v -> guardar());
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
                            ApiErrorHandler.handleUnauthorized(FormCarreraActivity.this);
                            return;
                        }
                        if (!response.isSuccessful() || response.body() == null) {
                            showMessage(ApiErrorHandler.getMessage(response));
                            return;
                        }
                        try {
                            populate(CarreraJsonMapper.toCarrera(response.body()));
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

    private void populate(Carrera carrera) {
        codigo.setText(carrera.getCodigo());
        nombre.setText(carrera.getNombre());
    }

    private void guardar() {
        CarreraRequest request = createRequest();
        if (request == null) return;

        setLoading(true);
        Call<JsonElement> call = carreraId < 0
                ? RetrofitClient.getInstance(this).getApiService().crearCarrera(request)
                : RetrofitClient.getInstance(this).getApiService()
                        .actualizarCarrera(carreraId, request);
        call.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                setLoading(false);
                if (response.code() == 401) {
                    ApiErrorHandler.handleUnauthorized(FormCarreraActivity.this);
                } else if (response.isSuccessful()) {
                    showMessage(carreraId < 0
                            ? "Carrera creada correctamente."
                            : "Carrera actualizada correctamente.");
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

    private CarreraRequest createRequest() {
        String codigoValue = codigo.getText().toString().trim();
        String nombreValue = nombre.getText().toString().trim();
        if (codigoValue.isEmpty() || nombreValue.isEmpty()) {
            showMessage("Complete todos los campos.");
            return null;
        }
        if (codigoValue.length() > 20) {
            codigo.setError("El código no puede superar los 20 caracteres.");
            return null;
        }
        if (nombreValue.length() > 150) {
            nombre.setError("El nombre no puede superar los 150 caracteres.");
            return null;
        }
        return new CarreraRequest(codigoValue, nombreValue);
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        guardar.setEnabled(!loading);
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
