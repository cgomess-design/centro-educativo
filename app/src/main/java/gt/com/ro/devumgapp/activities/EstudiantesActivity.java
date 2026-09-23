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
import gt.com.ro.devumgapp.adapters.EstudianteAdapter;
import gt.com.ro.devumgapp.network.RetrofitClient;
import gt.com.ro.devumgapp.network.model.Estudiante;
import gt.com.ro.devumgapp.utils.ApiErrorHandler;
import gt.com.ro.devumgapp.utils.EstudianteJsonMapper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EstudiantesActivity extends AppCompatActivity {
    private EstudianteAdapter adapter;
    private ProgressBar progressBar;
    private TextView emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_estudiantes);
        progressBar = findViewById(R.id.progressEstudiantes);
        emptyView = findViewById(R.id.txtEstudiantesVacios);
        RecyclerView recycler = findViewById(R.id.recyclerEstudiantes);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EstudianteAdapter(this::openDetail);
        recycler.setAdapter(adapter);
        Button add = findViewById(R.id.btnAgregarEstudiante);
        add.setOnClickListener(v -> startActivity(new Intent(this, FormEstudianteActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarEstudiantes();
    }

    private void cargarEstudiantes() {
        setLoading(true);
        RetrofitClient.getInstance(this).getApiService().obtenerEstudiantes()
                .enqueue(new Callback<JsonElement>() {
                    @Override
                    public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                        setLoading(false);
                        if (response.code() == 401) {
                            ApiErrorHandler.handleUnauthorized(EstudiantesActivity.this);
                            return;
                        }
                        if (!response.isSuccessful() || response.body() == null) {
                            showMessage(ApiErrorHandler.getMessage(response));
                            return;
                        }
                        try {
                            List<Estudiante> estudiantes = EstudianteJsonMapper.toList(response.body());
                            adapter.submitList(estudiantes);
                            emptyView.setVisibility(estudiantes.isEmpty() ? View.VISIBLE : View.GONE);
                        } catch (IllegalArgumentException error) {
                            showMessage("El servidor devolvió una lista de estudiantes inválida.");
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonElement> call, Throwable error) {
                        setLoading(false);
                        showMessage(ApiErrorHandler.getNetworkMessage(error));
                    }
                });
    }

    private void openDetail(Estudiante estudiante) {
        Intent intent = new Intent(this, DetalleEstudianteActivity.class);
        intent.putExtra(DetalleEstudianteActivity.EXTRA_ESTUDIANTE_ID, estudiante.getId());
        startActivity(intent);
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
