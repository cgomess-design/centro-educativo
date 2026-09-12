package gt.com.ro.devumgapp.utils;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/** Añade el Bearer JWT sólo a solicitudes protegidas. */
public final class AuthInterceptor implements Interceptor {
    private final TokenManager tokenManager;

    public AuthInterceptor(TokenManager tokenManager) {
        this.tokenManager = tokenManager;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        String path = request.url().encodedPath();

        if (path.endsWith("/auth/login") || request.header("Authorization") != null) {
            return chain.proceed(request);
        }

        String token = tokenManager.getToken();
        if (token == null || token.trim().isEmpty()) {
            return chain.proceed(request);
        }

        Request authenticatedRequest = request.newBuilder()
                .header("Authorization", "Bearer " + token)
                .build();
        return chain.proceed(authenticatedRequest);
    }
}
