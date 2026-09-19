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
import gt.com.ro.devumgapp.network.RetrofitClient;
import gt.com.ro.devumgapp.network.model.Curso;
import gt.com.ro.devumgapp.utils.ApiErrorHandler;
import gt.com.ro.devumgapp.utils.CursoJsonMapper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CursosActivity extends AppCompatActivity {

    private CursoAdapter adapter;
    private ProgressBar progressBar;
    private TextView emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cursos);

        progressBar = findViewById(R.id.progressCursos);
        emptyView = findViewById(R.id.txtCursosVacios);

        RecyclerView recycler = findViewById(R.id.recyclerCursos);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        adapter = new CursoAdapter(this::openDetail);
        recycler.setAdapter(adapter);

        Button add = findViewById(R.id.btnAgregarCurso);

        add.setOnClickListener(v ->
                startActivity(
                        new Intent(
                                this,
                                FormCursoActivity.class
                        )
                )
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarCursos();
    }

    private void cargarCursos() {

        setLoading(true);

        RetrofitClient.getInstance(this)
                .getApiService()
                .obtenerCursos()
                .enqueue(new Callback<JsonElement>() {

                    @Override
                    public void onResponse(
                            Call<JsonElement> call,
                            Response<JsonElement> response
                    ) {

                        setLoading(false);

                        if (response.code() == 401) {
                            ApiErrorHandler.handleUnauthorized(
                                    CursosActivity.this
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

                            List<Curso> cursos =
                                    CursoJsonMapper.toList(
                                            response.body()
                                    );

                            adapter.submitList(cursos);

                            emptyView.setVisibility(
                                    cursos.isEmpty()
                                            ? View.VISIBLE
                                            : View.GONE
                            );

                        } catch (IllegalArgumentException error) {

                            showMessage(
                                    "El servidor devolvió una lista de cursos inválida."
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

    private void openDetail(Curso curso) {

        Intent intent =
                new Intent(
                        this,
                        DetalleCursoActivity.class
                );

        intent.putExtra(
                DetalleCursoActivity.EXTRA_CURSO_ID,
                curso.getId()
        );

        startActivity(intent);
    }

    private void setLoading(boolean loading) {

        progressBar.setVisibility(
                loading
                        ? View.VISIBLE
                        : View.GONE
        );
    }

    private void showMessage(String message) {

        Toast.makeText(
                this,
                message,
                Toast.LENGTH_LONG
        ).show();
    }
}


