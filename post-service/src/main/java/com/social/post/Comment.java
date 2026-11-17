package com.social.post;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Instant;

@Entity
@Table(name = "comments", indexes = @Index(columnList = "postId"))
@Getter @Setter @NoArgsConstructor
public class Comment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long postId;
    private Long authorId;
    private String authorUsername;
    @Column(nullable = false, length = 500)
    private String text;
    private Instant createdAt = Instant.now();
}
