package com.spendiq.dto.response;

public class AuthResponse {
    private String token;
    private UserResponse user;

    private AuthResponse() {}

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String token; private UserResponse user;
        public Builder token(String v) { this.token = v; return this; }
        public Builder user(UserResponse v) { this.user = v; return this; }
        public AuthResponse build() {
            AuthResponse r = new AuthResponse(); r.token = token; r.user = user; return r;
        }
    }

    public String getToken()      { return token; }
    public UserResponse getUser() { return user; }
}