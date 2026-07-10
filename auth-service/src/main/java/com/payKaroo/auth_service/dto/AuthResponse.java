package com.payKaroo.auth_service.dto;

public class AuthResponse {

    private Long userId;
    private String name;
    private String email;
    private String role;
    private String token;

    public AuthResponse(Long userId, String name, String email, String role, String token) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.role = role;
        this.token = token;
    }

    public Long getUserId() { return userId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public String getToken() { return token; }
}
