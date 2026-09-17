package gt.com.ro.devumgapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import gt.com.ro.devumgapp.activities.EstudiantesActivity;
import gt.com.ro.devumgapp.utils.JwtUtils;
import gt.com.ro.devumgapp.utils.TokenManager;

/** Pantalla de menú principal con filtrado de opciones según el rol del usuario (RBAC). */
public class MainActivity extends AppCompatActivity {

    private TextView txtMenuUserRole;
    private TextView txtMenuSinPermisos;

    private Button btnEstudiantes;
    private Button btnDocentes;
    private Button btnCursos;
    private Button btnInscripciones;
    private Button btnNotas;
    private Button btnConsultarNotas;
    private Button btnConsultarCursos;
    private Button btnConsultarCursosAsignados;
    private Button btnCerrarSesion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!TokenManager.getInstance(this).hasToken()) {
            openLogin();
            return;
        }

        setContentView(R.layout.activity_menu);

        initViews();
        setupListeners();
        configurarMenuPorRol();
    }

    private void initViews() {
        txtMenuUserRole = findViewById(R.id.txtMenuUserRole);
        txtMenuSinPermisos = findViewById(R.id.txtMenuSinPermisos);

        btnEstudiantes = findViewById(R.id.btnMenuEstudiantes);
        btnDocentes = findViewById(R.id.btnMenuDocentes);
        btnCursos = findViewById(R.id.btnMenuCursos);
        btnInscripciones = findViewById(R.id.btnMenuInscripciones);
        btnNotas = findViewById(R.id.btnMenuNotas);
        btnConsultarNotas = findViewById(R.id.btnMenuConsultarNotas);
        btnConsultarCursos = findViewById(R.id.btnMenuConsultarCursos);
        btnConsultarCursosAsignados = findViewById(R.id.btnMenuConsultarCursosAsignados);
        btnCerrarSesion = findViewById(R.id.btnMenuCerrarSesion);
    }

    private void setupListeners() {
        btnEstudiantes.setOnClickListener(v ->
                startActivity(new Intent(this, EstudiantesActivity.class)));

        btnDocentes.setOnClickListener(v -> showUpcomingModule("Docentes"));
        btnCursos.setOnClickListener(v -> showUpcomingModule("Cursos"));
        btnInscripciones.setOnClickListener(v -> showUpcomingModule("Inscripciones"));
        btnNotas.setOnClickListener(v -> showUpcomingModule("Notas"));
        btnConsultarNotas.setOnClickListener(v -> showUpcomingModule("Consulta de Notas"));
        btnConsultarCursos.setOnClickListener(v -> showUpcomingModule("Consulta de Cursos"));
        btnConsultarCursosAsignados.setOnClickListener(v -> showUpcomingModule("Consulta de Cursos Asignados"));

        btnCerrarSesion.setOnClickListener(v -> {
            TokenManager.getInstance(this).clearToken();
            openLogin();
        });
    }

    private void configurarMenuPorRol() {
        TokenManager tokenManager = TokenManager.getInstance(this);
        String role = tokenManager.getRole();
        String username = tokenManager.getUsername();

        // Recuperar del JWT si la sesión no guardó previamente el rol o usuario
        String token = tokenManager.getToken();
        if ((role == null || role.trim().isEmpty()) && token != null) {
            role = JwtUtils.extractRole(token);
            if (role != null) {
                tokenManager.saveSession(token, role, username);
            }
        }
        if ((username == null || username.trim().isEmpty()) && token != null) {
            username = JwtUtils.extractUsername(token);
            if (username != null) {
                tokenManager.saveSession(token, role, username);
            }
        }

        String normalized = role != null ? role.trim().toUpperCase() : "";

        // Ocultar todas las opciones por defecto
        ocultarTodasLasOpciones();

        if (normalized.contains("DOCENTE")) {
            // Rol Docente: Solo Gestionar Notas y Consultar Cursos Asignados
            actualizarIndicadorRol(getString(R.string.role_docente), username);
            btnNotas.setVisibility(View.VISIBLE);
            btnConsultarCursosAsignados.setVisibility(View.VISIBLE);
            txtMenuSinPermisos.setVisibility(View.GONE);

        } else if (normalized.contains("ESTUDIANTE") || normalized.contains("ALUMNO")) {
            // Rol Estudiante: Solo Consultar Notas y Consultar Cursos
            actualizarIndicadorRol(getString(R.string.role_estudiante), username);
            btnConsultarNotas.setVisibility(View.VISIBLE);
            btnConsultarCursos.setVisibility(View.VISIBLE);
            txtMenuSinPermisos.setVisibility(View.GONE);

        } else if (normalized.contains("ADMIN")) {
            // Rol Admin: Gestionar Estudiantes, Docentes, Cursos e Inscripciones
            actualizarIndicadorRol(getString(R.string.role_admin), username);
            btnEstudiantes.setVisibility(View.VISIBLE);
            btnDocentes.setVisibility(View.VISIBLE);
            btnCursos.setVisibility(View.VISIBLE);
            btnInscripciones.setVisibility(View.VISIBLE);
            txtMenuSinPermisos.setVisibility(View.GONE);

        } else {
            // Rol desconocido o ausente: Solo mostrar opción de cerrar sesión
            actualizarIndicadorRol(getString(R.string.role_sin_rol), username);
            txtMenuSinPermisos.setVisibility(View.VISIBLE);
        }

        // Cerrar sesión siempre está visible
        btnCerrarSesion.setVisibility(View.VISIBLE);
    }

    private void ocultarTodasLasOpciones() {
        btnEstudiantes.setVisibility(View.GONE);
        btnDocentes.setVisibility(View.GONE);
        btnCursos.setVisibility(View.GONE);
        btnInscripciones.setVisibility(View.GONE);
        btnNotas.setVisibility(View.GONE);
        btnConsultarNotas.setVisibility(View.GONE);
        btnConsultarCursos.setVisibility(View.GONE);
        btnConsultarCursosAsignados.setVisibility(View.GONE);
    }

    private void actualizarIndicadorRol(String roleDisplay, String username) {
        if (txtMenuUserRole == null) return;
        if (username != null && !username.trim().isEmpty()) {
            txtMenuUserRole.setText(getString(R.string.menu_user_session, roleDisplay, username));
        } else {
            txtMenuUserRole.setText(getString(R.string.menu_user_session_no_user, roleDisplay));
        }
    }

    private void showUpcomingModule(String moduleName) {
        Toast.makeText(this, getString(R.string.msg_modulo_en_desarrollo, moduleName), Toast.LENGTH_SHORT).show();
    }

    private void openLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
