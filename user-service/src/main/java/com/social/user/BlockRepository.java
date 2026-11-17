package com.social.user;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface BlockRepository extends JpaRepository<UserBlock, Long> {
    boolean existsByBlockerIdAndBlockedId(Long blockerId, Long blockedId);
    List<UserBlock> findByBlockerId(Long blockerId);
    @Transactional void deleteByBlockerIdAndBlockedId(Long blockerId, Long blockedId);

    @Query("select count(b) > 0 from UserBlock b where (b.blockerId = :a and b.blockedId = :b) or (b.blockerId = :b and b.blockedId = :a)")
    boolean blockedEitherWay(@Param("a") Long a, @Param("b") Long b);

    /** Everyone I blocked plus everyone who blocked me. */
    @Query("select case when b.blockerId = :id then b.blockedId else b.blockerId end from UserBlock b where b.blockerId = :id or b.blockedId = :id")
    List<Long> findRelatedIds(@Param("id") Long id);
}
