package com.social.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_blocks", uniqueConstraints = @UniqueConstraint(columnNames = {"blockerId", "blockedId"}))
@Getter @Setter @NoArgsConstructor
public class UserBlock {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long blockerId;
    private Long blockedId;

    public UserBlock(Long blockerId, Long blockedId) { this.blockerId = blockerId; this.blockedId = blockedId; }
}
