package com.ieltsai.ielts_ai_backend.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

/**
 * SpringDoc OpenAPI (Swagger) configuration.
 *
 * Defines the API metadata and registers a global HTTP Bearer security scheme
 * so that the "Authorize" button appears at the top of the Swagger UI.
 * Once a JWT is entered there, Swagger will automatically send it as
 * "Authorization: Bearer <token>" on every protected endpoint's "Try it out" call.
 *
 * The scheme name "bearerAuth" must match what is referenced in @SecurityRequirement
 * here (global) and in any per-endpoint @Operation annotations if you ever want
 * to override at the method level.
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title       = "IELTS AI Backend API",
                version     = "1.0",
                description = "REST API for the IELTS AI preparation platform. " +
                              "Authenticate via POST /api/v1/auth/login, copy the " +
                              "returned accessToken, click 'Authorize' above, and " +
                              "paste the token to unlock protected endpoints.",
                contact     = @Contact(
                        name  = "IELTS AI Team",
                        email = "support@ieltsai.com"
                )
        ),
        servers = {
                @Server(url = "/", description = "Current host")
        },
        // Applies the "bearerAuth" security scheme globally to every endpoint.
        // Individual public endpoints (e.g. /api/v1/auth/**) are already open
        // via SecurityConfig.permitAll(), so the lock icon on them is cosmetic only.
        security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
        name        = "bearerAuth",          // Must match the name in @SecurityRequirement above
        description = "Paste your JWT access token here (without the 'Bearer ' prefix). " +
                      "Obtain a token from POST /api/v1/auth/login or /api/v1/auth/register.",
        type        = SecuritySchemeType.HTTP,
        scheme      = "bearer",              // Tells Swagger UI to render the Authorize dialog
        bearerFormat = "JWT",               // Purely informational label in the UI
        in          = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {
    // No beans needed — all configuration is handled declaratively via
    // the SpringDoc annotations on this class.
}
