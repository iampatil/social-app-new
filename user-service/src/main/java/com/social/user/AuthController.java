package com.social.user;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    public record RegisterRequest(@NotBlank @Size(min = 3, max = 30) String username,
                                  @NotBlank @Email String email,
                                  @NotBlank @Size(min = 6) String password) {}
    public record LoginRequest(@NotBlank String username, @NotBlank String password) {}
    public record AuthResponse(String token, Long id, String username, String role) {}

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public AuthController(UserRepository users, PasswordEncoder encoder, JwtService jwt) {
        this.users = users; this.encoder = encoder; this.jwt = jwt;
    }

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest r) {
        if (users.existsByUsername(r.username())) throw new ResponseStatusException(HttpStatus.CONFLICT, "Username taken");
        if (users.existsByEmail(r.email())) throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        User u = new User();
        u.setUsername(r.username());
        u.setEmail(r.email());
        u.setDisplayName(r.username());
        u.setPasswordHash(encoder.encode(r.password()));
        users.save(u);
        return new AuthResponse(jwt.generate(u), u.getId(), u.getUsername(), u.getRole());
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest r) {
        User u = users.findByUsername(r.username())
                .filter(x -> encoder.matches(r.password(), x.getPasswordHash()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        return new AuthResponse(jwt.generate(u), u.getId(), u.getUsername(), u.getRole());
    }
}
