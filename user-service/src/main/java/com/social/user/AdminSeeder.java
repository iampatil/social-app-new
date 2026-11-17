package com.social.user;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Creates a first ADMIN account on startup if it doesn't exist. Change the password via ADMIN_PASSWORD. */
@Component
public class AdminSeeder implements CommandLineRunner {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final String username, password;

    public AdminSeeder(UserRepository users, PasswordEncoder encoder,
                       @Value("${app.admin.username}") String username,
                       @Value("${app.admin.password}") String password) {
        this.users = users; this.encoder = encoder; this.username = username; this.password = password;
    }

    @Override
    public void run(String... args) {
        if (users.existsByUsername(username)) return;
        User u = new User();
        u.setUsername(username);
        u.setEmail(username + "@local");
        u.setDisplayName(username);
        u.setPasswordHash(encoder.encode(password));
        u.setRole("ROLE_ADMIN");
        users.save(u);
    }
}
