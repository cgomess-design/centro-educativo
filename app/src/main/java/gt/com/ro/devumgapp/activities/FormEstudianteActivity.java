package gt.com.ro.devumgapp.activities;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.JsonElement;

import gt.com.ro.devumgapp.R;
import gt.com.ro.devumgapp.network.RetrofitClient;
import gt.com.ro.devumgapp.network.model.Estudiante;
import gt.com.ro.devumgapp.network.model.EstudianteRequest;
import gt.com.ro.devumgapp.utils.ApiErrorHandler;
import gt.com.ro.devumgapp.utils.EstudianteJsonMapper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Usa el mismo formulario para POST y PUT de estudiantes. */
public class FormEstudianteActivity extends AppCompatActivity {
    public static final String EXTRA_ESTUDIANTE_ID = "estudiante_id";
    private long estudianteId = -1;
    private EditText codigo;
    private EditText carnet;
    private EditText email;
    private EditText nombre;
    private EditText apellido;
    private Button guardar;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_estudiante);
        estudianteId = getIntent().getLongExtra(EXTRA_ESTUDIANTE_ID, -1);
        codigo = findViewById(R.id.edtCodigoEstudiante);
        carnet = findViewById(R.id.edtCarnetEstudiante);
        email = findViewById(R.id.edtEmailEstudiante);
        nombre = findViewById(R.id.edtNombreEstudiante);
        apellido = findViewById(R.id.edtApellidoEstudiante);
        guardar = findViewById(R.id.btnGuardarEstudiante);
        progressBar = findViewById(R.id.progressFormEstudiante);
        guardar.setText(estudianteId < 0 ? "CREAR ESTUDIANTE" : "GUARDAR CAMBIOS");
        guardar.setOnClickListener(v -> guardar());
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
                            ApiErrorHandler.handleUnauthorized(FormEstudianteActivity.this);
                            return;
                        }
                        if (!response.isSuccessful() || response.body() == null) {
                            showMessage(ApiErrorHandler.getMessage(response));
                            return;
                        }
                        try { populate(EstudianteJsonMapper.toEstudiante(response.body())); }
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

    private void populate(Estudiante estudiante) {
        codigo.setText(estudiante.getCodigoEstudiante());
        carnet.setText(estudiante.getCarnet());
        email.setText(estudiante.getEmail());
        nombre.setText(estudiante.getNombre());
        apellido.setText(estudiante.getApellido());
    }

    private void guardar() {
        EstudianteRequest request = createRequest();
        if (request == null) return;
        setLoading(true);
        Call<JsonElement> call = estudianteId < 0
                ? RetrofitClient.getInstance(this).getApiService().crearEstudiante(request)
                : RetrofitClient.getInstance(this).getApiService().actualizarEstudiante(estudianteId, request);
        call.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                setLoading(false);
                if (response.code() == 401) {
                    ApiErrorHandler.handleUnauthorized(FormEstudianteActivity.this);
                } else if (response.isSuccessful()) {
                    showMessage(estudianteId < 0 ? "Estudiante creado correctamente." : "Estudiante actualizado correctamente.");
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

    private EstudianteRequest createRequest() {
        String codigoValue = codigo.getText().toString().trim();
        String carnetValue = carnet.getText().toString().trim();
        String emailValue = email.getText().toString().trim();
        String nombreValue = nombre.getText().toString().trim();
        String apellidoValue = apellido.getText().toString().trim();
        if (codigoValue.isEmpty() || carnetValue.isEmpty() || emailValue.isEmpty()
                || nombreValue.isEmpty() || apellidoValue.isEmpty()) {
            showMessage("Complete todos los campos.");
            return null;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(emailValue).matches()) {
            email.setError("Ingrese un correo válido.");
            return null;
        }
        return new EstudianteRequest(codigoValue, carnetValue, emailValue, nombreValue, apellidoValue);
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        guardar.setEnabled(!loading);
    }

    private void showMessage(String message) { Toast.makeText(this, message, Toast.LENGTH_LONG).show(); }
}
