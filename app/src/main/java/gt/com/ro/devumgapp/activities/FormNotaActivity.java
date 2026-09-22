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
import java.util.List;

import gt.com.ro.devumgapp.R;
import gt.com.ro.devumgapp.network.RetrofitClient;
import gt.com.ro.devumgapp.network.model.Curso;
import gt.com.ro.devumgapp.network.model.Estudiante;
import gt.com.ro.devumgapp.network.model.Nota;
import gt.com.ro.devumgapp.network.model.NotaRequest;
import gt.com.ro.devumgapp.utils.ApiErrorHandler;
import gt.com.ro.devumgapp.utils.CursoJsonMapper;
import gt.com.ro.devumgapp.utils.EstudianteJsonMapper;
import gt.com.ro.devumgapp.utils.NotaJsonMapper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FormNotaActivity extends AppCompatActivity {

    public static final String EXTRA_NOTA_ID = "nota_id";

    private long notaId = -1;
    private Spinner spnEstudiante, spnCurso;
    private EditText edtNota;
    private SwitchCompat swActivo;
    private Button btnGuardar;
    private ProgressBar progressBar;

    private List<Estudiante> listaEstudiantes = new ArrayList<>();
    private List<Curso> listaCursos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_nota);

        notaId = getIntent().getLongExtra(EXTRA_NOTA_ID, -1);

        spnEstudiante = findViewById(R.id.spnEstudianteNota);
        spnCurso = findViewById(R.id.spnCursoNota);
        edtNota = findViewById(R.id.edtValorNota);
        swActivo = findViewById(R.id.swActivoNota);
        btnGuardar = findViewById(R.id.btnGuardarNota);
        progressBar = findViewById(R.id.progressFormNota);

        btnGuardar.setText(notaId < 0 ? "REGISTRAR NOTA" : "GUARDAR CAMBIOS");
        btnGuardar.setOnClickListener(v -> guardar());

        cargarCatalogos();
    }

    private void cargarCatalogos() {
        setLoading(true);
        // Cargar Estudiantes
        RetrofitClient.getInstance(this).getApiService().obtenerEstudiantes().enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(@NonNull Call<JsonElement> call, @NonNull Response<JsonElement> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaEstudiantes = EstudianteJsonMapper.toList(response.body());
                    List<String> nombres = new ArrayList<>();
                    for (Estudiante e : listaEstudiantes) nombres.add(e.getNombre());
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(FormNotaActivity.this, android.R.layout.simple_spinner_item, nombres);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spnEstudiante.setAdapter(adapter);
                }
                checkCatalogosLoaded();
            }

            @Override
            public void onFailure(@NonNull Call<JsonElement> call, @NonNull Throwable t) {
                checkCatalogosLoaded();
            }
        });

        // Cargar Cursos
        RetrofitClient.getInstance(this).getApiService().obtenerCursos().enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(@NonNull Call<JsonElement> call, @NonNull Response<JsonElement> response) {
                if (response.isSuccessful() && response.body() != null) {
                    listaCursos = CursoJsonMapper.toList(response.body());
                    List<String> nombres = new ArrayList<>();
                    for (Curso c : listaCursos) nombres.add(c.getNombre());
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(FormNotaActivity.this, android.R.layout.simple_spinner_item, nombres);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spnCurso.setAdapter(adapter);
                }
                checkCatalogosLoaded();
            }

            @Override
            public void onFailure(@NonNull Call<JsonElement> call, @NonNull Throwable t) {
                checkCatalogosLoaded();
            }
        });
    }

    private int loadedCount = 0;
    private void checkCatalogosLoaded() {
        loadedCount++;
        if (loadedCount >= 2) {
            if (notaId >= 0) {
                cargarNota();
            } else {
                setLoading(false);
            }
        }
    }

    private void cargarNota() {
        RetrofitClient.getInstance(this).getApiService().obtenerNota(notaId).enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(@NonNull Call<JsonElement> call, @NonNull Response<JsonElement> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    populate(NotaJsonMapper.toNota(response.body()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonElement> call, @NonNull Throwable t) {
                setLoading(false);
            }
        });
    }

    private void populate(Nota nota) {
        edtNota.setText(String.valueOf(nota.getNota()));
        swActivo.setChecked(Boolean.TRUE.equals(nota.getActivo()));
        
        // Seleccionar en spinners
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
        if (notaStr.isEmpty() || spnEstudiante.getSelectedItem() == null || spnCurso.getSelectedItem() == null) {
            showMessage("Complete todos los campos.");
            return;
        }

        Double valorNota;
        try {
            valorNota = Double.parseDouble(notaStr);
        } catch (NumberFormatException e) {
            showMessage("Nota inválida.");
            return;
        }

        Long estId = listaEstudiantes.get(spnEstudiante.getSelectedItemPosition()).getId();
        Long cursId = listaCursos.get(spnCurso.getSelectedItemPosition()).getId();
        boolean active = swActivo.isChecked();

        NotaRequest request = new NotaRequest(valorNota, estId, cursId, active);
        setLoading(true);

        Call<JsonElement> call = notaId < 0
                ? RetrofitClient.getInstance(this).getApiService().crearNota(request)
                : RetrofitClient.getInstance(this).getApiService().actualizarNota(notaId, request);

        call.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(@NonNull Call<JsonElement> call, @NonNull Response<JsonElement> response) {
                setLoading(false);
                if (response.isSuccessful()) {
                    showMessage(notaId < 0 ? "Nota registrada." : "Nota actualizada.");
                    finish();
                } else {
                    showMessage(ApiErrorHandler.getMessage(response));
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonElement> call, @NonNull Throwable t) {
                setLoading(false);
                showMessage(ApiErrorHandler.getNetworkMessage(t));
            }
        });
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnGuardar.setEnabled(!loading);
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
