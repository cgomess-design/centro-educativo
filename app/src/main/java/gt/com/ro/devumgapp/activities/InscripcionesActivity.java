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
import gt.com.ro.devumgapp.adapters.InscripcionAdapter;
import gt.com.ro.devumgapp.network.RetrofitClient;
import gt.com.ro.devumgapp.network.model.Inscripcion;
import gt.com.ro.devumgapp.utils.ApiErrorHandler;
import gt.com.ro.devumgapp.utils.InscripcionJsonMapper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Pantalla principal del módulo de Inscripciones.
 */
public class InscripcionesActivity extends AppCompatActivity {

    private InscripcionAdapter adapter;
    private ProgressBar progressBar;
    private TextView emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inscripciones);

        progressBar = findViewById(
                R.id.progressInscripciones
        );

        emptyView = findViewById(
                R.id.txtInscripcionesVacias
        );

        RecyclerView recycler =
                findViewById(R.id.recyclerInscripciones);

        recycler.setLayoutManager(
                new LinearLayoutManager(this)
        );

        adapter = new InscripcionAdapter(
                this::openDetail
        );

        recycler.setAdapter(adapter);

        Button add =
                findViewById(R.id.btnAgregarInscripcion);

        add.setOnClickListener(v ->
                startActivity(
                        new Intent(
                                this,
                                FormInscripcionActivity.class
                        )
                )
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarInscripciones();
    }

    /**
     * Obtiene todas las inscripciones desde el backend.
     */
    private void cargarInscripciones() {

        setLoading(true);

        RetrofitClient.getInstance(this)
                .getApiService()
                .obtenerInscripciones()
                .enqueue(new Callback<JsonElement>() {

                    @Override
                    public void onResponse(
                            Call<JsonElement> call,
                            Response<JsonElement> response
                    ) {

                        setLoading(false);

                        if (response.code() == 401) {

                            ApiErrorHandler.handleUnauthorized(
                                    InscripcionesActivity.this
                            );

                            return;
                        }

                        if (!response.isSuccessful()
                                || response.body() == null) {

                            showMessage(
                                    ApiErrorHandler.getMessage(
                                            response
                                    )
                            );

                            return;
                        }

                        try {

                            List<Inscripcion> inscripciones =
                                    InscripcionJsonMapper.toList(
                                            response.body()
                                    );

                            adapter.submitList(
                                    inscripciones
                            );

                            emptyView.setVisibility(
                                    inscripciones.isEmpty()
                                            ? View.VISIBLE
                                            : View.GONE
                            );

                        } catch (IllegalArgumentException error) {

                            showMessage(
                                    "El servidor devolvió una lista de inscripciones inválida."
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
                                ApiErrorHandler.getNetworkMessage(
                                        error
                                )
                        );
                    }
                });
    }

    /**
     * Abre el detalle de una inscripción.
     */
    private void openDetail(
            Inscripcion inscripcion
    ) {

        Intent intent =
                new Intent(
                        this,
                        DetalleInscripcionActivity.class
                );

        intent.putExtra(
                DetalleInscripcionActivity.EXTRA_INSCRIPCION_ID,
                inscripcion.getId()
        );

        startActivity(intent);
    }

    /**
     * Muestra u oculta el indicador de carga.
     */
    private void setLoading(
            boolean loading
    ) {

        progressBar.setVisibility(
                loading
                        ? View.VISIBLE
                        : View.GONE
        );
    }

    /**
     * Muestra un mensaje al usuario.
     */
    private void showMessage(
            String message
    ) {

        Toast.makeText(
                this,
                message,
                Toast.LENGTH_LONG
        ).show();
    }
}