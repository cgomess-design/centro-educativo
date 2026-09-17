package gt.com.ro.devumgapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import gt.com.ro.devumgapp.utils.TokenManager;

/** Pantalla de bienvenida (Splash) que enruta hacia MainActivity o LoginActivity tras 2 segundos. */
public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DURATION_MS = 2000L;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable routeRunnable = this::checkSessionAndNavigate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        handler.postDelayed(routeRunnable, SPLASH_DURATION_MS);
    }

    private void checkSessionAndNavigate() {
        if (isFinishing() || isDestroyed()) {
            return;
        }

        boolean hasSession = TokenManager.getInstance(this).hasToken();
        Intent intent;
        if (hasSession) {
            intent = new Intent(this, MainActivity.class);
        } else {
            intent = new Intent(this, LoginActivity.class);
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(routeRunnable);
    }
}
