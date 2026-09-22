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
import gt.com.ro.devumgapp.adapters.DocenteAdapter;
import gt.com.ro.devumgapp.network.RetrofitClient;
import gt.com.ro.devumgapp.network.model.Docente;
import gt.com.ro.devumgapp.utils.ApiErrorHandler;
import gt.com.ro.devumgapp.utils.DocenteJsonMapper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DocenteActivity extends AppCompatActivity {

    private DocenteAdapter adapter;
    private ProgressBar progressBar;
    private TextView emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_docentes);

        progressBar = findViewById(R.id.progressDocentes);
        emptyView = findViewById(R.id.txtDocentesVacios);

        RecyclerView recycler = findViewById(R.id.recyclerDocentes);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        adapter = new DocenteAdapter(this::openDetail);
        recycler.setAdapter(adapter);

        Button add = findViewById(R.id.btnAgregarDocente);

        add.setOnClickListener(v ->
                startActivity(
                        new Intent(
                                this,
                                FormDocenteActivity.class
                        )
                )
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarDocentes();
    }

    private void cargarDocentes() {
        setLoading(true);

        RetrofitClient.getInstance(this)
                .getApiService()
                .obtenerDocentes()
                .enqueue(new Callback<JsonElement>() {
                    @Override
                    public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                        setLoading(false);

                        if (response.code() == 401) {
                            ApiErrorHandler.handleUnauthorized(DocenteActivity.this);
                            return;
                        }

                        if (!response.isSuccessful() || response.body() == null) {
                            showMessage(ApiErrorHandler.getMessage(response));
                            return;
                        }

                        try {
                            List<Docente> docentes = DocenteJsonMapper.toList(response.body());
                            adapter.submitList(docentes);
                            emptyView.setVisibility(docentes.isEmpty() ? View.VISIBLE : View.GONE);
                        } catch (IllegalArgumentException error) {
                            showMessage("El servidor devolvió una lista de docentes inválida.");
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonElement> call, Throwable error) {
                        setLoading(false);
                        showMessage(ApiErrorHandler.getNetworkMessage(error));
                    }
                });
    }

    private void openDetail(Docente docente) {
        Intent intent = new Intent(this, DetalleDocenteActivity.class);
        intent.putExtra("docente_id", docente.getId());
        startActivity(intent);
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}

