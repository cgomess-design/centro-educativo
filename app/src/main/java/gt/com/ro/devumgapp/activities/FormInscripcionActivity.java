package gt.com.ro.devumgapp.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import gt.com.ro.devumgapp.R;
import gt.com.ro.devumgapp.network.RetrofitClient;
import gt.com.ro.devumgapp.network.model.Curso;
import gt.com.ro.devumgapp.network.model.Estudiante;
import gt.com.ro.devumgapp.network.model.Inscripcion;
import gt.com.ro.devumgapp.network.model.InscripcionRequest;
import gt.com.ro.devumgapp.utils.ApiErrorHandler;
import gt.com.ro.devumgapp.utils.CursoJsonMapper;
import gt.com.ro.devumgapp.utils.EstudianteJsonMapper;
import gt.com.ro.devumgapp.utils.InscripcionJsonMapper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Formulario utilizado para crear y actualizar inscripciones.
 */
public class FormInscripcionActivity extends AppCompatActivity {

    public static final String EXTRA_INSCRIPCION_ID = "inscripcion_id";
    private static final String OPCION_SELECCIONAR = "Seleccionar";

    private long inscripcionId = -1;

    private Spinner spnEstudiante;
    private Spinner spnCurso;
    private EditText fechaInscripcion;

    private Button guardar;
    private ProgressBar progressBar;

