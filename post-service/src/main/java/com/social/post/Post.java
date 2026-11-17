package com.social.post;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Instant;

@Entity
@Table(name = "posts", indexes = @Index(columnList = "authorId"))
@Getter @Setter @NoArgsConstructor
public class Post {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long authorId;
    private String authorUsername;
    @Column(nullable = false, length = 1000)
    private String content;
    private Instant createdAt = Instant.now();
}
