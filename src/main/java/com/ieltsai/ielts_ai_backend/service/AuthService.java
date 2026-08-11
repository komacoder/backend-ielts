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
            throw new RuntimeException("Email already exists");
        }

        var user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();
        
        userRepository.save(user);
        var jwtToken = jwtService.generateToken(user);
        var refreshToken = createRefreshToken(user);

        return AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken.getToken())
                .build();
    }

    @Transactional
    public AuthenticationResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow();
                
        var jwtToken = jwtService.generateToken(user);
        
        var refreshToken = createRefreshToken(user);

        return AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken.getToken())
                .build();
    }

    private RefreshToken createRefreshToken(User user) {
        var existingToken = refreshTokenRepository.findByUser(user);
        RefreshToken refreshToken;
        
        if (existingToken.isPresent()) {
            refreshToken = existingToken.get();
            refreshToken.setToken(UUID.randomUUID().toString());
            refreshToken.setExpiryDate(Instant.now().plusMillis(604800000));
        } else {
            refreshToken = RefreshToken.builder()
                    .user(user)
                    .token(UUID.randomUUID().toString())
                    .expiryDate(Instant.now().plusMillis(604800000)) // 7 days
                    .build();
        }
        
        return refreshTokenRepository.save(refreshToken);
    }
}
