package com.ieltsai.ielts_ai_backend.entity;

/**
 * Application-level roles.
 *
 * The enum constant names are intentionally WITHOUT the "ROLE_" prefix.
 * Spring Security's hasRole("ADMIN") automatically prepends "ROLE_" when matching,
 * so getAuthorities() must return "ROLE_ADMIN" / "ROLE_USER" — which is done in User.java.
 */
public enum Role {
    USER,
    ADMIN;

    /** Returns the Spring Security authority string, e.g. "ROLE_USER". */
    public String getAuthority() {
        return "ROLE_" + this.name();
    }
}
