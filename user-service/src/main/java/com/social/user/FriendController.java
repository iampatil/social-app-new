package com.social.user;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.social.user.security.AuthUser;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@PreAuthorize("hasAnyRole('USER','ADMIN')")
public class FriendController {

    private final FriendService friends;

    public FriendController(FriendService friends) { this.friends = friends; }

    @GetMapping("/friends")
    public List<FriendService.UserCard> friends(@AuthenticationPrincipal AuthUser auth) { return friends.friends(auth.getId()); }

    @GetMapping("/friends/requests")
    public List<FriendService.UserCard> incoming(@AuthenticationPrincipal AuthUser auth) { return friends.incoming(auth.getId()); }

    @GetMapping("/friends/requests/sent")
    public List<FriendService.UserCard> outgoing(@AuthenticationPrincipal AuthUser auth) { return friends.outgoing(auth.getId()); }

    @GetMapping("/friends/suggestions")
    public List<FriendService.UserCard> suggestions(@AuthenticationPrincipal AuthUser auth,
                                                    @RequestParam(defaultValue = "10") int limit) {
        return friends.suggestions(auth.getId(), Math.min(Math.max(limit, 1), 30));
    }

    @PostMapping("/friends/requests/{userId}")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Relationship> send(@AuthenticationPrincipal AuthUser auth, @PathVariable Long userId) {
        return Map.of("relationship", friends.sendRequest(auth.getId(), userId));
    }

    @PostMapping("/friends/requests/{userId}/accept")
    public Map<String, Relationship> accept(@AuthenticationPrincipal AuthUser auth, @PathVariable Long userId) {
        friends.accept(auth.getId(), userId);
        return Map.of("relationship", Relationship.FRIENDS);
    }

    /** Decline an incoming request or cancel one you sent. */
    @DeleteMapping("/friends/requests/{userId}")
    public Map<String, Relationship> declineOrCancel(@AuthenticationPrincipal AuthUser auth, @PathVariable Long userId) {
        friends.declineOrCancel(auth.getId(), userId);
        return Map.of("relationship", Relationship.NONE);
    }

    @DeleteMapping("/friends/{userId}")
    public Map<String, Relationship> unfriend(@AuthenticationPrincipal AuthUser auth, @PathVariable Long userId) {
        friends.unfriend(auth.getId(), userId);
        return Map.of("relationship", Relationship.NONE);
    }

    @GetMapping("/friends/mutual/{userId}")
    public List<Long> mutual(@AuthenticationPrincipal AuthUser auth, @PathVariable Long userId) {
        return List.copyOf(friends.mutualFriendIds(auth.getId(), userId));
    }

    @GetMapping("/blocks")
    public List<FriendService.UserCard> blocked(@AuthenticationPrincipal AuthUser auth) { return friends.blockedUsers(auth.getId()); }

    @PostMapping("/blocks/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void block(@AuthenticationPrincipal AuthUser auth, @PathVariable Long userId) { friends.block(auth.getId(), userId); }

    @DeleteMapping("/blocks/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unblock(@AuthenticationPrincipal AuthUser auth, @PathVariable Long userId) { friends.unblock(auth.getId(), userId); }
}
