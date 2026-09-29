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
import gt.com.ro.devumgapp.adapters.CarreraAdapter;
import gt.com.ro.devumgapp.network.RetrofitClient;
import gt.com.ro.devumgapp.network.model.Carrera;
import gt.com.ro.devumgapp.utils.ApiErrorHandler;
import gt.com.ro.devumgapp.utils.CarreraJsonMapper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Listado administrativo de carreras. */
public class CarrerasActivity extends AppCompatActivity {
    private CarreraAdapter carreraAdapter;
    private ProgressBar progressBar;
    private TextView emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_carreras);

        progressBar = findViewById(R.id.progressCarreras);
        emptyView = findViewById(R.id.txtCarrerasVacias);

        RecyclerView recycler = findViewById(R.id.recyclerCarreras);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        carreraAdapter = new CarreraAdapter(this::openDetail);
        recycler.setAdapter(carreraAdapter);

        Button add = findViewById(R.id.btnAgregarCarrera);
        add.setOnClickListener(v ->
                startActivity(new Intent(this, FormCarreraActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarCarreras();
    }

    private void cargarCarreras() {
        setLoading(true);
        RetrofitClient.getInstance(this).getApiService().obtenerCarreras()
                .enqueue(new Callback<JsonElement>() {
                    @Override
                    public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                        setLoading(false);
                        if (response.code() == 401) {
                            ApiErrorHandler.handleUnauthorized(CarrerasActivity.this);
                            return;
                        }
                        if (!response.isSuccessful() || response.body() == null) {
                            showMessage(ApiErrorHandler.getMessage(response));
                            return;
                        }
                        try {
                            List<Carrera> carreras = CarreraJsonMapper.toList(response.body());
                            carreraAdapter.submitList(carreras);
                            emptyView.setVisibility(carreras.isEmpty() ? View.VISIBLE : View.GONE);
                        } catch (IllegalArgumentException error) {
                            showMessage("El servidor devolvió una lista de carreras inválida.");
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonElement> call, Throwable error) {
                        setLoading(false);
                        showMessage(ApiErrorHandler.getNetworkMessage(error));
                    }
                });
    }

    private void openDetail(Carrera carrera) {
        Intent intent = new Intent(this, DetalleCarreraActivity.class);
        intent.putExtra(DetalleCarreraActivity.EXTRA_CARRERA_ID, carrera.getId());
        startActivity(intent);
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
