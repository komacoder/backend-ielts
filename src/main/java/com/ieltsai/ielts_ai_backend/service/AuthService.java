package com.ieltsai.ielts_ai_backend.service;

import com.ieltsai.ielts_ai_backend.dto.AuthenticationResponse;
import com.ieltsai.ielts_ai_backend.dto.LoginRequest;
import com.ieltsai.ielts_ai_backend.dto.RegisterRequest;
import com.ieltsai.ielts_ai_backend.entity.RefreshToken;
import com.ieltsai.ielts_ai_backend.entity.Role;
import com.ieltsai.ielts_ai_backend.entity.User;
import com.ieltsai.ielts_ai_backend.repository.RefreshTokenRepository;
import com.ieltsai.ielts_ai_backend.repository.UserRepository;
import com.ieltsai.ielts_ai_backend.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthenticationResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("An account with this email already exists.");
        }

        Role assignedRole = java.util.Objects.requireNonNullElse(request.getRole(), Role.USER);

        var user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(assignedRole)
                .build();

        userRepository.save(user);

        var jwtToken = jwtService.generateToken(user);
        // ── Delegate to RefreshTokenService (the SINGLE source of truth) ──
        var refreshToken = refreshTokenService.createOrUpdateRefreshToken(user);

        return AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .email(user.getEmail())
                .role(user.getRole())
                .refreshToken(refreshToken.getToken())
                .build();
    }

    @Transactional
    public AuthenticationResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        } catch (org.springframework.security.core.AuthenticationException ex) {
            throw new BadCredentialsException("Invalid email or password", ex);
        }

        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found after authentication"));

        var jwtToken = jwtService.generateToken(user);
        // ── Delegate to RefreshTokenService (the SINGLE source of truth) ──
        var refreshToken = refreshTokenService.createOrUpdateRefreshToken(user);

        return AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .email(user.getEmail())
                .role(user.getRole())
                .refreshToken(refreshToken.getToken())
                .build();
    }

    @Transactional
    public AuthenticationResponse refreshToken(String token) {
        RefreshToken existingToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));

        if (existingToken.isRevoked()) {
            throw new RuntimeException("Refresh token is revoked");
        }

        if (existingToken.getExpiryDate().compareTo(Instant.now()) < 0) {
            existingToken.setRevoked(true);
            refreshTokenRepository.save(existingToken);
            throw new RuntimeException("Refresh token was expired");
        }

        User user = existingToken.getUser();
        String newAccessToken = jwtService.generateToken(user);
        // ── Delegate to RefreshTokenService (the SINGLE source of truth) ──
        // This will UPDATE the existing row, not insert a duplicate
        RefreshToken newRefreshToken = refreshTokenService.createOrUpdateRefreshToken(user);

        return AuthenticationResponse.builder()
                .accessToken(newAccessToken)
                .email(user.getEmail())
                .role(user.getRole())
                .refreshToken(newRefreshToken.getToken())
                .build();
    }

    @Transactional
    public void logout(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(existingToken -> {
            existingToken.setRevoked(true);
            refreshTokenRepository.save(existingToken);
        });
    }
}
