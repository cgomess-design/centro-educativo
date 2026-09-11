package gt.com.ro.devumgapp.network;

import android.content.Context;

import java.util.concurrent.TimeUnit;

import gt.com.ro.devumgapp.utils.AuthInterceptor;
import gt.com.ro.devumgapp.utils.TokenManager;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static final String BASE_URL = "https://sgau-backend-api-467280352705.us-central1.run.app/api/";
    private static RetrofitClient instance;
    private final ApiService apiService;

    private RetrofitClient(Context context) {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(new AuthInterceptor(TokenManager.getInstance(context)))
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build();

        apiService = retrofit.create(ApiService.class);
    }

    public static synchronized RetrofitClient getInstance(Context context) {
        if (instance == null) {
            instance = new RetrofitClient(context.getApplicationContext());
        }
        return instance;
    }

    public ApiService getApiService() {
        return apiService;
    }

    public AuthService getAuthService() {
        return apiService;
    }
}