    private List<Estudiante> listaEstudiantes = new ArrayList<>();
    private List<Curso> listaCursos = new ArrayList<>();
    private int catalogosCargados = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_inscripcion);

        inscripcionId = getIntent().getLongExtra(EXTRA_INSCRIPCION_ID, -1);

        spnEstudiante = findViewById(R.id.spnEstudianteInscripcion);
        spnCurso = findViewById(R.id.spnCursoInscripcion);
        fechaInscripcion = findViewById(R.id.edtFechaInscripcion);
        guardar = findViewById(R.id.btnGuardarInscripcion);
        progressBar = findViewById(R.id.progressFormInscripcion);

        fechaInscripcion.setOnClickListener(v -> mostrarDatePicker());

        guardar.setText(
                inscripcionId < 0
                        ? "CREAR INSCRIPCIÓN"
                        : "GUARDAR CAMBIOS"
        );

        guardar.setOnClickListener(v -> guardar());

        cargarCatalogos();
    }

    private void mostrarDatePicker() {
        Calendar calendar = Calendar.getInstance();
        String actual = fechaInscripcion.getText().toString().trim();
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
                    fechaInscripcion.setText(fecha);
                    fechaInscripcion.setError(null);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }

    private void cargarCatalogos() {
        setLoading(true);
        catalogosCargados = 0;
        setSpinnerItems(spnEstudiante, Collections.singletonList(OPCION_SELECCIONAR));
        setSpinnerItems(spnCurso, Collections.singletonList(OPCION_SELECCIONAR));
        cargarEstudiantes();
        cargarCursos();
    }

    private void cargarEstudiantes() {
        RetrofitClient.getInstance(this)
                .getApiService()
                .obtenerEstudiantes()
                .enqueue(new Callback<JsonElement>() {
                    @Override
                    public void onResponse(
                            @NonNull Call<JsonElement> call,
                            @NonNull Response<JsonElement> response
                    ) {
                        if (response.code() == 401) {
                            ApiErrorHandler.handleUnauthorized(FormInscripcionActivity.this);
                            return;
                        }

                        if (response.isSuccessful() && response.body() != null) {
                            try {
                                listaEstudiantes = EstudianteJsonMapper.toList(response.body());
                                List<String> nombres = new ArrayList<>();
                                nombres.add(OPCION_SELECCIONAR);
                                for (Estudiante estudiante : listaEstudiantes) {
                                    String nom = estudiante.getNombreCompleto();
                                    if (nom.isEmpty()) {
                                        nombres.add("Estudiante #" + estudiante.getId());
                                    } else if (estudiante.getCarnet() != null && !estudiante.getCarnet().isEmpty()) {
                                        nombres.add(nom + " (" + estudiante.getCarnet() + ")");
                                    } else {
                                        nombres.add(nom);
                                    }
                                }
                                setSpinnerItems(spnEstudiante, nombres);
                            } catch (IllegalArgumentException error) {
                                showMessage("El servidor devolvió una lista de estudiantes inválida.");
                            }
                        } else {
                            showMessage(ApiErrorHandler.getMessage(response));
                        }

                        verificarCatalogosCompletados();
                    }

                    @Override
                    public void onFailure(
                            @NonNull Call<JsonElement> call,
                            @NonNull Throwable error
                    ) {
                        showMessage(ApiErrorHandler.getNetworkMessage(error));
                        verificarCatalogosCompletados();
                    }
                });
    }

    private void cargarCursos() {
        RetrofitClient.getInstance(this)
                .getApiService()
                .obtenerCursos()
                .enqueue(new Callback<JsonElement>() {
                    @Override
                    public void onResponse(
                            @NonNull Call<JsonElement> call,
                            @NonNull Response<JsonElement> response
                    ) {
                        if (response.code() == 401) {
                            ApiErrorHandler.handleUnauthorized(FormInscripcionActivity.this);
                            return;
                        }

                        if (response.isSuccessful() && response.body() != null) {
                            try {
                                listaCursos = CursoJsonMapper.toList(response.body());
                                List<String> nombres = new ArrayList<>();
                                nombres.add(OPCION_SELECCIONAR);
                                for (Curso curso : listaCursos) {
                                    String nom = curso.getNombre();
                                    if (curso.getCodigo() != null && !curso.getCodigo().isEmpty()) {
                                        nombres.add(nom + " (" + curso.getCodigo() + ")");
                                    } else {
                                        nombres.add(nom);
                                    }
                                }
                                setSpinnerItems(spnCurso, nombres);
                            } catch (IllegalArgumentException error) {
                                showMessage("El servidor devolvió una lista de cursos inválida.");
                            }
                        } else {
                            showMessage(ApiErrorHandler.getMessage(response));
                        }

                        verificarCatalogosCompletados();
                    }

                    @Override
                    public void onFailure(
                            @NonNull Call<JsonElement> call,
                            @NonNull Throwable error
                    ) {
                        showMessage(ApiErrorHandler.getNetworkMessage(error));
                        verificarCatalogosCompletados();
                    }
                });
    }

    private void verificarCatalogosCompletados() {
        catalogosCargados++;
        if (catalogosCargados < 2) {
            return;
        }

        if (inscripcionId >= 0) {
            cargarInscripcion();
        } else {
            setLoading(false);
        }
    }

    private void setSpinnerItems(Spinner spinner, List<String> items) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                items
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    /**
     * Carga la inscripción cuando estamos editando.
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
                            ApiErrorHandler.handleUnauthorized(FormInscripcionActivity.this);
                            return;
                        }

                        if (!response.isSuccessful() || response.body() == null) {
                            showMessage(ApiErrorHandler.getMessage(response));
                            return;
                        }

                        try {
                            Inscripcion inscripcion =
                                    InscripcionJsonMapper.toInscripcion(response.body());
                            populate(inscripcion);
                        } catch (IllegalArgumentException error) {
                            showMessage("El servidor devolvió una inscripción inválida.");
                        }
                    }

                    @Override
                    public void onFailure(
                            @NonNull Call<JsonElement> call,
                            @NonNull Throwable error
                    ) {
                        setLoading(false);
                        showMessage(ApiErrorHandler.getNetworkMessage(error));
                    }
                });
    }

    /**
     * Coloca los datos de la inscripción en el formulario.
     */
    private void populate(Inscripcion inscripcion) {
        if (inscripcion.getEstudianteId() != null) {
            for (int i = 0; i < listaEstudiantes.size(); i++) {
                if (listaEstudiantes.get(i).getId() == inscripcion.getEstudianteId()) {
                    spnEstudiante.setSelection(i + 1);
                    break;
                }
            }
        }

        if (inscripcion.getCursoId() != null) {
            for (int i = 0; i < listaCursos.size(); i++) {
                if (listaCursos.get(i).getId() == inscripcion.getCursoId()) {
                    spnCurso.setSelection(i + 1);
                    break;
                }
            }
        }

        if (inscripcion.getFechaInscripcion() != null) {
            fechaInscripcion.setText(inscripcion.getFechaInscripcion());
        }
    }

    /**
     * Crea o actualiza una inscripción.
     */
    private void guardar() {
        InscripcionRequest request = createRequest();
        if (request == null) {
            return;
        }

        setLoading(true);

        Call<JsonElement> call = inscripcionId < 0
                ? RetrofitClient.getInstance(this).getApiService().crearInscripcion(request)
                : RetrofitClient.getInstance(this).getApiService().actualizarInscripcion(inscripcionId, request);

        call.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(
                    @NonNull Call<JsonElement> call,
                    @NonNull Response<JsonElement> response
            ) {
                setLoading(false);

                if (response.code() == 401) {
                    ApiErrorHandler.handleUnauthorized(FormInscripcionActivity.this);
                } else if (response.isSuccessful()) {
                    showMessage(
                            inscripcionId < 0
                                    ? "Inscripción creada correctamente."
                                    : "Inscripción actualizada correctamente."
                    );
                    finish();
                } else {
                    showMessage(ApiErrorHandler.getMessage(response));
                }
            }

            @Override
            public void onFailure(
                    @NonNull Call<JsonElement> call,
                    @NonNull Throwable error
            ) {
                setLoading(false);
                showMessage(ApiErrorHandler.getNetworkMessage(error));
            }
        });
    }

    /**
     * Valida los datos antes de enviarlos al backend.
     */
    private InscripcionRequest createRequest() {
        if (listaEstudiantes.isEmpty()) {
            showMessage("No hay estudiantes disponibles.");
            return null;
        }

        if (listaCursos.isEmpty()) {
            showMessage("No hay cursos disponibles.");
            return null;
        }

        int estudiantePosition = spnEstudiante.getSelectedItemPosition();
        int cursoPosition = spnCurso.getSelectedItemPosition();

        if (estudiantePosition <= 0 || estudiantePosition > listaEstudiantes.size()) {
            showMessage("Debe seleccionar un estudiante.");
            return null;
        }

        if (cursoPosition <= 0 || cursoPosition > listaCursos.size()) {
            showMessage("Debe seleccionar un curso.");
            return null;
        }

        Long estudianteIdNumber = listaEstudiantes.get(estudiantePosition - 1).getId();
        long cursoIdNumber = listaCursos.get(cursoPosition - 1).getId();

        if (estudianteIdNumber <= 0 || cursoIdNumber <= 0) {
            showMessage("Seleccione un estudiante y un curso válidos.");
            return null;
        }

        String fechaValue = fechaInscripcion.getText().toString().trim();

        if (!fechaValue.isEmpty() && !fechaValue.matches("\\d{4}-\\d{2}-\\d{2}")) {
            showMessage("La fecha debe tener el formato AAAA-MM-DD.");
            return null;
        }

        return new InscripcionRequest(
                estudianteIdNumber,
                cursoIdNumber,
                fechaValue.isEmpty() ? null : fechaValue
        );
    }

    /**
     * Controla el indicador de carga.
     */
    private void setLoading(boolean loading) {
        progressBar.setVisibility(
                loading ? View.VISIBLE : View.GONE
        );
        guardar.setEnabled(!loading);
    }

    /**
     * Muestra mensajes al usuario.
     */
    private void showMessage(String message) {
        Toast.makeText(
                this,
                message,
                Toast.LENGTH_LONG
        ).show();
    }
}
