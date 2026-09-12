package gt.com.ro.devumgapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

/** Guarda únicamente el JWT; nunca credenciales del usuario. */
public final class TokenManager {
    private static final String PREFS_NAME = "auth_preferences";
    private static final String KEY_TOKEN = "jwt";
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

    public boolean hasToken() {
        String token = getToken();
        return token != null && !token.trim().isEmpty();
    }

    public void clearToken() {
        preferences.edit().remove(KEY_TOKEN).apply();
    }
}
