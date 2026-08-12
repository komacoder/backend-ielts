package com.ieltsai.ielts_ai_backend.dto;

import com.ieltsai.ielts_ai_backend.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "Email must not be blank")
    @Email(message = "Email must be a valid email address")
    private String email;

    @NotBlank(message = "Password must not be blank")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    /**
     * Optional role assignment at registration time.
     *
     * Accepted JSON values: "USER" or "ADMIN" (NOT "ROLE_USER" / "ROLE_ADMIN").
     * The "ROLE_" prefix is a Spring Security internal convention; our enum stores
     * plain names (USER, ADMIN) and getAuthority() adds the prefix when needed.
     *
     * If this field is omitted or null in the request body, AuthService defaults to Role.USER.
     * This keeps all existing client requests that don't send a role fully backward compatible.
     */
    private Role role;
}
