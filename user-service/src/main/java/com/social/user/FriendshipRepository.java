package com.social.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    @Query("select f from Friendship f where f.userA = :a and f.userB = :b")
    Optional<Friendship> findPair(@Param("a") Long a, @Param("b") Long b);

    List<Friendship> findByAddresseeIdAndStatusOrderByCreatedAtDesc(Long addresseeId, FriendshipStatus status);
    List<Friendship> findByRequesterIdAndStatusOrderByCreatedAtDesc(Long requesterId, FriendshipStatus status);

    @Query("select f from Friendship f where f.status = com.social.user.FriendshipStatus.ACCEPTED and (f.userA = :id or f.userB = :id)")
    List<Friendship> findFriendships(@Param("id") Long id);

    @Query("select f from Friendship f where f.status = com.social.user.FriendshipStatus.ACCEPTED and (f.userA in :ids or f.userB in :ids)")
    List<Friendship> findFriendshipsOfAny(@Param("ids") Collection<Long> ids);
}
