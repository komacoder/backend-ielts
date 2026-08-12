package com.ieltsai.ielts_ai_backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ieltsai.ielts_ai_backend.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationResponse {

    /** The short-lived JWT access token to be included in Authorization: Bearer headers. */
    private String accessToken;

    /** The user's email address, returned for convenience so clients don't need to decode the JWT. */
    private String email;

    /** The user's assigned role, e.g. USER or ADMIN. */
    private Role role;

    /**
     * The refresh token is intentionally excluded from the JSON response body.
     * It is sent via a Set-Cookie header as an HttpOnly, Secure cookie by the controller.
     */
    @JsonIgnore
    private String refreshToken;
}
