package com.ieltsai.ielts_ai_backend.controller;

import com.ieltsai.ielts_ai_backend.dto.AuthenticationResponse;
import com.ieltsai.ielts_ai_backend.dto.LoginRequest;
import com.ieltsai.ielts_ai_backend.dto.RegisterRequest;
import com.ieltsai.ielts_ai_backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
            @RequestBody RegisterRequest request
    ) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(
            @RequestBody LoginRequest request
    ) {
        return ResponseEntity.ok(authService.login(request));
    }
}
