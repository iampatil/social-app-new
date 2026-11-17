package com.social.user;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface FollowRepository extends JpaRepository<Follow, Long> {
    boolean existsByFollowerIdAndFolloweeId(Long followerId, Long followeeId);
    long countByFolloweeId(Long id);
    long countByFollowerId(Long id);
    @Transactional
    void deleteByFollowerIdAndFolloweeId(Long followerId, Long followeeId);
    @Query("select f.followeeId from Follow f where f.followerId = :id")
    List<Long> findFolloweeIds(@Param("id") Long id);
}
