package gt.com.ro.devumgapp.activities;


import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.JsonElement;

import gt.com.ro.devumgapp.R;
import gt.com.ro.devumgapp.network.RetrofitClient;
import gt.com.ro.devumgapp.network.model.Curso;
import gt.com.ro.devumgapp.network.model.CursoRequest;
import gt.com.ro.devumgapp.utils.ApiErrorHandler;
import gt.com.ro.devumgapp.utils.CursoJsonMapper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Usa el mismo formulario para POST y PUT de cursos. */
public class FormCursoActivity extends AppCompatActivity {

    public static final String EXTRA_CURSO_ID = "curso_id";

    private long cursoId = -1;

    private EditText codigo;
    private EditText nombre;
    private EditText creditos;
    private EditText docenteId;
    private EditText carreraId;
    private Button guardar;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_curso);

        cursoId = getIntent().getLongExtra(EXTRA_CURSO_ID, -1);

        codigo = findViewById(R.id.edtCodigoCurso);
        nombre = findViewById(R.id.edtNombreCurso);
        creditos = findViewById(R.id.edtCreditosCurso);
        docenteId = findViewById(R.id.edtDocenteIdCurso);
        carreraId = findViewById(R.id.edtCarreraIdCurso);
        guardar = findViewById(R.id.btnGuardarCurso);
        progressBar = findViewById(R.id.progressFormCurso);

        guardar.setText(
                cursoId < 0
                        ? "CREAR CURSO"
                        : "GUARDAR CAMBIOS"
        );

        guardar.setOnClickListener(v -> guardar());

        if (cursoId >= 0) {
            cargarCurso();
        }
    }

    private void cargarCurso() {

        setLoading(true);

        RetrofitClient.getInstance(this)
                .getApiService()
                .obtenerCurso(cursoId)
                .enqueue(new Callback<JsonElement>() {

                    @Override
                    public void onResponse(
                            Call<JsonElement> call,
                            Response<JsonElement> response
                    ) {

                        setLoading(false);

                        if (response.code() == 401) {
                            ApiErrorHandler.handleUnauthorized(
                                    FormCursoActivity.this
                            );
                            return;
                        }

                        if (!response.isSuccessful()
                                || response.body() == null) {

                            showMessage(
                                    ApiErrorHandler.getMessage(response)
                            );
                            return;
                        }

                        try {

                            populate(
                                    CursoJsonMapper.toCurso(
                                            response.body()
                                    )
                            );

                        } catch (IllegalArgumentException error) {

                            showMessage(
                                    "El servidor devolvió un curso inválido."
                            );
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<JsonElement> call,
                            Throwable error
                    ) {

                        setLoading(false);

                        showMessage(
                                ApiErrorHandler.getNetworkMessage(error)
                        );
                    }
                });
    }

    private void populate(Curso curso) {

        codigo.setText(curso.getCodigo());

        nombre.setText(curso.getNombre());

        if (curso.getCreditos() != null) {
            creditos.setText(
                    String.valueOf(curso.getCreditos())
            );
        }

        if (curso.getDocenteId() != null) {
            docenteId.setText(
                    String.valueOf(curso.getDocenteId())
            );
        }

        if (curso.getCarreraId() != null) {
            carreraId.setText(
                    String.valueOf(curso.getCarreraId())
            );
        }
    }

    private void guardar() {

        CursoRequest request = createRequest();

        if (request == null) {
            return;
        }

        setLoading(true);

        Call<JsonElement> call = cursoId < 0
                ? RetrofitClient.getInstance(this)
                .getApiService()
                .crearCurso(request)
                : RetrofitClient.getInstance(this)
                .getApiService()
                .actualizarCurso(cursoId, request);

        call.enqueue(new Callback<JsonElement>() {

            @Override
            public void onResponse(
                    Call<JsonElement> call,
                    Response<JsonElement> response
            ) {

                setLoading(false);

                if (response.code() == 401) {

                    ApiErrorHandler.handleUnauthorized(
                            FormCursoActivity.this
                    );

                } else if (response.isSuccessful()) {

                    showMessage(
                            cursoId < 0
                                    ? "Curso creado correctamente."
                                    : "Curso actualizado correctamente."
                    );

                    finish();

                } else {

                    showMessage(
                            ApiErrorHandler.getMessage(response)
                    );
                }
            }

            @Override
            public void onFailure(
                    Call<JsonElement> call,
                    Throwable error
            ) {

                setLoading(false);

                showMessage(
                        ApiErrorHandler.getNetworkMessage(error)
                );
            }
        });
    }

    private CursoRequest createRequest() {

        String codigoValue =
                codigo.getText().toString().trim();

        String nombreValue =
                nombre.getText().toString().trim();

        String creditosValue =
                creditos.getText().toString().trim();

        String docenteIdValue =
                docenteId.getText().toString().trim();

        String carreraIdValue =
                carreraId.getText().toString().trim();

        if (codigoValue.isEmpty()
                || nombreValue.isEmpty()
                || creditosValue.isEmpty()
                || docenteIdValue.isEmpty()
                || carreraIdValue.isEmpty()) {

            showMessage("Complete todos los campos.");

            return null;
        }

        int creditosNumber;

        long docenteIdNumber;
        long carreraIdNumber;

        try {

            creditosNumber =
                    Integer.parseInt(creditosValue);

            docenteIdNumber =
                    Long.parseLong(docenteIdValue);

            carreraIdNumber =
                    Long.parseLong(carreraIdValue);

        } catch (NumberFormatException error) {

            showMessage(
                    "Los créditos y los IDs deben ser números válidos."
            );

            return null;
        }

        if (creditosNumber < 1 || creditosNumber > 20) {

            showMessage(
                    "Los créditos deben estar entre 1 y 20."
            );

            return null;
        }

        if (docenteIdNumber <= 0 || carreraIdNumber <= 0) {

            showMessage(
                    "Los IDs de docente y carrera deben ser mayores que 0."
            );

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
                loading
                        ? View.VISIBLE
                        : View.GONE
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

