package com.payKaroo.auth_service.controller;


import com.payKaroo.auth_service.dto.AuthResponse;
import com.payKaroo.auth_service.dto.LoginRequest;
import com.payKaroo.auth_service.dto.RefreshRequest;
import com.payKaroo.auth_service.dto.RegisterRequest;
import com.payKaroo.auth_service.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request){
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody RefreshRequest request) {
        AuthResponse response = authService.refresh(request.getRefreshToken());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestParam Long userId) {
        authService.logout(userId);
        return ResponseEntity.ok("Logged out successfully");
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/admin/test")
    public ResponseEntity<String> adminOnly() {
        return ResponseEntity.ok("Welcome, admin! This is a protected route.");
    }
}
