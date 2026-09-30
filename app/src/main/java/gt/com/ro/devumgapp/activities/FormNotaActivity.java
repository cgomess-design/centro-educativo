package gt.com.ro.devumgapp.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.google.gson.JsonElement;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import gt.com.ro.devumgapp.R;
import gt.com.ro.devumgapp.network.RetrofitClient;
import gt.com.ro.devumgapp.network.model.Curso;
import gt.com.ro.devumgapp.network.model.Estudiante;
import gt.com.ro.devumgapp.network.model.Inscripcion;
import gt.com.ro.devumgapp.network.model.Nota;
import gt.com.ro.devumgapp.network.model.NotaRequest;
import gt.com.ro.devumgapp.utils.ApiErrorHandler;
import gt.com.ro.devumgapp.utils.CursoJsonMapper;
import gt.com.ro.devumgapp.utils.EstudianteJsonMapper;
import gt.com.ro.devumgapp.utils.InscripcionJsonMapper;
import gt.com.ro.devumgapp.utils.KeyboardAdjustHelper;
import gt.com.ro.devumgapp.utils.NotaJsonMapper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Formulario reutilizado para crear y actualizar notas. */
public class FormNotaActivity extends AppCompatActivity {

    public static final String EXTRA_NOTA_ID = "nota_id";
    public static final String EXTRA_CURSO_ID = "curso_id";
    public static final String EXTRA_CURSO_NOMBRE = "curso_nombre";

    private long notaId = -1;
    private long scopedCourseId = -1;
    private String scopedCourseNombre;
    private Spinner spnEstudiante;
    private Spinner spnCurso;
    private EditText edtCicloAcademico;
    private EditText edtZona;
    private EditText edtExamenFinal;
    private EditText edtNota;
    private Spinner spnEstado;
    private EditText edtFechaRegistro;
    private SwitchCompat swActivo;
    private Button btnGuardar;
    private ProgressBar progressBar;
    private boolean isUpdatingNota = false;

