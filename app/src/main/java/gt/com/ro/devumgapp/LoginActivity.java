package gt.com.ro.devumgapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import gt.com.ro.devumgapp.network.RetrofitClient;
import gt.com.ro.devumgapp.network.model.LoginRequest;
import gt.com.ro.devumgapp.network.model.LoginResponse;
import gt.com.ro.devumgapp.utils.ApiErrorHandler;
import gt.com.ro.devumgapp.utils.TokenManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Autentica contra POST /api/auth/login y conserva únicamente el JWT. */
public class LoginActivity extends AppCompatActivity {
    private EditText edtUsuario;
    private EditText edtPassword;
    private Button btnLogin;
    private ImageButton btnShowPassword;
    private ProgressBar progressBarLogin;
    private boolean passwordVisible;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (TokenManager.getInstance(this).hasToken()) {
            openMain();
            return;
        }
        setContentView(R.layout.activity_login);
        edtUsuario = findViewById(R.id.edtUsuario);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnShowPassword = findViewById(R.id.btnShowPassword);
        progressBarLogin = findViewById(R.id.progressBarLogin);
        findViewById(R.id.chkRecordar).setVisibility(View.GONE);
        findViewById(R.id.txtForgotPassword).setVisibility(View.GONE);

        btnShowPassword.setOnClickListener(v -> togglePassword());
        btnLogin.setOnClickListener(v -> login());
    }

    private void togglePassword() {
        int type = InputType.TYPE_CLASS_TEXT | (passwordVisible
                ? InputType.TYPE_TEXT_VARIATION_PASSWORD
                : InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        edtPassword.setInputType(type);
        btnShowPassword.setImageResource(passwordVisible
                ? R.drawable.ic_visibility : R.drawable.ic_visibility_off);
        passwordVisible = !passwordVisible;
        edtPassword.setSelection(edtPassword.length());
    }

    private void login() {
        String username = edtUsuario.getText().toString().trim();
        String password = edtPassword.getText().toString();
        if (username.isEmpty() || password.trim().isEmpty()) {
            showMessage("Ingrese su usuario y contraseña.");
            return;
        }
        setLoading(true);
        RetrofitClient.getInstance(this).getAuthService()
                .login(new LoginRequest(username, password))
                .enqueue(new Callback<LoginResponse>() {
                    @Override
                    public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                        setLoading(false);
                        if (response.isSuccessful() && response.body() != null
                                && response.body().getToken() != null
                                && !response.body().getToken().trim().isEmpty()) {
                            TokenManager.getInstance(LoginActivity.this)
                                    .saveToken(response.body().getToken());
                            openMain();
                            return;
                        }
                        if (response.code() == 401) {
                            showMessage("Credenciales inválidas.");
                        } else if (response.code() == 403) {
                            showMessage("Acceso no autorizado.");
                        } else if (response.isSuccessful()) {
                            showMessage("El servidor no devolvió un token válido.");
                        } else {
                            showMessage(ApiErrorHandler.getMessage(response));
                        }
                    }

                    @Override
                    public void onFailure(Call<LoginResponse> call, Throwable error) {
                        setLoading(false);
                        showMessage(ApiErrorHandler.getNetworkMessage(error));
                    }
                });
    }

    private void setLoading(boolean loading) {
        progressBarLogin.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!loading);
        edtUsuario.setEnabled(!loading);
        edtPassword.setEnabled(!loading);
    }

    private void openMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
