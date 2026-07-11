package com.payKaroo.auth_service.dto;

public class AuthResponse {

    private Long userId;
    private String name;
    private String email;
    private String role;
    private String token;
    private String refreshToken;

    public AuthResponse(Long userId, String name, String email, String role, String token, String refreshToken) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.role = role;
        this.token = token;
        this.refreshToken = refreshToken;
    }

    public Long getUserId() { return userId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public String getToken() { return token; }
    public String getRefreshToken() { return refreshToken; }
}
