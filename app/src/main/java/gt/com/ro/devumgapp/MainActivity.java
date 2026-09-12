package gt.com.ro.devumgapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import gt.com.ro.devumgapp.activities.EstudiantesActivity;
import gt.com.ro.devumgapp.utils.TokenManager;

/** Pantalla principal disponible sólo con una sesión JWT válida localmente. */
public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!TokenManager.getInstance(this).hasToken()) {
            openLogin();
            return;
        }

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setGravity(Gravity.CENTER);
        content.setPadding(48, 48, 48, 48);
        content.setBackgroundColor(Color.rgb(11, 36, 70));

        TextView title = new TextView(this);
        title.setText("Centro Educativo");
        title.setTextColor(Color.WHITE);
        title.setTextSize(28);
        title.setGravity(Gravity.CENTER);

        Button estudiantes = new Button(this);
        estudiantes.setText("GESTIONAR ESTUDIANTES");
        estudiantes.setOnClickListener(v -> startActivity(new Intent(this, EstudiantesActivity.class)));

        Button cerrarSesion = new Button(this);
        cerrarSesion.setText("CERRAR SESIÓN");
        cerrarSesion.setOnClickListener(v -> {
            TokenManager.getInstance(this).clearToken();
            openLogin();
        });

        content.addView(title, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        buttonParams.topMargin = 32;
        content.addView(estudiantes, buttonParams);
        LinearLayout.LayoutParams logoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        logoutParams.topMargin = 16;
        content.addView(cerrarSesion, logoutParams);
        setContentView(content);
    }

    private void openLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
