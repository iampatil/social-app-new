package com.social.post;

import com.social.post.security.AuthUser;
import org.springframework.stereotype.Component;

/** Custom authorization rules, used from @PreAuthorize("@postAuth....(#id, principal)") */
@Component("postAuth")
public class PostAuth {
    private final PostRepository posts;

    public PostAuth(PostRepository posts) { this.posts = posts; }

    /** Author or ADMIN may delete. A missing post passes so the controller can return 404. */
    public boolean canDelete(Long postId, AuthUser user) {
        if (user.isAdmin()) return true;
        return posts.findById(postId).map(p -> p.getAuthorId().equals(user.getId())).orElse(true);
    }
}
