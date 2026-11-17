package com.social.post;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.social.post.security.AuthUser;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/posts")
@PreAuthorize("hasAnyRole('USER','ADMIN')")
public class PostController {

    public record CreatePost(@NotBlank @Size(max = 1000) String content) {}
    public record CreateComment(@NotBlank @Size(max = 500) String text) {}
    public record PostDto(Long id, Long authorId, String authorUsername, String content, Instant createdAt,
                          long likeCount, boolean likedByMe, long commentCount) {}
    public record CommentDto(Long id, Long authorId, String authorUsername, String text, Instant createdAt) {}
    public record LikeState(long likeCount, boolean likedByMe) {}

    private final PostRepository posts;
    private final LikeRepository likes;
    private final CommentRepository comments;
    private final UserClient userClient;

    public PostController(PostRepository posts, LikeRepository likes, CommentRepository comments, UserClient userClient) {
        this.posts = posts; this.likes = likes; this.comments = comments; this.userClient = userClient;
    }

    private PostDto toDto(Post p, Long me) {
        return new PostDto(p.getId(), p.getAuthorId(), p.getAuthorUsername(), p.getContent(), p.getCreatedAt(),
                likes.countByPostId(p.getId()), likes.existsByPostIdAndUserId(p.getId(), me),
                comments.countByPostId(p.getId()));
    }

    private Post find(Long id) {
        return posts.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public PostDto create(@AuthenticationPrincipal AuthUser auth, @Valid @RequestBody CreatePost r) {
        Post p = new Post();
        p.setAuthorId(auth.getId());
        p.setAuthorUsername(auth.getUsername());
        p.setContent(r.content());
        return toDto(posts.save(p), auth.getId());
    }

    @GetMapping("/feed")
    public List<PostDto> feed(@AuthenticationPrincipal AuthUser auth, @RequestParam(defaultValue = "0") int page) {
        Long me = auth.getId();
        List<Long> ids = new ArrayList<>(userClient.followingIds(me));
        ids.add(me);
        return posts.findByAuthorIdInOrderByCreatedAtDesc(ids, PageRequest.of(page, 20)).stream()
                .map(p -> toDto(p, me)).toList();
    }

    @GetMapping("/user/{userId}")
    public List<PostDto> byUser(@AuthenticationPrincipal AuthUser auth, @PathVariable Long userId,
                                @RequestParam(defaultValue = "0") int page) {
        return posts.findByAuthorIdOrderByCreatedAtDesc(userId, PageRequest.of(page, 20)).stream()
                .map(p -> toDto(p, auth.getId())).toList();
    }

    /** Author or ADMIN (moderation). */
    @DeleteMapping("/{id}")
    @PreAuthorize("@postAuth.canDelete(#id, principal)")
    @Transactional
    public void delete(@PathVariable Long id) {
        Post p = find(id);
        likes.deleteByPostId(id);
        comments.deleteByPostId(id);
        posts.delete(p);
    }

    @PostMapping("/{id}/like")
    public LikeState toggleLike(@AuthenticationPrincipal AuthUser auth, @PathVariable Long id) {
        find(id);
        Long me = auth.getId();
        boolean liked;
        if (likes.existsByPostIdAndUserId(id, me)) { likes.deleteByPostIdAndUserId(id, me); liked = false; }
        else { likes.save(new PostLike(id, me)); liked = true; }
        return new LikeState(likes.countByPostId(id), liked);
    }

    @GetMapping("/{id}/comments")
    public List<CommentDto> listComments(@PathVariable Long id) {
        return comments.findByPostIdOrderByCreatedAtAsc(id).stream()
                .map(c -> new CommentDto(c.getId(), c.getAuthorId(), c.getAuthorUsername(), c.getText(), c.getCreatedAt()))
                .toList();
    }

    @PostMapping("/{id}/comments")
    public CommentDto addComment(@AuthenticationPrincipal AuthUser auth, @PathVariable Long id,
                                 @Valid @RequestBody CreateComment r) {
        find(id);
        Comment c = new Comment();
        c.setPostId(id);
        c.setAuthorId(auth.getId());
        c.setAuthorUsername(auth.getUsername());
        c.setText(r.text());
        comments.save(c);
        return new CommentDto(c.getId(), c.getAuthorId(), c.getAuthorUsername(), c.getText(), c.getCreatedAt());
    }
}
