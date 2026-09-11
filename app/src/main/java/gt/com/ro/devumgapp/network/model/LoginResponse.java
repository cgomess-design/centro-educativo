package gt.com.ro.devumgapp.network.model;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {

    @SerializedName("token")
    private String token;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @SerializedName("type")
    private String type;

    @SerializedName("username")
    private String username;

    @SerializedName("rol")
    private String rol;

    public String getType() { return type; }
    public String getUsername() { return username; }
    public String getRol() { return rol; }
}
