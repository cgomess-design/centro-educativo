package gt.com.ro.devumgapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

/** Gestiona la persistencia de la sesión: token JWT, rol y nombre de usuario. */
public final class TokenManager {
    private static final String PREFS_NAME = "auth_preferences";
    private static final String KEY_TOKEN = "jwt";
    private static final String KEY_ROLE = "user_role";
    private static final String KEY_USERNAME = "user_username";

    private static TokenManager instance;
    private final SharedPreferences preferences;

    private TokenManager(Context context) {
        preferences = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized TokenManager getInstance(Context context) {
        if (instance == null) {
            instance = new TokenManager(context);
        }
        return instance;
    }

    public void saveSession(String token, String role, String username) {
        SharedPreferences.Editor editor = preferences.edit();
        if (token != null && !token.trim().isEmpty()) {
            editor.putString(KEY_TOKEN, token.trim());
        } else {
            editor.remove(KEY_TOKEN);
        }
        if (role != null && !role.trim().isEmpty()) {
            editor.putString(KEY_ROLE, role.trim());
        } else {
            editor.remove(KEY_ROLE);
        }
        if (username != null && !username.trim().isEmpty()) {
            editor.putString(KEY_USERNAME, username.trim());
        } else {
            editor.remove(KEY_USERNAME);
        }
        editor.apply();
    }

    public void saveToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            clearToken();
            return;
        }
        preferences.edit().putString(KEY_TOKEN, token.trim()).apply();
    }

    public String getToken() {
        return preferences.getString(KEY_TOKEN, null);
    }

    public String getRole() {
        return preferences.getString(KEY_ROLE, null);
    }

    public String getUsername() {
        return preferences.getString(KEY_USERNAME, null);
    }

    public boolean hasToken() {
        String token = getToken();
        return token != null && !token.trim().isEmpty();
    }

    public void clearToken() {
        preferences.edit()
                .remove(KEY_TOKEN)
                .remove(KEY_ROLE)
                .remove(KEY_USERNAME)
                .apply();
    }
}
