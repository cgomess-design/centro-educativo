package gt.com.ro.devumgapp.network.model;

import com.google.gson.annotations.SerializedName;

public class LoginRequest {

    @SerializedName("username")
    private String username;

    @SerializedName("email")
    private String email;

    @SerializedName("password")
    private String password;

    public LoginRequest(String identifier, String password) {
        this.password = password;
        if (identifier != null && identifier.contains("@")) {
            this.email = identifier;
            this.username = identifier;
        } else {
            this.username = identifier;
            this.email = null;
        }
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
