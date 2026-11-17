package com.social.post.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

/** Verifies tokens issued by user-service (same shared secret). Throws JwtException when invalid. */
@Component
public class JwtVerifier {
    private final SecretKey key;

    public JwtVerifier(@Value("${jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public AuthUser parse(String token) {
        Claims c = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
        List<?> raw = c.get("roles", List.class);
        List<String> roles = raw == null ? List.of() : raw.stream().map(String::valueOf).toList();
        return new AuthUser(Long.valueOf(c.getSubject()), c.get("username", String.class), roles);
    }
}
