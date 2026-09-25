package gt.com.ro.devumgapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.List;

import gt.com.ro.devumgapp.R;
import gt.com.ro.devumgapp.adapters.NotaAdapter;
import gt.com.ro.devumgapp.network.RetrofitClient;
import gt.com.ro.devumgapp.network.model.Curso;
import gt.com.ro.devumgapp.network.model.Inscripcion;
import gt.com.ro.devumgapp.network.model.Nota;
import gt.com.ro.devumgapp.utils.ApiErrorHandler;
import gt.com.ro.devumgapp.utils.CursoJsonMapper;
import gt.com.ro.devumgapp.utils.InscripcionJsonMapper;
import gt.com.ro.devumgapp.utils.NotaJsonMapper;
import gt.com.ro.devumgapp.utils.TokenManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Gestión de notas. El docente trabaja únicamente con sus cursos autenticados. */
public class NotasActivity extends AppCompatActivity {

    private NotaAdapter adapter;
    private ProgressBar progressBar;
    private TextView emptyView;
    private TextView selectCourseLabel;
    private Spinner courseSpinner;
    private Button addButton;
    private boolean studentMode;
    private boolean teacherMode;
    private boolean bindingCourseSelector;
    private long selectedCourseId = -1;
    private int enrolledStudents;
    private List<Curso> teacherCourses = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notas);

        progressBar = findViewById(R.id.progressNotas);
        emptyView = findViewById(R.id.txtNotasVacias);
        selectCourseLabel = findViewById(R.id.txtSeleccionarCursoNota);
        courseSpinner = findViewById(R.id.spnCursoDocenteNota);
        addButton = findViewById(R.id.btnAgregarNota);

        String role = TokenManager.getInstance(this).getRole();
        String normalizedRole = role == null ? "" : role.trim().toUpperCase();
        studentMode = normalizedRole.contains("ESTUDIANTE")
                || normalizedRole.contains("ALUMNO");
        teacherMode = normalizedRole.contains("DOCENTE");

        RecyclerView recycler = findViewById(R.id.recyclerNotas);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        if (studentMode) {
            adapter = new NotaAdapter(null, true);
            emptyView.setText("Aún no tienes notas registradas.");
        } else if (teacherMode) {
            adapter = new NotaAdapter(this::editarNotaDelCurso);
            selectCourseLabel.setVisibility(View.VISIBLE);
            courseSpinner.setVisibility(View.VISIBLE);
        } else {
            adapter = new NotaAdapter(this::openDetail);
        }
        recycler.setAdapter(adapter);

        addButton.setVisibility(studentMode ? View.GONE : View.VISIBLE);
        addButton.setEnabled(!teacherMode);
        addButton.setOnClickListener(v -> abrirFormularioNuevaNota());

        courseSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!bindingCourseSelector && teacherMode && position >= 0
                        && position < teacherCourses.size()) {
                    seleccionarCurso(position);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                if (teacherMode) {
                    selectedCourseId = -1;
                    addButton.setEnabled(false);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (teacherMode) {
            cargarCursosDelDocente();
        } else {
            cargarNotasGenerales();
        }
    }

    private void cargarNotasGenerales() {
        setLoading(true);
        Call<JsonElement> call = studentMode
                ? RetrofitClient.getInstance(this).getApiService().obtenerMisNotas()
                : RetrofitClient.getInstance(this).getApiService().obtenerNotas();

        call.enqueue(new Callback<JsonElement>() {
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
                    List<Nota> notas = NotaJsonMapper.toList(response.body());
                    adapter.submitList(notas);
                    emptyView.setVisibility(notas.isEmpty() ? View.VISIBLE : View.GONE);
                } catch (IllegalArgumentException error) {
                    showMessage("El servidor devolvió una lista de notas inválida.");
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

    /** Consulta directamente los cursos asociados al docente autenticado. */
    private void cargarCursosDelDocente() {
        setLoading(true);
        RetrofitClient.getInstance(this).getApiService().obtenerCursosDelDocenteActual()
                .enqueue(new Callback<JsonElement>() {
                    @Override
                    public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                        setLoading(false);
                        if (handleUnauthorized(response)) return;
                        if (!response.isSuccessful() || response.body() == null) {
                            showMessage(ApiErrorHandler.getMessage(response));
                            return;
                        }
                        try {
                            mostrarCursosDelDocente(CursoJsonMapper.toList(response.body()));
                        } catch (IllegalArgumentException error) {
                            showMessage("El servidor devolvió una lista de cursos inválida.");
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonElement> call, Throwable error) {
                        setLoading(false);
                        showMessage(ApiErrorHandler.getNetworkMessage(error));
                    }
                });
    }

    private void mostrarCursosDelDocente(List<Curso> cursos) {
        teacherCourses = cursos;
        bindingCourseSelector = true;

        List<String> labels = new ArrayList<>();
        for (Curso curso : teacherCourses) {
            String codigo = curso.getCodigo() == null ? "" : curso.getCodigo() + " - ";
            labels.add(codigo + (curso.getNombre() == null ? "Curso sin nombre" : curso.getNombre()));
        }
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, labels);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        courseSpinner.setAdapter(spinnerAdapter);

        if (teacherCourses.isEmpty()) {
            selectedCourseId = -1;
            enrolledStudents = 0;
            adapter.submitList(new ArrayList<>());
            emptyView.setText("No tienes cursos asignados.");
            emptyView.setVisibility(View.VISIBLE);
            addButton.setEnabled(false);
            bindingCourseSelector = false;
            return;
        }

        int selectedPosition = 0;
        for (int i = 0; i < teacherCourses.size(); i++) {
            if (teacherCourses.get(i).getId() == selectedCourseId) {
                selectedPosition = i;
                break;
            }
        }
        courseSpinner.setSelection(selectedPosition, false);
        bindingCourseSelector = false;
        seleccionarCurso(selectedPosition);
    }

    private void seleccionarCurso(int position) {
        Curso curso = teacherCourses.get(position);
        selectedCourseId = curso.getId();
        enrolledStudents = 0;
        adapter.submitList(new ArrayList<>());
        emptyView.setVisibility(View.GONE);
        addButton.setEnabled(true);
        cargarInscripcionesYNotas(selectedCourseId);
    }

    /** Obtiene primero los alumnos inscritos reales y luego las notas del curso. */
    private void cargarInscripcionesYNotas(long cursoId) {
        setLoading(true);
        RetrofitClient.getInstance(this).getApiService().obtenerInscripcionesPorCurso(cursoId)
                .enqueue(new Callback<JsonElement>() {
                    @Override
                    public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                        if (cursoId != selectedCourseId) return;
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
                            List<Inscripcion> inscripciones =
                                    InscripcionJsonMapper.toList(response.body());
                            enrolledStudents = inscripciones.size();
                            cargarNotasDelCurso(cursoId);
                        } catch (IllegalArgumentException error) {
                            setLoading(false);
                            showMessage("El servidor devolvió una lista de inscripciones inválida.");
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonElement> call, Throwable error) {
                        if (cursoId != selectedCourseId) return;
                        setLoading(false);
                        showMessage(ApiErrorHandler.getNetworkMessage(error));
                    }
                });
    }

    private void cargarNotasDelCurso(long cursoId) {
        RetrofitClient.getInstance(this)
                .getApiService()
                .obtenerNotasPorCurso(cursoId)
                .enqueue(new Callback<JsonElement>() {
                    @Override
                    public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                        if (cursoId != selectedCourseId) return;

                        setLoading(false);

                        if (handleUnauthorized(response)) return;

                        if (!response.isSuccessful() || response.body() == null) {
                            showMessage(ApiErrorHandler.getMessage(response));
                            return;
                        }

                        try {
                            // El backend ya devuelve únicamente las notas del curso.
                            List<Nota> notasDelCurso =
                                    NotaJsonMapper.toList(response.body());

                            adapter.submitList(notasDelCurso);

                            if (notasDelCurso.isEmpty()) {
                                emptyView.setText(
                                        "No hay notas para este curso. Estudiantes inscritos: "
                                                + enrolledStudents + "."
                                );
                                emptyView.setVisibility(View.VISIBLE);
                            } else {
                                emptyView.setVisibility(View.GONE);
                            }

                        } catch (IllegalArgumentException error) {
                            showMessage("El servidor devolvió una lista de notas inválida.");
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonElement> call, Throwable error) {
                        if (cursoId != selectedCourseId) return;

                        setLoading(false);
                        showMessage(ApiErrorHandler.getNetworkMessage(error));
                    }
                });
    }
    private void abrirFormularioNuevaNota() {
        Intent intent = new Intent(this, FormNotaActivity.class);
        if (teacherMode) {
            if (selectedCourseId <= 0) return;
            intent.putExtra(FormNotaActivity.EXTRA_CURSO_ID, selectedCourseId);
        }
        startActivity(intent);
    }

    private void editarNotaDelCurso(Nota nota) {
        if (nota.getCursoId() == null || nota.getCursoId() != selectedCourseId) return;
        Intent intent = new Intent(this, FormNotaActivity.class);
        intent.putExtra(FormNotaActivity.EXTRA_NOTA_ID, nota.getId());
        intent.putExtra(FormNotaActivity.EXTRA_CURSO_ID, selectedCourseId);
        startActivity(intent);
    }

    private void openDetail(Nota nota) {
        Intent intent = new Intent(this, DetalleNotaActivity.class);
        intent.putExtra(DetalleNotaActivity.EXTRA_NOTA_ID, nota.getId());
        startActivity(intent);
    }

    private boolean handleUnauthorized(Response<?> response) {
        if (response.code() != 401) return false;
        ApiErrorHandler.handleUnauthorized(this);
        return true;
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
