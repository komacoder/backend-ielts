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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthenticationResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("An account with this email already exists.");
        }

        // Use the role supplied in the request, or fall back to USER for all self-registrations.
        // Objects.requireNonNullElse is a clean null-coalescing alternative to an if/else block.
        Role assignedRole = java.util.Objects.requireNonNullElse(request.getRole(), Role.USER);

        var user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(assignedRole)
                .build();

        userRepository.save(user);
        var jwtToken = jwtService.generateToken(user);
        var refreshToken = createRefreshToken(user);

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
            // BadCredentialsException, UsernameNotFoundException, DisabledException, etc.
            // Re-throw as a domain exception so the global handler returns 401 instead of 500.
            throw new org.springframework.security.authentication.BadCredentialsException(
                    "Invalid email or password", ex
            );
        }

        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found after authentication"));

        var jwtToken = jwtService.generateToken(user);
        var refreshToken = createRefreshToken(user);

        return AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .email(user.getEmail())
                .role(user.getRole())
                .refreshToken(refreshToken.getToken())
                .build();
    }

    private RefreshToken createRefreshToken(User user) {
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(604800000)) // 7 days
                .revoked(false)
                .build();
        
        return refreshTokenRepository.save(refreshToken);
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
        
        // Revoke the old token (Refresh Token Rotation)
        existingToken.setRevoked(true);
        refreshTokenRepository.save(existingToken);
        
        User user = existingToken.getUser();
        String newAccessToken = jwtService.generateToken(user);
        RefreshToken newRefreshToken = createRefreshToken(user);
        
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
