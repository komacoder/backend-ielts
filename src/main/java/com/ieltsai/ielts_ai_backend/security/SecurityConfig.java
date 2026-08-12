package com.ieltsai.ielts_ai_backend.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity  // Activates @PreAuthorize / @PostAuthorize on controller methods
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    // ─── Public paths — no JWT required ───────────────────────────────────────
    private static final String[] PUBLIC_PATHS = {
            // Auth endpoints (register, login, refresh, logout)
            "/api/v1/auth/**",

            // SpringDoc / Swagger UI endpoints (comprehensive list for Spring Boot 3)
            "/v3/api-docs",
            "/v3/api-docs/**",
            "/v3/api-docs.yaml",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/swagger-resources",
            "/swagger-resources/**",
            "/configuration/ui",
            "/configuration/security",
            "/webjars/**",

            // Spring Boot built-in error endpoint
            "/error"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // ── CORS: delegates to CorsConfigurationSource bean (if defined) ──
            .cors(org.springframework.security.config.Customizer.withDefaults())

            // ── CSRF: disabled — stateless JWT APIs do not need CSRF protection ──
            .csrf(AbstractHttpConfigurer::disable)

            // ── Authorization rules (order matters — first match wins) ──────────
            .authorizeHttpRequests(auth -> auth

                // 1. Public endpoints — no token needed
                .requestMatchers(PUBLIC_PATHS).permitAll()

                // 2. Admin-only endpoints — URL-level guard (defense-in-depth;
                //    individual methods are also protected via @PreAuthorize)
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

                // 3. Everything else requires a valid JWT
                .anyRequest().authenticated()
            )

            // ── Session: STATELESS — Spring Security never creates an HttpSession ──
            .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // ── Auth provider: uses our UserDetailsService + Argon2 encoder ──────
            .authenticationProvider(authenticationProvider)

            // ── JWT filter runs before Spring's form-login filter ─────────────────
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
