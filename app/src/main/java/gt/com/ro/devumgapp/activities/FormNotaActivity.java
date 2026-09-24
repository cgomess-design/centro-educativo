package gt.com.ro.devumgapp.activities;

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
import androidx.appcompat.widget.SwitchCompat;

import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
import gt.com.ro.devumgapp.utils.NotaJsonMapper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Formulario reutilizado para crear y actualizar notas. */
public class FormNotaActivity extends AppCompatActivity {

    public static final String EXTRA_NOTA_ID = "nota_id";
    public static final String EXTRA_CURSO_ID = "curso_id";

    private long notaId = -1;
    private long scopedCourseId = -1;
    private Spinner spnEstudiante;
    private Spinner spnCurso;
    private EditText edtNota;
    private SwitchCompat swActivo;
    private Button btnGuardar;
    private ProgressBar progressBar;

    private List<Estudiante> listaEstudiantes = new ArrayList<>();
    private List<Curso> listaCursos = new ArrayList<>();
    private List<Inscripcion> inscripcionesDelCurso = new ArrayList<>();
    private int catalogosCargados;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_nota);

        notaId = getIntent().getLongExtra(EXTRA_NOTA_ID, -1);
        scopedCourseId = getIntent().getLongExtra(EXTRA_CURSO_ID, -1);

        spnEstudiante = findViewById(R.id.spnEstudianteNota);
        spnCurso = findViewById(R.id.spnCursoNota);
        edtNota = findViewById(R.id.edtValorNota);
        swActivo = findViewById(R.id.swActivoNota);
        btnGuardar = findViewById(R.id.btnGuardarNota);
        progressBar = findViewById(R.id.progressFormNota);

        btnGuardar.setText(notaId < 0 ? "REGISTRAR NOTA" : "GUARDAR CAMBIOS");
        btnGuardar.setOnClickListener(v -> guardar());

        if (scopedCourseId > 0) {
            cargarCatalogoDelCursoSeleccionado();
        } else {
            cargarCatalogosGenerales();
        }
    }

    /** Conserva el comportamiento de administración para el formulario sin curso preseleccionado. */
    private void cargarCatalogosGenerales() {
        setLoading(true);
        cargarEstudiantes();
        cargarCursos();
    }

    /** Para el docente, el curso llega de la selección previa y solo se cargan sus inscripciones. */
    private void cargarCatalogoDelCursoSeleccionado() {
        setLoading(true);
        spnCurso.setEnabled(false);
        setSpinnerItems(spnCurso, Collections.singletonList("Curso seleccionado"));

        RetrofitClient.getInstance(this).getApiService()
                .obtenerInscripcionesPorCurso(scopedCourseId)
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
                            List<String> estudiantes = new ArrayList<>();
                            for (Inscripcion inscripcion : inscripcionesDelCurso) {
                                String nombre = inscripcion.getNombreEstudiante();
                                estudiantes.add(nombre == null || nombre.trim().isEmpty()
                                        ? "Estudiante ID: " + inscripcion.getEstudianteId()
                                        : nombre);
                            }
                            setSpinnerItems(spnEstudiante, estudiantes);
                            if (notaId >= 0) {
                                cargarNota();
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

    private void cargarEstudiantes() {
        RetrofitClient.getInstance(this).getApiService().obtenerEstudiantes()
                .enqueue(new Callback<JsonElement>() {
                    @Override
                    public void onResponse(@NonNull Call<JsonElement> call,
                                           @NonNull Response<JsonElement> response) {
                        if (handleUnauthorized(response)) {
                            return;
                        }
                        if (response.isSuccessful() && response.body() != null) {
                            try {
                                listaEstudiantes = EstudianteJsonMapper.toList(response.body());
                                List<String> nombres = new ArrayList<>();
                                for (Estudiante estudiante : listaEstudiantes) {
                                    nombres.add(estudiante.getNombreCompleto());
                                }
                                setSpinnerItems(spnEstudiante, nombres);
                            } catch (IllegalArgumentException error) {
                                showMessage("El servidor devolvió una lista de estudiantes inválida.");
                            }
                        } else {
                            showMessage(ApiErrorHandler.getMessage(response));
                        }
                        catalogoGeneralCompletado();
                    }

                    @Override
                    public void onFailure(@NonNull Call<JsonElement> call,
                                          @NonNull Throwable error) {
                        showMessage(ApiErrorHandler.getNetworkMessage(error));
                        catalogoGeneralCompletado();
                    }
                });
    }

    private void cargarCursos() {
        RetrofitClient.getInstance(this).getApiService().obtenerCursos()
                .enqueue(new Callback<JsonElement>() {
                    @Override
                    public void onResponse(@NonNull Call<JsonElement> call,
                                           @NonNull Response<JsonElement> response) {
                        if (handleUnauthorized(response)) {
                            return;
                        }
                        if (response.isSuccessful() && response.body() != null) {
                            try {
                                listaCursos = CursoJsonMapper.toList(response.body());
                                List<String> nombres = new ArrayList<>();
                                for (Curso curso : listaCursos) {
                                    nombres.add(curso.getNombre());
                                }
                                setSpinnerItems(spnCurso, nombres);
                            } catch (IllegalArgumentException error) {
                                showMessage("El servidor devolvió una lista de cursos inválida.");
                            }
                        } else {
                            showMessage(ApiErrorHandler.getMessage(response));
                        }
                        catalogoGeneralCompletado();
                    }

                    @Override
                    public void onFailure(@NonNull Call<JsonElement> call,
                                          @NonNull Throwable error) {
                        showMessage(ApiErrorHandler.getNetworkMessage(error));
                        catalogoGeneralCompletado();
                    }
                });
    }

    private void catalogoGeneralCompletado() {
        catalogosCargados++;
        if (catalogosCargados < 2) return;
        if (notaId >= 0) {
            cargarNota();
        } else {
            setLoading(false);
        }
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
                            populate(NotaJsonMapper.toNota(response.body()));
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
        edtNota.setText(nota.getNota() == null ? "" : String.valueOf(nota.getNota()));
        swActivo.setChecked(Boolean.TRUE.equals(nota.getActivo()));

        if (scopedCourseId > 0) {
            if (nota.getCursoId() == null || nota.getCursoId() != scopedCourseId) {
                btnGuardar.setEnabled(false);
                showMessage("La nota no pertenece al curso seleccionado.");
                return;
            }
            for (int i = 0; i < inscripcionesDelCurso.size(); i++) {
                if (inscripcionesDelCurso.get(i).getEstudianteId() != null
                        && inscripcionesDelCurso.get(i).getEstudianteId()
                        .equals(nota.getEstudianteId())) {
                    spnEstudiante.setSelection(i);
                    return;
                }
            }
            btnGuardar.setEnabled(false);
            showMessage("El estudiante de la nota ya no está inscrito en este curso.");
            return;
        }

        for (int i = 0; i < listaEstudiantes.size(); i++) {
            if (listaEstudiantes.get(i).getId() == nota.getEstudianteId()) {
                spnEstudiante.setSelection(i);
                break;
            }
        }
        for (int i = 0; i < listaCursos.size(); i++) {
            if (listaCursos.get(i).getId() == nota.getCursoId()) {
                spnCurso.setSelection(i);
                break;
            }
        }
    }

    private void guardar() {
        String notaStr = edtNota.getText().toString().trim();
        if (notaStr.isEmpty()) {
            showMessage("Complete todos los campos.");
            return;
        }

        Double valorNota;
        try {
            valorNota = Double.parseDouble(notaStr);
        } catch (NumberFormatException error) {
            showMessage("Nota inválida.");
            return;
        }

        int estudiantePosition = spnEstudiante.getSelectedItemPosition();
        Long estudianteId;
        Long cursoId;
        if (scopedCourseId > 0) {
            if (estudiantePosition < 0 || estudiantePosition >= inscripcionesDelCurso.size()) {
                showMessage("Seleccione un estudiante inscrito en el curso.");
                return;
            }
            estudianteId = inscripcionesDelCurso.get(estudiantePosition).getEstudianteId();
            cursoId = scopedCourseId;
        } else {
            int cursoPosition = spnCurso.getSelectedItemPosition();
            if (estudiantePosition < 0 || estudiantePosition >= listaEstudiantes.size()
                    || cursoPosition < 0 || cursoPosition >= listaCursos.size()) {
                showMessage("Complete todos los campos.");
                return;
            }
            estudianteId = listaEstudiantes.get(estudiantePosition).getId();
            cursoId = listaCursos.get(cursoPosition).getId();
        }

        if (estudianteId == null || cursoId == null) {
            showMessage("Complete todos los campos.");
            return;
        }

        // Zona, examen y nota final son reglas del backend; el cliente no las recalcula.
        NotaRequest request = new NotaRequest(valorNota, estudianteId, cursoId,
                swActivo.isChecked());
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
                    showMessage(notaId < 0 ? "Nota registrada." : "Nota actualizada.");
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
        btnGuardar.setEnabled(!loading);
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
