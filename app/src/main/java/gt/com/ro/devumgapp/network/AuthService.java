package gt.com.ro.devumgapp.network;

import gt.com.ro.devumgapp.network.model.LoginRequest;
import gt.com.ro.devumgapp.network.model.LoginResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/** Contrato público de autenticación: POST /api/auth/login. */
public interface AuthService {
    @POST("auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);
}