    private List<Estudiante> listaEstudiantes = new ArrayList<>();
    private final Map<Long, Estudiante> estudiantesMap = new HashMap<>();
    private List<Curso> listaCursos = new ArrayList<>();
    private List<Inscripcion> inscripcionesDelCurso = new ArrayList<>();
    private Nota notaCargada = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_nota);

        notaId = getIntent().getLongExtra(EXTRA_NOTA_ID, -1);
        scopedCourseId = getIntent().getLongExtra(EXTRA_CURSO_ID, -1);
        scopedCourseNombre = getIntent().getStringExtra(EXTRA_CURSO_NOMBRE);

        spnEstudiante = findViewById(R.id.spnEstudianteNota);
        spnCurso = findViewById(R.id.spnCursoNota);
        edtCicloAcademico = findViewById(R.id.edtCicloAcademicoNota);
        edtZona = findViewById(R.id.edtZonaNota);
        edtExamenFinal = findViewById(R.id.edtExamenFinalNota);
        edtNota = findViewById(R.id.edtValorNota);
        spnEstado = findViewById(R.id.spnEstadoNota);
        edtFechaRegistro = findViewById(R.id.edtFechaRegistroNota);
        swActivo = findViewById(R.id.swActivoNota);
        btnGuardar = findViewById(R.id.btnGuardarNota);
        progressBar = findViewById(R.id.progressFormNota);

        // Valores por defecto
        edtCicloAcademico.setText("Primer Semestre 2026");
        swActivo.setChecked(true);

        KeyboardAdjustHelper.setupKeyboardAdjustment(this);
        configurarEstadoSpinner();
        configurarFechaRegistro();
        configurarCalculoAutomaticoNota();

        btnGuardar.setText(notaId < 0 ? "REGISTRAR NOTA" : "GUARDAR CAMBIOS");
        btnGuardar.setOnClickListener(v -> guardar());

        if (scopedCourseId > 0) {
            cargarCatalogoDelCursoSeleccionado();
        } else {
            cargarCatalogosGenerales();
        }
    }

    private void configurarEstadoSpinner() {
        List<String> estados = Arrays.asList("APROBADO", "REPROBADO");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, estados);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnEstado.setAdapter(adapter);
    }

    private void setEstadoSpinner(String estado) {
        if (estado == null || spnEstado == null || spnEstado.getAdapter() == null) return;
        for (int i = 0; i < spnEstado.getAdapter().getCount(); i++) {
            if (estado.equalsIgnoreCase(String.valueOf(spnEstado.getAdapter().getItem(i)))) {
                spnEstado.setSelection(i);
                break;
            }
        }
    }

    private void configurarFechaRegistro() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        edtFechaRegistro.setText(sdf.format(new Date()));
        edtFechaRegistro.setOnClickListener(v -> mostrarDatePickerRegistro());
    }

    private void mostrarDatePickerRegistro() {
        Calendar cal = Calendar.getInstance();
        String current = edtFechaRegistro.getText().toString().trim();
        if (!current.isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                Date d = sdf.parse(current);
                if (d != null) cal.setTime(d);
            } catch (Exception ignored) {}
        }
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(this, (view, y, m, d) -> {
            String formatted = String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d);
            edtFechaRegistro.setText(formatted);
        }, year, month, day);
        dialog.show();
    }

    private void configurarCalculoAutomaticoNota() {
        TextWatcher autoCalculoWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isUpdatingNota) return;
                calcularNotaFinal();
            }
        };

        edtZona.addTextChangedListener(autoCalculoWatcher);
        edtExamenFinal.addTextChangedListener(autoCalculoWatcher);

        edtNota.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isUpdatingNota) return;
                try {
                    String totalStr = edtNota.getText().toString().trim();
                    if (!totalStr.isEmpty()) {
                        double total = Double.parseDouble(totalStr);
                        setEstadoSpinner(total >= 61.0 ? "APROBADO" : "REPROBADO");
                    }
                } catch (NumberFormatException ignored) {}
            }
        });
    }

    private void calcularNotaFinal() {
        String zonaStr = edtZona.getText().toString().trim();
        String examenStr = edtExamenFinal.getText().toString().trim();

        if (zonaStr.isEmpty() && examenStr.isEmpty()) {
            return;
        }

        double zona = 0.0;
        double examen = 0.0;
        try {
            if (!zonaStr.isEmpty()) zona = Double.parseDouble(zonaStr);
        } catch (NumberFormatException ignored) {}

        try {
            if (!examenStr.isEmpty()) examen = Double.parseDouble(examenStr);
        } catch (NumberFormatException ignored) {}

        double total = zona + examen;
        isUpdatingNota = true;
        edtNota.setText(String.format(Locale.US, "%.1f", total));
        setEstadoSpinner(total >= 61.0 ? "APROBADO" : "REPROBADO");
        isUpdatingNota = false;
    }

    /** Busca el nombre completo y carnet del estudiante a partir de los datos de la inscripción. */
    private String resolverNombreEstudiante(Inscripcion inscripcion) {
        if (inscripcion == null) return "Sin estudiante";

        // 1. Busca en el catálogo de estudiantes previamente mapeado por estudianteId
        if (inscripcion.getEstudianteId() != null && estudiantesMap.containsKey(inscripcion.getEstudianteId())) {
            Estudiante est = estudiantesMap.get(inscripcion.getEstudianteId());
            if (est != null) {
                String nombreCompleto = est.getNombreCompleto();
                if (nombreCompleto != null && !nombreCompleto.trim().isEmpty()) {
                    String carnet = est.getCarnet();
                    if (carnet != null && !carnet.trim().isEmpty()) {
                        return nombreCompleto + " (" + carnet + ")";
                    }
                    return nombreCompleto;
                }
            }
        }

        // 2. Si la inscripción ya trae el nombre directamente
        if (inscripcion.getNombreEstudiante() != null && !inscripcion.getNombreEstudiante().trim().isEmpty()) {
            String cod = inscripcion.getCodigoEstudiante();
            if (cod != null && !cod.trim().isEmpty()) {
                return inscripcion.getNombreEstudiante().trim() + " (" + cod + ")";
            }
            return inscripcion.getNombreEstudiante().trim();
        }

        // 3. Fallbacks informativos con código o ID
        if (inscripcion.getCodigoEstudiante() != null && !inscripcion.getCodigoEstudiante().trim().isEmpty()) {
            return "Estudiante (" + inscripcion.getCodigoEstudiante() + ")";
        }
        if (inscripcion.getEstudianteId() != null) {
            return "Estudiante #" + inscripcion.getEstudianteId();
        }
        return "Inscripción #" + inscripcion.getId();
    }

    /** Docente: Carga primero el catálogo de estudiantes para resolver nombres y luego las inscripciones del curso. */
    private void cargarCatalogoDelCursoSeleccionado() {
        setLoading(true);
        spnCurso.setEnabled(false);

        String displayCurso = (scopedCourseNombre != null && !scopedCourseNombre.trim().isEmpty())
                ? scopedCourseNombre
                : "Curso #" + scopedCourseId;
        setSpinnerItems(spnCurso, Collections.singletonList(displayCurso));

        if (scopedCourseNombre == null || scopedCourseNombre.trim().isEmpty()) {
            RetrofitClient.getInstance(this).getApiService()
                    .obtenerCurso(scopedCourseId)
                    .enqueue(new Callback<JsonElement>() {
                        @Override
                        public void onResponse(@NonNull Call<JsonElement> call,
                                               @NonNull Response<JsonElement> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    Curso curso = CursoJsonMapper.toCurso(response.body());
                                    String cod = curso.getCodigo() == null ? "" : curso.getCodigo() + " - ";
                                    String nom = curso.getNombre() == null ? "Curso sin nombre" : curso.getNombre();
                                    scopedCourseNombre = cod + nom;
                                    setSpinnerItems(spnCurso, Collections.singletonList(scopedCourseNombre));
                                } catch (Exception ignored) { }
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<JsonElement> call, @NonNull Throwable error) { }
                    });
        }

        // Obtener estudiantes para construir el mapa y luego cargar inscripciones del curso
        RetrofitClient.getInstance(this).getApiService().obtenerEstudiantes()
                .enqueue(new Callback<JsonElement>() {
                    @Override
                    public void onResponse(@NonNull Call<JsonElement> call,
                                           @NonNull Response<JsonElement> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            try {
                                listaEstudiantes = EstudianteJsonMapper.toList(response.body());
                                for (Estudiante est : listaEstudiantes) {
                                    estudiantesMap.put(est.getId(), est);
                                }
                            } catch (Exception ignored) {}
                        }
                        cargarInscripcionesDelCurso(scopedCourseId);
                    }

                    @Override
                    public void onFailure(@NonNull Call<JsonElement> call, @NonNull Throwable error) {
                        cargarInscripcionesDelCurso(scopedCourseId);
                    }
                });
    }

    private void cargarInscripcionesDelCurso(long cursoId) {
        RetrofitClient.getInstance(this).getApiService()
                .obtenerInscripcionesPorCurso(cursoId)
                .enqueue(new Callback<JsonElement>() {
                    @Override
                    public void onResponse(@NonNull Call<JsonElement> call,
                                           @NonNull Response<JsonElement> response) {
                        if (handleUnauthorized(response)) {
                            setLoading(false);
                            return;
                        }
                        if (!response.isSuccessful() || response.body() == null) {
                            setLoading(false);
                            showMessage(ApiErrorHandler.getMessage(response));
                            return;
                        }
                        try {
                            inscripcionesDelCurso =
                                    InscripcionJsonMapper.toList(response.body());
                            List<String> nombresEstudiantes = new ArrayList<>();
                            for (Inscripcion inscripcion : inscripcionesDelCurso) {
                                nombresEstudiantes.add(resolverNombreEstudiante(inscripcion));
                            }

                            if (nombresEstudiantes.isEmpty()) {
                                setSpinnerItems(spnEstudiante, Collections.singletonList("Sin estudiantes inscritos"));
                                spnEstudiante.setEnabled(false);
                                btnGuardar.setEnabled(false);
                                setLoading(false);
                                showMessage("No hay estudiantes inscritos en este curso.");
                                return;
                            }

                            spnEstudiante.setEnabled(true);
                            btnGuardar.setEnabled(true);
                            setSpinnerItems(spnEstudiante, nombresEstudiantes);

                            if (notaId >= 0 && notaCargada == null) {
                                cargarNota();
                            } else if (notaCargada != null) {
                                populate(notaCargada);
                                setLoading(false);
                            } else {
                                setLoading(false);
                            }
                        } catch (IllegalArgumentException error) {
                            setLoading(false);
                            showMessage("El servidor devolvió una lista de inscripciones inválida.");
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<JsonElement> call,
                                          @NonNull Throwable error) {
                        setLoading(false);
                        showMessage(ApiErrorHandler.getNetworkMessage(error));
                    }
                });
    }

    /** Formulario general para Administradores: carga cursos y estudiantes, y al seleccionar curso obtiene inscripciones. */
    private void cargarCatalogosGenerales() {
        setLoading(true);
        RetrofitClient.getInstance(this).getApiService().obtenerEstudiantes()
                .enqueue(new Callback<JsonElement>() {
                    @Override
                    public void onResponse(@NonNull Call<JsonElement> call,
                                           @NonNull Response<JsonElement> response) {
                        if (handleUnauthorized(response)) return;
                        if (response.isSuccessful() && response.body() != null) {
                            try {
                                listaEstudiantes = EstudianteJsonMapper.toList(response.body());
                                for (Estudiante est : listaEstudiantes) {
                                    estudiantesMap.put(est.getId(), est);
                                }
                            } catch (Exception ignored) {}
                        }
                        cargarCursosGenerales();
                    }

                    @Override
                    public void onFailure(@NonNull Call<JsonElement> call, @NonNull Throwable error) {
                        cargarCursosGenerales();
                    }
                });
    }

    private void cargarCursosGenerales() {
        RetrofitClient.getInstance(this).getApiService().obtenerCursos()
                .enqueue(new Callback<JsonElement>() {
                    @Override
                    public void onResponse(@NonNull Call<JsonElement> call,
                                           @NonNull Response<JsonElement> response) {
                        if (handleUnauthorized(response)) {
                            setLoading(false);
                            return;
                        }
                        if (response.isSuccessful() && response.body() != null) {
                            try {
                                listaCursos = CursoJsonMapper.toList(response.body());
                                List<String> nombresCursos = new ArrayList<>();
                                for (Curso curso : listaCursos) {
                                    String cod = curso.getCodigo() == null ? "" : curso.getCodigo() + " - ";
                                    nombresCursos.add(cod + (curso.getNombre() == null ? "Curso sin nombre" : curso.getNombre()));
                                }
                                setSpinnerItems(spnCurso, nombresCursos);

                                spnCurso.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                    @Override
                                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                        if (position >= 0 && position < listaCursos.size()) {
                                            long selectedCid = listaCursos.get(position).getId();
                                            cargarInscripcionesDelCurso(selectedCid);
                                        }
                                    }

                                    @Override
                                    public void onNothingSelected(AdapterView<?> parent) {}
                                });

                                if (notaId >= 0) {
                                    cargarNota();
                                } else if (!listaCursos.isEmpty()) {
                                    cargarInscripcionesDelCurso(listaCursos.get(0).getId());
                                } else {
                                    setLoading(false);
                                }
                            } catch (IllegalArgumentException error) {
                                setLoading(false);
                                showMessage("El servidor devolvió una lista de cursos inválida.");
                            }
                        } else {
                            setLoading(false);
                            showMessage(ApiErrorHandler.getMessage(response));
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<JsonElement> call,
                                          @NonNull Throwable error) {
                        setLoading(false);
                        showMessage(ApiErrorHandler.getNetworkMessage(error));
                    }
                });
    }

    private void cargarNota() {
        RetrofitClient.getInstance(this).getApiService().obtenerNota(notaId)
                .enqueue(new Callback<JsonElement>() {
                    @Override
                    public void onResponse(@NonNull Call<JsonElement> call,
                                           @NonNull Response<JsonElement> response) {
                        setLoading(false);
                        if (handleUnauthorized(response)) return;
                        if (!response.isSuccessful() || response.body() == null) {
                            showMessage(ApiErrorHandler.getMessage(response));
                            return;
                        }
                        try {
                            notaCargada = NotaJsonMapper.toNota(response.body());
                            populate(notaCargada);
                        } catch (IllegalArgumentException error) {
                            showMessage("El servidor devolvió una nota inválida.");
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<JsonElement> call,
                                          @NonNull Throwable error) {
                        setLoading(false);
                        showMessage(ApiErrorHandler.getNetworkMessage(error));
                    }
                });
    }

    private void populate(Nota nota) {
        if (nota == null) return;

        if (nota.getCicloAcademico() != null && !nota.getCicloAcademico().trim().isEmpty()) {
            edtCicloAcademico.setText(nota.getCicloAcademico());
        }
        if (nota.getZona() != null) {
            edtZona.setText(String.format(Locale.US, "%.1f", nota.getZona()));
        }
        if (nota.getExamenFinal() != null) {
            edtExamenFinal.setText(String.format(Locale.US, "%.1f", nota.getExamenFinal()));
        }
        Double finalVal = nota.getNotaFinal() != null ? nota.getNotaFinal() : nota.getNota();
        if (finalVal != null) {
            edtNota.setText(String.format(Locale.US, "%.1f", finalVal));
        }
        if (nota.getEstado() != null && !nota.getEstado().trim().isEmpty()) {
            setEstadoSpinner(nota.getEstado());
        }
        if (nota.getFechaRegistro() != null && !nota.getFechaRegistro().trim().isEmpty()) {
            edtFechaRegistro.setText(nota.getFechaRegistro());
        }
        swActivo.setChecked(Boolean.TRUE.equals(nota.getActivo()));

        if (scopedCourseId > 0) {
            if (nota.getCursoId() != null && nota.getCursoId() != scopedCourseId) {
                btnGuardar.setEnabled(false);
                showMessage("La nota no pertenece al curso seleccionado.");
                return;
            }
        } else if (nota.getCursoId() != null && !listaCursos.isEmpty()) {
            for (int i = 0; i < listaCursos.size(); i++) {
                if (listaCursos.get(i).getId() == nota.getCursoId()) {
                    spnCurso.setSelection(i);
                    break;
                }
            }
        }

        int selectedIndex = -1;
        for (int i = 0; i < inscripcionesDelCurso.size(); i++) {
            Inscripcion ins = inscripcionesDelCurso.get(i);
            if (nota.getInscripcionId() != null && ins.getId() == nota.getInscripcionId()) {
                selectedIndex = i;
                break;
            }
            if (nota.getEstudianteId() != null && ins.getEstudianteId() != null
                    && ins.getEstudianteId().equals(nota.getEstudianteId())) {
                selectedIndex = i;
                break;
            }
        }

        if (selectedIndex >= 0) {
            spnEstudiante.setSelection(selectedIndex);
        } else if (!inscripcionesDelCurso.isEmpty()) {
            showMessage("El estudiante asociado a la nota no se encontró en la lista de inscritos activa.");
        }
    }

    private void guardar() {
        if (inscripcionesDelCurso.isEmpty()) {
            showMessage("No hay estudiantes inscritos disponibles para asignarles nota.");
            return;
        }

        int estudiantePosition = spnEstudiante.getSelectedItemPosition();
        if (estudiantePosition < 0 || estudiantePosition >= inscripcionesDelCurso.size()) {
            showMessage("Seleccione un estudiante inscrito.");
            return;
        }

        Long inscripcionId = inscripcionesDelCurso.get(estudiantePosition).getId();
        if (inscripcionId <= 0) {
            showMessage("La inscripción seleccionada no es válida.");
            return;
        }

        String cicloAcademicoVal = edtCicloAcademico.getText().toString().trim();
        if (cicloAcademicoVal.isEmpty()) {
            cicloAcademicoVal = "Primer Semestre 2026";
        }

        String zonaStr = edtZona.getText().toString().trim();
        if (zonaStr.isEmpty()) {
            showMessage("Ingrese el punteo de zona.");
            return;
        }
        Double zonaVal;
        try {
            zonaVal = Double.parseDouble(zonaStr);
            if (zonaVal < 0.0 || zonaVal > 100.0) {
                showMessage("La zona debe estar entre 0.0 y 100.0.");
                return;
            }
        } catch (NumberFormatException error) {
            showMessage("Zona inválida.");
            return;
        }

        String examenStr = edtExamenFinal.getText().toString().trim();
        if (examenStr.isEmpty()) {
            showMessage("Ingrese el punteo de examen final.");
            return;
        }
        Double examenVal;
        try {
            examenVal = Double.parseDouble(examenStr);
            if (examenVal < 0.0 || examenVal > 100.0) {
                showMessage("El examen final debe estar entre 0.0 y 100.0.");
                return;
            }
        } catch (NumberFormatException error) {
            showMessage("Examen final inválido.");
            return;
        }

        String estadoVal = spnEstado.getSelectedItem() != null
                ? spnEstado.getSelectedItem().toString()
                : ((zonaVal + examenVal >= 61.0) ? "APROBADO" : "REPROBADO");

        String fechaRegistroVal = edtFechaRegistro.getText().toString().trim();
        if (fechaRegistroVal.isEmpty()) {
            fechaRegistroVal = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
        }

        boolean activoVal = swActivo.isChecked();

        NotaRequest request = new NotaRequest(
                cicloAcademicoVal,
                zonaVal,
                examenVal,
                estadoVal,
                activoVal,
                fechaRegistroVal,
                inscripcionId
        );

        setLoading(true);

        Call<JsonElement> call = notaId < 0
                ? RetrofitClient.getInstance(this).getApiService().crearNota(request)
                : RetrofitClient.getInstance(this).getApiService().actualizarNota(notaId, request);

        call.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(@NonNull Call<JsonElement> call,
                                   @NonNull Response<JsonElement> response) {
                setLoading(false);
                if (handleUnauthorized(response)) return;
                if (response.isSuccessful()) {
                    showMessage(notaId < 0 ? "Nota registrada exitosamente." : "Nota actualizada exitosamente.");
                    finish();
                } else {
                    showMessage(ApiErrorHandler.getMessage(response));
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonElement> call,
                                  @NonNull Throwable error) {
                setLoading(false);
                showMessage(ApiErrorHandler.getNetworkMessage(error));
            }
        });
    }

    private void setSpinnerItems(Spinner spinner, List<String> values) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, values);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private boolean handleUnauthorized(Response<?> response) {
        if (response.code() != 401) return false;
        ApiErrorHandler.handleUnauthorized(this);
        return true;
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        if (inscripcionesDelCurso.isEmpty()) {
            btnGuardar.setEnabled(false);
        } else {
            btnGuardar.setEnabled(!loading);
        }
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
