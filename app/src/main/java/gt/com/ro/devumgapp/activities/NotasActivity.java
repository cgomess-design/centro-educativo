package gt.com.ro.devumgapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonElement;

import java.util.List;

import gt.com.ro.devumgapp.R;
import gt.com.ro.devumgapp.adapters.NotaAdapter;
import gt.com.ro.devumgapp.network.RetrofitClient;
import gt.com.ro.devumgapp.network.model.Nota;
import gt.com.ro.devumgapp.utils.ApiErrorHandler;
import gt.com.ro.devumgapp.utils.NotaJsonMapper;
import gt.com.ro.devumgapp.utils.TokenManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotasActivity extends AppCompatActivity {

    private NotaAdapter adapter;
    private ProgressBar progressBar;
    private TextView emptyView;
    private boolean studentMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notas);

        progressBar = findViewById(R.id.progressNotas);
        emptyView = findViewById(R.id.txtNotasVacias);
        String role = TokenManager.getInstance(this).getRole();
        studentMode = role != null
                && (role.trim().toUpperCase().contains("ESTUDIANTE")
                || role.trim().toUpperCase().contains("ALUMNO"));

        RecyclerView recycler = findViewById(R.id.recyclerNotas);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        adapter = new NotaAdapter(studentMode ? null : this::openDetail, studentMode);
        recycler.setAdapter(adapter);

        Button add = findViewById(R.id.btnAgregarNota);
        add.setVisibility(studentMode ? View.GONE : View.VISIBLE);
        add.setOnClickListener(v -> startActivity(new Intent(this, FormNotaActivity.class)));

        if (studentMode) {
            emptyView.setText("Aún no tienes notas registradas.");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarNotas();
    }

    private void cargarNotas() {
        setLoading(true);

        Call<JsonElement> call = studentMode
                ? RetrofitClient.getInstance(this).getApiService().obtenerMisNotas()
                : RetrofitClient.getInstance(this).getApiService().obtenerNotas();

        call
                .enqueue(new Callback<JsonElement>() {
                    @Override
                    public void onResponse(@NonNull Call<JsonElement> call, @NonNull Response<JsonElement> response) {
                        setLoading(false);

                        if (response.code() == 401) {
                            ApiErrorHandler.handleUnauthorized(NotasActivity.this);
                            return;
                        }

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
                    public void onFailure(@NonNull Call<JsonElement> call, @NonNull Throwable error) {
                        setLoading(false);
                        showMessage(ApiErrorHandler.getNetworkMessage(error));
                    }
                });
    }

    private void openDetail(Nota nota) {
        Intent intent = new Intent(this, DetalleNotaActivity.class);
        intent.putExtra(DetalleNotaActivity.EXTRA_NOTA_ID, nota.getId());
        startActivity(intent);
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
