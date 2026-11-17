package com.social.user;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.social.user.security.AuthUser;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasAnyRole('USER','ADMIN')")
public class UserController {

    public record UserDto(Long id, String username, String displayName, String avatarUrl, String bio, String role,
                          long followers, long following, boolean followedByMe, Relationship relationship) {}
    public record RoleRequest(String role) {}

    private final UserRepository users;
    private final FollowRepository follows;
    private final FriendService friends;

    public UserController(UserRepository users, FollowRepository follows, FriendService friends) {
        this.users = users; this.follows = follows; this.friends = friends;
    }

    private UserDto toDto(User u, Long me) {
        return new UserDto(u.getId(), u.getUsername(), u.label(), u.getAvatarUrl(), u.getBio(), u.getRole(),
                follows.countByFolloweeId(u.getId()), follows.countByFollowerId(u.getId()),
                follows.existsByFollowerIdAndFolloweeId(me, u.getId()), friends.relationship(me, u.getId()));
    }

    private User find(Long id) {
        return users.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/me")
    public UserDto me(@AuthenticationPrincipal AuthUser auth) { return toDto(find(auth.getId()), auth.getId()); }

    @GetMapping("/search")
    public List<UserDto> search(@AuthenticationPrincipal AuthUser auth, @RequestParam(defaultValue = "") String q) {
        Long me = auth.getId();
        Set<Long> blocked = friends.blockedIds(me);
        return users.findTop20ByUsernameContainingIgnoreCaseOrDisplayNameContainingIgnoreCase(q, q).stream()
                .filter(u -> !u.getId().equals(me) && !blocked.contains(u.getId()))
                .map(u -> toDto(u, me)).toList();
    }

    @GetMapping("/{id}")
    public UserDto get(@AuthenticationPrincipal AuthUser auth, @PathVariable Long id) {
        Long me = auth.getId();
        if (!me.equals(id) && friends.isBlockedEitherWay(me, id)) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return toDto(find(id), me);
    }

    @PostMapping("/{id}/follow")
    public UserDto follow(@AuthenticationPrincipal AuthUser auth, @PathVariable Long id) {
        Long me = auth.getId();
        if (me.equals(id)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot follow yourself");
        User target = find(id);
        if (friends.isBlockedEitherWay(me, id)) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        if (!follows.existsByFollowerIdAndFolloweeId(me, id)) follows.save(new Follow(me, id));
        return toDto(target, me);
    }

    @DeleteMapping("/{id}/follow")
    public UserDto unfollow(@AuthenticationPrincipal AuthUser auth, @PathVariable Long id) {
        User target = find(id);
        follows.deleteByFollowerIdAndFolloweeId(auth.getId(), id);
        return toDto(target, auth.getId());
    }

    /** Service-to-service (JWT relayed by post-service): friends + followed users, minus blocked. Owner or ADMIN only. */
    @GetMapping("/{id}/connection-ids")
    @PreAuthorize("#id == principal.id or hasRole('ADMIN')")
    public List<Long> connectionIds(@PathVariable Long id) { return List.copyOf(friends.connectionIds(id)); }

    @GetMapping("/{id}/following-ids")
    @PreAuthorize("#id == principal.id or hasRole('ADMIN')")
    public List<Long> followingIds(@PathVariable Long id) { return follows.findFolloweeIds(id); }

    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public UserDto changeRole(@AuthenticationPrincipal AuthUser auth, @PathVariable Long id, @RequestBody RoleRequest r) {
        String role = "ROLE_" + r.role().toUpperCase();
        if (!Set.of("ROLE_USER", "ROLE_ADMIN").contains(role))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role must be USER or ADMIN");
        User u = find(id);
        u.setRole(role);
        return toDto(users.save(u), auth.getId());
    }
}
