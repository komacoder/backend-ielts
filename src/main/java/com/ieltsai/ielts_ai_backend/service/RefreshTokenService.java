package com.ieltsai.ielts_ai_backend.service;

import com.ieltsai.ielts_ai_backend.entity.RefreshToken;
import com.ieltsai.ielts_ai_backend.entity.User;
import com.ieltsai.ielts_ai_backend.repository.RefreshTokenRepository;
import com.ieltsai.ielts_ai_backend.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Single source of truth for all refresh token persistence.
 * No other class should directly save RefreshToken entities.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    @Value("${app.jwtRefreshExpirationMs:604800000}")
    private Long refreshTokenDurationMs;

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Creates or updates a refresh token for the given user.
     * Guarantees exactly one active (non-revoked) token per user at all times.
     *
     * Strategy:
     * 1. Query by user ID (primitive long, immune to Hibernate proxy issues).
     * 2. If a row exists → UPDATE its token string and expiry in-place.
     * 3. If no row exists → DELETE any phantom/orphaned rows first, flush to DB,
     *    then INSERT a brand-new row.
     */
    @Transactional
    public RefreshToken createOrUpdateRefreshToken(User user) {
        Long userId = user.getId();

        Optional<RefreshToken> existingOpt = refreshTokenRepository.findByUserId(userId);

        if (existingOpt.isPresent()) {
            // ── UPDATE path ──
            RefreshToken existing = existingOpt.get();
            existing.setToken(UUID.randomUUID().toString());
            existing.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
            existing.setRevoked(false);
            log.debug("Updated existing refresh token for userId={}", userId);
            return refreshTokenRepository.save(existing);
        }

        // ── INSERT path (with failsafe cleanup) ──
        refreshTokenRepository.deleteAllByUserId(userId);
        entityManager.flush();

        RefreshToken newToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(refreshTokenDurationMs))
                .revoked(false)
                .build();

        log.debug("Created new refresh token for userId={}", userId);
        return refreshTokenRepository.save(newToken);
    }
}
