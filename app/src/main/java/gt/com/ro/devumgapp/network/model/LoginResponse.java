package gt.com.ro.devumgapp.network.model;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {

    @SerializedName("token")
    private String token;

    @SerializedName("access_token")
    private String accessToken;

    @SerializedName("jwt")
    private String jwt;

    @SerializedName("message")
    private String message;

    @SerializedName("status")
    private String status;

    @SerializedName("data")
    private ResponseData data;

    @SerializedName("user")
    private User user;

    public static class ResponseData {
        @SerializedName("token")
        private String token;

        @SerializedName("access_token")
        private String accessToken;

        @SerializedName("user")
        private User user;

        public String getToken() {
            return token != null ? token : accessToken;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public String getAccessToken() {
            return accessToken;
        }

        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }

        public User getUser() {
            return user;
        }

        public void setUser(User user) {
            this.user = user;
        }
    }

    public static class User {
        @SerializedName("id")
        private String id;

        @SerializedName("username")
        private String username;

        @SerializedName("name")
        private String name;

        @SerializedName("email")
        private String email;

        @SerializedName("role")
        private String role;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }
    }

    public String getEffectiveToken() {
        if (token != null && !token.trim().isEmpty()) {
            return token;
        }
        if (accessToken != null && !accessToken.trim().isEmpty()) {
            return accessToken;
        }
        if (jwt != null && !jwt.trim().isEmpty()) {
            return jwt;
        }
        if (data != null && data.getToken() != null && !data.getToken().trim().isEmpty()) {
            return data.getToken();
        }
        return null;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getJwt() {
        return jwt;
    }

    public void setJwt(String jwt) {
        this.jwt = jwt;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ResponseData getData() {
        return data;
    }

    public void setData(ResponseData data) {
        this.data = data;
    }

    public User getUser() {
        if (user != null) {
            return user;
        }
        if (data != null && data.getUser() != null) {
            return data.getUser();
        }
        return null;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
