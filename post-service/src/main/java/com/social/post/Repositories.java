package com.social.post;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Collection;
import java.util.List;

interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByAuthorIdInOrderByCreatedAtDesc(Collection<Long> authorIds, Pageable page);
    List<Post> findByAuthorIdOrderByCreatedAtDesc(Long authorId, Pageable page);
}

interface LikeRepository extends JpaRepository<PostLike, Long> {
    long countByPostId(Long postId);
    boolean existsByPostIdAndUserId(Long postId, Long userId);
    @Transactional void deleteByPostIdAndUserId(Long postId, Long userId);
    @Transactional void deleteByPostId(Long postId);
}

interface CommentRepository extends JpaRepository<Comment, Long> {
    long countByPostId(Long postId);
    List<Comment> findByPostIdOrderByCreatedAtAsc(Long postId);
    @Transactional void deleteByPostId(Long postId);
}
