package com.social.post.security;

import java.util.List;

/** The authenticated caller, built from the verified JWT. Injected with @AuthenticationPrincipal. */
public class AuthUser {
    private final Long id;
    private final String username;
    private final List<String> roles;

    public AuthUser(Long id, String username, List<String> roles) {
        this.id = id;
        this.username = username;
        this.roles = roles;
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public List<String> getRoles() { return roles; }
    public boolean isAdmin() { return roles.contains("ROLE_ADMIN"); }
}
