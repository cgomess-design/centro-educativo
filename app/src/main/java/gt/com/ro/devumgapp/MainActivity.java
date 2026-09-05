
package gt.com.ro.devumgapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import gt.com.ro.devumgapp.network.RetrofitClient;
import gt.com.ro.devumgapp.network.model.LoginRequest;
import gt.com.ro.devumgapp.network.model.LoginResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "DevUmgPrefs";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_PASS = "user_pass";
    private static final String KEY_AUTH_TOKEN = "auth_token";

    private EditText edtUsuario;
    private EditText edtPassword;
    private Button btnLogin;
    private CheckBox chkRecordar;
    private ImageButton btnShowPassword;
    private ProgressBar progressBarLogin;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        getWindow().setStatusBarColor(getResources().getColor(R.color.bg_blue_darkest, getTheme()));
        getWindow().setNavigationBarColor(getResources().getColor(R.color.bg_blue_darkest, getTheme()));

        inicializarVistas();
        configurarTogglePassword();
        configurarLogin();
        cargarSesion();
    }

    private void inicializarVistas() {
        edtUsuario = findViewById(R.id.edtUsuario);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        chkRecordar = findViewById(R.id.chkRecordar);
        btnShowPassword = findViewById(R.id.btnShowPassword);
        progressBarLogin = findViewById(R.id.progressBarLogin);
    }

    private void configurarTogglePassword() {
        btnShowPassword.setOnClickListener(v -> {
            if (isPasswordVisible) {
                edtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                btnShowPassword.setImageResource(R.drawable.ic_visibility);
            } else {
                edtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                btnShowPassword.setImageResource(R.drawable.ic_visibility_off);
            }

            isPasswordVisible = !isPasswordVisible;
            edtPassword.setSelection(edtPassword.getText().length());
        });
    }

    private void configurarLogin() {
        btnLogin.setOnClickListener(v -> validarCredenciales());
    }

    private void validarCredenciales() {
        String user = edtUsuario.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        if (user.isEmpty() || password.isEmpty()) {
            mostrarMensaje("Los campos son obligatorios");
            return;
        }

        ejecutarLoginApi(user, password);
    }

    private void ejecutarLoginApi(String user, String password) {
        mostrarCargando(true);

        LoginRequest request = new LoginRequest(user, password);
        RetrofitClient.getInstance().getApiService().login(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                mostrarCargando(false);

                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();
                    String token = loginResponse.getEffectiveToken();
                    String tokenToSave = (token != null && !token.trim().isEmpty()) ? token : "AUTH-TOKEN-OK";

                    mostrarMensaje("Bienvenido, validación exitosa");

                    if (chkRecordar.isChecked()) {
                        guardarSesion(user, password, tokenToSave);
                        mostrarMensaje("Sesión registrada correctamente");
                    } else {
                        limpiarSesion();
                    }
                } else {
                    String mensajeError;
                    int code = response.code();

                    if (code == 401 || code == 403) {
                        mensajeError = "Usuario o contraseña incorrectos";
                    } else if (code == 404) {
                        mensajeError = "Servicio de autenticación no encontrado (404)";
                    } else if (code >= 500) {
                        mensajeError = "Error en el servidor (" + code + ")";
                    } else {
                        mensajeError = "Error en la autenticación (Código " + code + ")";
                    }

                    mostrarMensaje(mensajeError);
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                mostrarCargando(false);
                mostrarMensaje("Error de conexión: " + (t.getMessage() != null ? t.getMessage() : "No se pudo contactar al servidor"));
            }
        });
    }

    private void mostrarCargando(boolean cargando) {
        if (cargando) {
            progressBarLogin.setVisibility(View.VISIBLE);
            btnLogin.setEnabled(false);
            btnLogin.setText("");
            edtUsuario.setEnabled(false);
            edtPassword.setEnabled(false);
            chkRecordar.setEnabled(false);
        } else {
            progressBarLogin.setVisibility(View.GONE);
            btnLogin.setEnabled(true);
            btnLogin.setText(R.string.login);
            edtUsuario.setEnabled(true);
            edtPassword.setEnabled(true);
            chkRecordar.setEnabled(true);
        }
    }

    private void guardarSesion(String user, String password, String token) {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_USER_NAME, user);
        editor.putString(KEY_USER_PASS, password);
        editor.putString(KEY_AUTH_TOKEN, token);
        editor.apply();
    }

    private void cargarSesion() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String user = preferences.getString(KEY_USER_NAME, "");
        String password = preferences.getString(KEY_USER_PASS, "");
        String authToken = preferences.getString(KEY_AUTH_TOKEN, "");

        if (!user.isEmpty()) {
            edtUsuario.setText(user);
        }

        if (!password.isEmpty()) {
            edtPassword.setText(password);
        }

        if (!user.isEmpty() || !password.isEmpty() || !authToken.isEmpty()) {
            chkRecordar.setChecked(true);
        }
    }

    private void limpiarSesion() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(KEY_USER_NAME);
        editor.remove(KEY_USER_PASS);
        editor.remove(KEY_AUTH_TOKEN);
        editor.apply();
    }

    private void mostrarMensaje(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
    }
}