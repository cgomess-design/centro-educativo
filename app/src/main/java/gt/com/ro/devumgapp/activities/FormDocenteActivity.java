package gt.com.ro.devumgapp.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.google.gson.JsonElement;

import gt.com.ro.devumgapp.R;
import gt.com.ro.devumgapp.network.RetrofitClient;
import gt.com.ro.devumgapp.network.model.Docente;
import gt.com.ro.devumgapp.network.model.DocenteRequest;
import gt.com.ro.devumgapp.utils.ApiErrorHandler;
import gt.com.ro.devumgapp.utils.DocenteJsonMapper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FormDocenteActivity extends AppCompatActivity {

    public static final String EXTRA_DOCENTE_ID = "docente_id";

    private long docenteId = -1;
    private EditText nombres, apellidos, correo, telefono;
    private SwitchCompat swActivo;
    private Button btnGuardar;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_docente);

        docenteId = getIntent().getLongExtra(EXTRA_DOCENTE_ID, -1);

        nombres = findViewById(R.id.edtNombresDocente);
        apellidos = findViewById(R.id.edtApellidosDocente);
        correo = findViewById(R.id.edtCorreoDocente);
        telefono = findViewById(R.id.edtTelefonoDocente);
        swActivo = findViewById(R.id.swActivoDocente);
        btnGuardar = findViewById(R.id.btnGuardarDocente);
        progressBar = findViewById(R.id.progressFormDocente);

        btnGuardar.setText(docenteId < 0 ? "CREAR DOCENTE" : "GUARDAR CAMBIOS");
        btnGuardar.setOnClickListener(v -> guardar());

        if (docenteId >= 0) {
            cargarDocente();
        }
    }

    private void cargarDocente() {
        setLoading(true);
        RetrofitClient.getInstance(this).getApiService().obtenerDocente(docenteId).enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(@NonNull Call<JsonElement> call, @NonNull Response<JsonElement> response) {
                setLoading(false);
                if (response.code() == 401) {
                    ApiErrorHandler.handleUnauthorized(FormDocenteActivity.this);
                    return;
                }
                if (!response.isSuccessful() || response.body() == null) {
                    showMessage(ApiErrorHandler.getMessage(response));
                    return;
                }
                try {
                    populate(DocenteJsonMapper.toDocente(response.body()));
                } catch (IllegalArgumentException error) {
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

    private void populate(Docente docente) {
        nombres.setText(docente.getNombres());
        apellidos.setText(docente.getApellidos());
        correo.setText(docente.getCorreo());
        telefono.setText(docente.getTelefono());
        swActivo.setChecked(Boolean.TRUE.equals(docente.getActivo()));
    }

    private void guardar() {
        DocenteRequest request = createRequest();
        if (request == null) return;

        setLoading(true);
        Call<JsonElement> call = docenteId < 0
                ? RetrofitClient.getInstance(this).getApiService().crearDocente(request)
                : RetrofitClient.getInstance(this).getApiService().actualizarDocente(docenteId, request);

        call.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(@NonNull Call<JsonElement> call, @NonNull Response<JsonElement> response) {
                setLoading(false);
                if (response.code() == 401) {
                    ApiErrorHandler.handleUnauthorized(FormDocenteActivity.this);
                } else if (response.isSuccessful()) {
                    showMessage(docenteId < 0 ? "Docente creado correctamente." : "Docente actualizado correctamente.");
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

    private DocenteRequest createRequest() {
        String n = nombres.getText().toString().trim();
        String a = apellidos.getText().toString().trim();
        String c = correo.getText().toString().trim();
        String t = telefono.getText().toString().trim();
        boolean active = swActivo.isChecked();

        if (n.isEmpty() || a.isEmpty() || c.isEmpty() || t.isEmpty()) {
            showMessage("Complete todos los campos.");
            return null;
        }

        return new DocenteRequest(n, a, c, t, active);
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnGuardar.setEnabled(!loading);
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}

