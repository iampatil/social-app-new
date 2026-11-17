package com.social.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "users")
@Getter @Setter @NoArgsConstructor
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String username;
    @Column(unique = true, nullable = false)
    private String email;
    @Column(nullable = false)
    private String passwordHash;
    @Column(nullable = false)
    private String role = "ROLE_USER";

    // ---- profile ----
    private String displayName;
    @Column(length = 500) private String bio;
    private String location;
    @Column(length = 500) private String avatarUrl;
    @Column(length = 500) private String coverUrl;
    private LocalDate birthday;
    @Enumerated(EnumType.STRING)
    private Visibility visibility = Visibility.PUBLIC;

    private Instant createdAt = Instant.now();

    public String label() { return displayName == null || displayName.isBlank() ? username : displayName; }
    public Visibility effectiveVisibility() { return visibility == null ? Visibility.PUBLIC : visibility; }
}
