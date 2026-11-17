package com.social.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Instant;

/** One row per pair of users. (userA, userB) = (min id, max id) so a pair can never be duplicated in either direction. */
@Entity
@Table(name = "friendships", uniqueConstraints = @UniqueConstraint(columnNames = {"userA", "userB"}))
@Getter @Setter @NoArgsConstructor
public class Friendship {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userA;
    private Long userB;
    private Long requesterId;
    private Long addresseeId;
    @Enumerated(EnumType.STRING)
    private FriendshipStatus status = FriendshipStatus.PENDING;
    private Instant createdAt = Instant.now();
    private Instant respondedAt;

    public Friendship(Long requesterId, Long addresseeId) {
        this.requesterId = requesterId;
        this.addresseeId = addresseeId;
        this.userA = Math.min(requesterId, addresseeId);
        this.userB = Math.max(requesterId, addresseeId);
    }

    public Long other(Long me) { return me.equals(requesterId) ? addresseeId : requesterId; }
}
