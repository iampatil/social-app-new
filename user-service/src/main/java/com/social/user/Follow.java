package com.social.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "follows", uniqueConstraints = @UniqueConstraint(columnNames = {"followerId", "followeeId"}))
@Getter @Setter @NoArgsConstructor
public class Follow {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long followerId;
    private Long followeeId;

    public Follow(Long followerId, Long followeeId) { this.followerId = followerId; this.followeeId = followeeId; }
}
