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

import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import gt.com.ro.devumgapp.R;
import gt.com.ro.devumgapp.network.RetrofitClient;
import gt.com.ro.devumgapp.network.model.Carrera;
import gt.com.ro.devumgapp.network.model.Curso;
import gt.com.ro.devumgapp.network.model.CursoRequest;
import gt.com.ro.devumgapp.network.model.Docente;
import gt.com.ro.devumgapp.utils.ApiErrorHandler;
import gt.com.ro.devumgapp.utils.CarreraJsonMapper;
import gt.com.ro.devumgapp.utils.CursoJsonMapper;
import gt.com.ro.devumgapp.utils.DocenteJsonMapper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Usa el mismo formulario para POST y PUT de cursos. */
public class FormCursoActivity extends AppCompatActivity {

    public static final String EXTRA_CURSO_ID = "curso_id";
    private static final String OPCION_SELECCIONAR = "Seleccionar";

    private long cursoId = -1;

    private EditText codigo;
    private EditText nombre;
    private EditText creditos;
    private Spinner spnDocente;
    private Spinner spnCarrera;
    private Button guardar;
    private ProgressBar progressBar;

    private List<Docente> listaDocentes = new ArrayList<>();
    private List<Carrera> listaCarreras = new ArrayList<>();
    private int catalogosCargados = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_curso);
        gt.com.ro.devumgapp.utils.KeyboardAdjustHelper.setupKeyboardAdjustment(this);

        cursoId = getIntent().getLongExtra(EXTRA_CURSO_ID, -1);

        codigo = findViewById(R.id.edtCodigoCurso);
        nombre = findViewById(R.id.edtNombreCurso);
        creditos = findViewById(R.id.edtCreditosCurso);
        spnDocente = findViewById(R.id.spnDocenteCurso);
        spnCarrera = findViewById(R.id.spnCarreraCurso);
        guardar = findViewById(R.id.btnGuardarCurso);
        progressBar = findViewById(R.id.progressFormCurso);

        guardar.setText(
                cursoId < 0
                        ? "CREAR CURSO"
                        : "GUARDAR CAMBIOS"
        );

        guardar.setOnClickListener(v -> guardar());

        cargarCatalogos();
    }

    private void cargarCatalogos() {
        setLoading(true);
        catalogosCargados = 0;
        setSpinnerItems(spnDocente, Collections.singletonList(OPCION_SELECCIONAR));
        setSpinnerItems(spnCarrera, Collections.singletonList(OPCION_SELECCIONAR));
        cargarDocentes();
        cargarCarreras();
    }

    private void cargarDocentes() {
        RetrofitClient.getInstance(this)
                .getApiService()
                .obtenerDocentes()
                .enqueue(new Callback<JsonElement>() {
                    @Override
                    public void onResponse(
                            @NonNull Call<JsonElement> call,
                            @NonNull Response<JsonElement> response
                    ) {
                        if (response.code() == 401) {
                            ApiErrorHandler.handleUnauthorized(FormCursoActivity.this);
                            return;
                        }

                        if (response.isSuccessful() && response.body() != null) {
                            try {
                                listaDocentes = DocenteJsonMapper.toList(response.body());
                                List<String> nombres = new ArrayList<>();
                                nombres.add(OPCION_SELECCIONAR);
                                for (Docente docente : listaDocentes) {
                                    String nom = docente.getNombreCompleto();
                                    nombres.add(nom.isEmpty()
                                            ? "Docente #" + docente.getId()
                                            : nom);
                                }
                                setSpinnerItems(spnDocente, nombres);
                            } catch (IllegalArgumentException error) {
                                showMessage("El servidor devolvió una lista de docentes inválida.");
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

    private void cargarCarreras() {
        RetrofitClient.getInstance(this)
                .getApiService()
                .obtenerCarreras()
                .enqueue(new Callback<JsonElement>() {
                    @Override
                    public void onResponse(
                            @NonNull Call<JsonElement> call,
                            @NonNull Response<JsonElement> response
                    ) {
                        if (response.code() == 401) {
                            ApiErrorHandler.handleUnauthorized(FormCursoActivity.this);
                            return;
                        }

                        if (response.isSuccessful() && response.body() != null) {
                            try {
                                listaCarreras = CarreraJsonMapper.toList(response.body());
                                List<String> nombres = new ArrayList<>();
                                nombres.add(OPCION_SELECCIONAR);
                                for (Carrera carrera : listaCarreras) {
                                    String nombreCarrera = carrera.getNombre();
                                    if (carrera.getCodigo() != null && !carrera.getCodigo().isEmpty()) {
                                        nombres.add(nombreCarrera + " (" + carrera.getCodigo() + ")");
                                    } else {
                                        nombres.add(nombreCarrera);
                                    }
                                }
                                setSpinnerItems(spnCarrera, nombres);
                            } catch (IllegalArgumentException error) {
                                showMessage("El servidor devolvió una lista de carreras inválida.");
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

        if (cursoId >= 0) {
            cargarCurso();
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

    private void cargarCurso() {
        setLoading(true);

        RetrofitClient.getInstance(this)
                .getApiService()
                .obtenerCurso(cursoId)
                .enqueue(new Callback<JsonElement>() {
                    @Override
                    public void onResponse(
                            @NonNull Call<JsonElement> call,
                            @NonNull Response<JsonElement> response
                    ) {
                        setLoading(false);

                        if (response.code() == 401) {
                            ApiErrorHandler.handleUnauthorized(FormCursoActivity.this);
                            return;
                        }

                        if (!response.isSuccessful() || response.body() == null) {
                            showMessage(ApiErrorHandler.getMessage(response));
                            return;
                        }

                        try {
                            populate(CursoJsonMapper.toCurso(response.body()));
                        } catch (IllegalArgumentException error) {
                            showMessage("El servidor devolvió un curso inválido.");
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

    private void populate(Curso curso) {
        codigo.setText(curso.getCodigo());
        nombre.setText(curso.getNombre());

        if (curso.getCreditos() != null) {
            creditos.setText(String.valueOf(curso.getCreditos()));
        }

        if (curso.getDocenteId() != null) {
            for (int i = 0; i < listaDocentes.size(); i++) {
                if (listaDocentes.get(i).getId() != null
                        && listaDocentes.get(i).getId().equals(curso.getDocenteId())) {
                    spnDocente.setSelection(i + 1);
                    break;
                }
            }
        }

        if (curso.getCarreraId() != null) {
            for (int i = 0; i < listaCarreras.size(); i++) {
                if (listaCarreras.get(i).getId() == curso.getCarreraId()) {
                    spnCarrera.setSelection(i + 1);
                    break;
                }
            }
        }
    }

    private void guardar() {
        CursoRequest request = createRequest();
        if (request == null) {
            return;
        }

        setLoading(true);

        Call<JsonElement> call = cursoId < 0
                ? RetrofitClient.getInstance(this).getApiService().crearCurso(request)
                : RetrofitClient.getInstance(this).getApiService().actualizarCurso(cursoId, request);

        call.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(
                    @NonNull Call<JsonElement> call,
                    @NonNull Response<JsonElement> response
            ) {
                setLoading(false);

                if (response.code() == 401) {
                    ApiErrorHandler.handleUnauthorized(FormCursoActivity.this);
                } else if (response.isSuccessful()) {
                    showMessage(
                            cursoId < 0
                                    ? "Curso creado correctamente."
                                    : "Curso actualizado correctamente."
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

    private CursoRequest createRequest() {
        String codigoValue = codigo.getText().toString().trim();
        String nombreValue = nombre.getText().toString().trim();
        String creditosValue = creditos.getText().toString().trim();

        if (codigoValue.isEmpty() || nombreValue.isEmpty() || creditosValue.isEmpty()) {
            showMessage("Complete todos los campos.");
            return null;
        }

        if (listaDocentes.isEmpty()) {
            showMessage("No hay docentes disponibles.");
            return null;
        }

        if (listaCarreras.isEmpty()) {
            showMessage("No hay carreras disponibles.");
            return null;
        }

        int docentePosition = spnDocente.getSelectedItemPosition();
        int carreraPosition = spnCarrera.getSelectedItemPosition();

        if (docentePosition <= 0 || docentePosition > listaDocentes.size()) {
            showMessage("Debe seleccionar un docente.");
            return null;
        }

        if (carreraPosition <= 0 || carreraPosition > listaCarreras.size()) {
            showMessage("Debe seleccionar una carrera.");
            return null;
        }

        Long docenteIdNumber = listaDocentes.get(docentePosition - 1).getId();
        long carreraIdNumber = listaCarreras.get(carreraPosition - 1).getId();

        if (docenteIdNumber == null || docenteIdNumber <= 0 || carreraIdNumber <= 0) {
            showMessage("Seleccione un docente y una carrera válidos.");
            return null;
        }

        int creditosNumber;
        try {
            creditosNumber = Integer.parseInt(creditosValue);
        } catch (NumberFormatException error) {
            showMessage("Los créditos deben ser un número válido.");
            return null;
        }

        if (creditosNumber < 1 || creditosNumber > 20) {
            showMessage("Los créditos deben estar entre 1 y 20.");
            return null;
        }

        return new CursoRequest(
                codigoValue,
                nombreValue,
                creditosNumber,
                docenteIdNumber,
                carreraIdNumber
        );
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(
                loading ? View.VISIBLE : View.GONE
        );
        guardar.setEnabled(!loading);
    }

    private void showMessage(String message) {
        Toast.makeText(
                this,
                message,
                Toast.LENGTH_LONG
        ).show();
    }
}
