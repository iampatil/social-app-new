package com.social.user;

import com.social.user.security.AuthUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Service
public class JwtService {
    private final SecretKey key;
    private final long ttl;

    public JwtService(@Value("${jwt.secret}") String secret, @Value("${jwt.ttl-ms}") long ttl) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.ttl = ttl;
    }

    public String generate(User u) {
        return Jwts.builder()
                .subject(String.valueOf(u.getId()))
                .claim("username", u.getUsername())
                .claim("roles", List.of(u.getRole()))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + ttl))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    /** Verifies signature and expiry; throws JwtException if the token is not valid. */
    public AuthUser parse(String token) {
        Claims c = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
        List<?> raw = c.get("roles", List.class);
        List<String> roles = raw == null ? List.of() : raw.stream().map(String::valueOf).toList();
        return new AuthUser(Long.valueOf(c.getSubject()), c.get("username", String.class), roles);
    }
}
