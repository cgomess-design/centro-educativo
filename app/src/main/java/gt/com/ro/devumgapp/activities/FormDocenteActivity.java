package gt.com.ro.devumgapp.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.google.gson.JsonElement;

import java.util.Calendar;
import java.util.Locale;

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
    private EditText nombre;
    private EditText apellido;
    private EditText emailInstitucional;
    private EditText emailPersonal;
    private EditText dpi;
    private EditText telefono;
    private EditText especialidad;
    private EditText fechaContratacion;
    private SwitchCompat swActivo;
    private Button btnGuardar;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_docente);

        docenteId = getIntent().getLongExtra(EXTRA_DOCENTE_ID, -1);

        nombre = findViewById(R.id.edtNombreDocente);
        apellido = findViewById(R.id.edtApellidoDocente);
        emailInstitucional = findViewById(R.id.edtEmailInstitucionalDocente);
        emailPersonal = findViewById(R.id.edtEmailPersonalDocente);
        dpi = findViewById(R.id.edtDpiDocente);
        telefono = findViewById(R.id.edtTelefonoDocente);
        especialidad = findViewById(R.id.edtEspecialidadDocente);
        fechaContratacion = findViewById(R.id.edtFechaContratacionDocente);
        swActivo = findViewById(R.id.swActivoDocente);
        btnGuardar = findViewById(R.id.btnGuardarDocente);
        progressBar = findViewById(R.id.progressFormDocente);

        fechaContratacion.setOnClickListener(v -> mostrarDatePicker());

        btnGuardar.setText(docenteId < 0 ? "CREAR DOCENTE" : "GUARDAR CAMBIOS");
        btnGuardar.setOnClickListener(v -> guardar());

        if (docenteId >= 0) {
            cargarDocente();
        }
    }

    private void mostrarDatePicker() {
        Calendar calendar = Calendar.getInstance();
        String actual = fechaContratacion.getText().toString().trim();
        if (!actual.isEmpty()) {
            try {
                String[] parts = actual.split("-");
                if (parts.length == 3) {
                    int year = Integer.parseInt(parts[0]);
                    int month = Integer.parseInt(parts[1]) - 1;
                    int day = Integer.parseInt(parts[2]);
                    calendar.set(year, month, day);
                }
            } catch (Exception ignored) {
            }
        }

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    String fecha = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                    fechaContratacion.setText(fecha);
                    fechaContratacion.setError(null);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
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

    private void populate(Docente docente) {
        if (docente == null) return;
        nombre.setText(docente.getNombre());
        apellido.setText(docente.getApellido());
        emailInstitucional.setText(docente.getemailInstitucional());
        emailPersonal.setText(docente.getEmailPersonal());
        dpi.setText(docente.getDpi());
        telefono.setText(docente.getTelefono());
        especialidad.setText(docente.getEspecialidad());
        fechaContratacion.setText(docente.getFechaContratacion());
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
        String nom = nombre.getText().toString().trim();
        String ape = apellido.getText().toString().trim();
        String emailInst = emailInstitucional.getText().toString().trim();
        String emailPers = emailPersonal.getText().toString().trim();
        String numDpi = dpi.getText().toString().trim();
        String tel = telefono.getText().toString().trim();
        String esp = especialidad.getText().toString().trim();
        String fecha = fechaContratacion.getText().toString().trim();
        boolean active = swActivo.isChecked();

        if (nom.isEmpty()) {
            nombre.setError("El nombre es requerido");
            nombre.requestFocus();
            return null;
        }
        if (ape.isEmpty()) {
            apellido.setError("El apellido es requerido");
            apellido.requestFocus();
            return null;
        }
        if (emailInst.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(emailInst).matches()) {
            emailInstitucional.setError("Ingrese un correo institucional válido");
            emailInstitucional.requestFocus();
            return null;
        }
        if (!emailPers.isEmpty() && !Patterns.EMAIL_ADDRESS.matcher(emailPers).matches()) {
            emailPersonal.setError("Ingrese un correo personal válido");
            emailPersonal.requestFocus();
            return null;
        }
        if (numDpi.isEmpty()) {
            dpi.setError("El DPI es requerido");
            dpi.requestFocus();
            return null;
        }
        if (tel.isEmpty()) {
            telefono.setError("El teléfono es requerido");
            telefono.requestFocus();
            return null;
        }
        if (esp.isEmpty()) {
            especialidad.setError("La especialidad es requerida");
            especialidad.requestFocus();
            return null;
        }
        if (fecha.isEmpty()) {
            fechaContratacion.setError("Seleccione la fecha de contratación");
            fechaContratacion.requestFocus();
            return null;
        }

        return new DocenteRequest(nom, ape, emailInst, emailPers, numDpi, tel, esp, fecha, active);
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnGuardar.setEnabled(!loading);
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
