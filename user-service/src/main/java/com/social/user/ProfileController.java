package com.social.user;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.social.user.security.AuthUser;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

/** Facebook-style profile pages with privacy (PUBLIC / FRIENDS / ONLY_ME). */
@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasAnyRole('USER','ADMIN')")
public class ProfileController {

    public record UpdateProfile(
            @Size(max = 60) String displayName,
            @Size(max = 500) String bio,
            @Size(max = 100) String location,
            @Size(max = 500) @Pattern(regexp = "^(https?://.*)?$", message = "must be an http(s) URL") String avatarUrl,
            @Size(max = 500) @Pattern(regexp = "^(https?://.*)?$", message = "must be an http(s) URL") String coverUrl,
            @Past LocalDate birthday,
            Visibility visibility) {}

    public record ProfileDto(Long id, String username, String displayName, String avatarUrl, String coverUrl,
                             String bio, String location, LocalDate birthday, Visibility visibility,
                             int friendCount, long followers, long following, int mutualFriends,
                             Relationship relationship, boolean followedByMe, boolean restricted) {}

    private final UserRepository users;
    private final FollowRepository follows;
    private final FriendService friends;

    public ProfileController(UserRepository users, FollowRepository follows, FriendService friends) {
        this.users = users; this.follows = follows; this.friends = friends;
    }

    @GetMapping("/{id}/profile")
    public ProfileDto profile(@AuthenticationPrincipal AuthUser auth, @PathVariable Long id) {
        Long me = auth.getId();
        User u = users.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!me.equals(id) && friends.isBlockedEitherWay(me, id)) throw new ResponseStatusException(HttpStatus.NOT_FOUND);

        Relationship rel = friends.relationship(me, id);
        Visibility v = u.effectiveVisibility();
        boolean full = rel == Relationship.SELF || v == Visibility.PUBLIC
                || (v == Visibility.FRIENDS && rel == Relationship.FRIENDS);
        int mutual = rel == Relationship.SELF ? 0 : friends.mutualFriendIds(me, id).size();

        return new ProfileDto(u.getId(), u.getUsername(), u.label(), u.getAvatarUrl(),
                full ? u.getCoverUrl() : null, full ? u.getBio() : null, full ? u.getLocation() : null,
                full ? u.getBirthday() : null, v,
                full ? friends.friendIds(id).size() : 0,
                follows.countByFolloweeId(id), follows.countByFollowerId(id), mutual, rel,
                follows.existsByFollowerIdAndFolloweeId(me, id), !full);
    }

    @PutMapping("/me/profile")
    public ProfileDto update(@AuthenticationPrincipal AuthUser auth, @Valid @RequestBody UpdateProfile r) {
        Long me = auth.getId();
        User u = users.findById(me).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        u.setDisplayName(r.displayName() == null || r.displayName().isBlank() ? u.getUsername() : r.displayName().trim());
        u.setBio(r.bio());
        u.setLocation(r.location());
        u.setAvatarUrl(r.avatarUrl() == null || r.avatarUrl().isBlank() ? null : r.avatarUrl());
        u.setCoverUrl(r.coverUrl() == null || r.coverUrl().isBlank() ? null : r.coverUrl());
        u.setBirthday(r.birthday());
        if (r.visibility() != null) u.setVisibility(r.visibility());
        users.save(u);
        return profile(auth, me);
    }
}
