package gt.com.ro.devumgapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonElement;

import java.util.List;

import gt.com.ro.devumgapp.R;
import gt.com.ro.devumgapp.adapters.CursoAdapter;
import gt.com.ro.devumgapp.adapters.InscripcionAdapter;
import gt.com.ro.devumgapp.network.RetrofitClient;
import gt.com.ro.devumgapp.network.model.Curso;
import gt.com.ro.devumgapp.network.model.Inscripcion;
import gt.com.ro.devumgapp.utils.ApiErrorHandler;
import gt.com.ro.devumgapp.utils.CursoJsonMapper;
import gt.com.ro.devumgapp.utils.InscripcionJsonMapper;
import gt.com.ro.devumgapp.utils.TokenManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Muestra cursos según el rol autenticado, sin identificadores fijos. */
public class CursosActivity extends AppCompatActivity {

    private CursoAdapter cursoAdapter;
    private InscripcionAdapter inscripcionAdapter;
    private ProgressBar progressBar;
    private TextView emptyView;
    private boolean studentMode;
    private boolean teacherMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cursos);

        progressBar = findViewById(R.id.progressCursos);
        emptyView = findViewById(R.id.txtCursosVacios);

        String role = TokenManager.getInstance(this).getRole();
        String normalizedRole = role == null ? "" : role.trim().toUpperCase();
        studentMode = normalizedRole.contains("ESTUDIANTE")
                || normalizedRole.contains("ALUMNO");
        teacherMode = normalizedRole.contains("DOCENTE");

        RecyclerView recycler = findViewById(R.id.recyclerCursos);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        if (studentMode) {
            inscripcionAdapter = new InscripcionAdapter(inscripcion -> { });
            recycler.setAdapter(inscripcionAdapter);
            emptyView.setText("Aún no tienes cursos asignados.");
        } else {
            // El docente consulta; la edición de cursos sigue siendo exclusiva del módulo admin.
            cursoAdapter = new CursoAdapter(teacherMode ? null : this::openDetail);
            recycler.setAdapter(cursoAdapter);
            if (teacherMode) {
                emptyView.setText("No tienes cursos asignados.");
            }
        }

        Button add = findViewById(R.id.btnAgregarCurso);
        add.setVisibility(studentMode || teacherMode ? View.GONE : View.VISIBLE);
        add.setOnClickListener(v ->
                startActivity(new Intent(this, FormCursoActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarCursos();
    }

    private void cargarCursos() {
        if (studentMode) {
            cargarCursosDelEstudiante();
        } else if (teacherMode) {
            cargarCursosDelDocente();
        } else {
            cargarTodosLosCursos();
        }
    }

    private void cargarTodosLosCursos() {
        setLoading(true);
        RetrofitClient.getInstance(this).getApiService().obtenerCursos()
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
                            List<Curso> cursos = CursoJsonMapper.toList(response.body());
                            cursoAdapter.submitList(cursos);
                            emptyView.setVisibility(cursos.isEmpty() ? View.VISIBLE : View.GONE);
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
                            List<Curso> cursos = CursoJsonMapper.toList(response.body());
                            cursoAdapter.submitList(cursos);
                            emptyView.setVisibility(cursos.isEmpty() ? View.VISIBLE : View.GONE);
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

    private void cargarCursosDelEstudiante() {
        setLoading(true);
        RetrofitClient.getInstance(this).getApiService().obtenerMisInscripciones()
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
                            List<Inscripcion> inscripciones =
                                    InscripcionJsonMapper.toList(response.body());
                            inscripcionAdapter.submitList(inscripciones);
                            emptyView.setVisibility(inscripciones.isEmpty() ? View.VISIBLE : View.GONE);
                        } catch (IllegalArgumentException error) {
                            showMessage("El servidor devolvió una lista de inscripciones inválida.");
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonElement> call, Throwable error) {
                        setLoading(false);
                        showMessage(ApiErrorHandler.getNetworkMessage(error));
                    }
                });
    }

    private boolean handleUnauthorized(Response<?> response) {
        if (response.code() != 401) return false;
        ApiErrorHandler.handleUnauthorized(this);
        return true;
    }

    private void openDetail(Curso curso) {
        Intent intent = new Intent(this, DetalleCursoActivity.class);
        intent.putExtra(DetalleCursoActivity.EXTRA_CURSO_ID, curso.getId());
        startActivity(intent);
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
