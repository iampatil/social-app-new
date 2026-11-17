package com.social.user;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/** Facebook-style social graph: friend requests, friendships, mutual friends, suggestions, blocking. */
@Service
public class FriendService {

    public record UserCard(Long id, String username, String displayName, String avatarUrl, int mutualFriends) {}

    private final UserRepository users;
    private final FriendshipRepository friendships;
    private final BlockRepository blocks;
    private final FollowRepository follows;

    public FriendService(UserRepository users, FriendshipRepository friendships, BlockRepository blocks, FollowRepository follows) {
        this.users = users; this.friendships = friendships; this.blocks = blocks; this.follows = follows;
    }

    private static ResponseStatusException err(HttpStatus s, String m) { return new ResponseStatusException(s, m); }

    private User user(Long id) { return users.findById(id).orElseThrow(() -> err(HttpStatus.NOT_FOUND, "User not found")); }

    private Optional<Friendship> pair(Long a, Long b) { return friendships.findPair(Math.min(a, b), Math.max(a, b)); }

    private UserCard card(User u, int mutual) { return new UserCard(u.getId(), u.getUsername(), u.label(), u.getAvatarUrl(), mutual); }

    // ---------- queries ----------

    public Set<Long> friendIds(Long id) {
        return friendships.findFriendships(id).stream().map(f -> f.other(id)).collect(Collectors.toCollection(HashSet::new));
    }

    public Set<Long> blockedIds(Long id) { return new HashSet<>(blocks.findRelatedIds(id)); }

    public boolean isBlockedEitherWay(Long a, Long b) { return blocks.blockedEitherWay(a, b); }

    public Set<Long> mutualFriendIds(Long a, Long b) {
        Set<Long> s = friendIds(a);
        s.retainAll(friendIds(b));
        return s;
    }

    public Relationship relationship(Long me, Long other) {
        if (me.equals(other)) return Relationship.SELF;
        return pair(me, other)
                .map(f -> f.getStatus() == FriendshipStatus.ACCEPTED ? Relationship.FRIENDS
                        : f.getRequesterId().equals(me) ? Relationship.REQUEST_SENT : Relationship.REQUEST_RECEIVED)
                .orElse(Relationship.NONE);
    }

    /** Friends + people I follow, minus blocked users. Used by post-service to build the feed. */
    public Set<Long> connectionIds(Long me) {
        Set<Long> ids = friendIds(me);
        ids.addAll(follows.findFolloweeIds(me));
        ids.removeAll(blockedIds(me));
        return ids;
    }

    // ---------- friend requests ----------

    /** Sends a request. If the other person already asked me, both become friends immediately. */
    @Transactional
    public Relationship sendRequest(Long me, Long target) {
        if (me.equals(target)) throw err(HttpStatus.BAD_REQUEST, "You cannot add yourself");
        user(target);
        if (isBlockedEitherWay(me, target)) throw err(HttpStatus.NOT_FOUND, "User not found");
        Optional<Friendship> existing = pair(me, target);
        if (existing.isPresent()) {
            Friendship f = existing.get();
            if (f.getStatus() == FriendshipStatus.ACCEPTED) throw err(HttpStatus.CONFLICT, "Already friends");
            if (f.getRequesterId().equals(me)) throw err(HttpStatus.CONFLICT, "Request already sent");
            f.setStatus(FriendshipStatus.ACCEPTED);
            f.setRespondedAt(Instant.now());
            friendships.save(f);
            return Relationship.FRIENDS;
        }
        friendships.save(new Friendship(me, target));
        return Relationship.REQUEST_SENT;
    }

    @Transactional
    public void accept(Long me, Long requesterId) {
        Friendship f = pair(me, requesterId)
                .filter(x -> x.getStatus() == FriendshipStatus.PENDING && x.getAddresseeId().equals(me))
                .orElseThrow(() -> err(HttpStatus.NOT_FOUND, "No pending request from this user"));
        f.setStatus(FriendshipStatus.ACCEPTED);
        f.setRespondedAt(Instant.now());
        friendships.save(f);
    }

    /** Declines an incoming request or cancels an outgoing one. */
    @Transactional
    public void declineOrCancel(Long me, Long other) {
        Friendship f = pair(me, other).filter(x -> x.getStatus() == FriendshipStatus.PENDING)
                .orElseThrow(() -> err(HttpStatus.NOT_FOUND, "No pending request"));
        friendships.delete(f);
    }

    @Transactional
    public void unfriend(Long me, Long other) {
        Friendship f = pair(me, other).filter(x -> x.getStatus() == FriendshipStatus.ACCEPTED)
                .orElseThrow(() -> err(HttpStatus.NOT_FOUND, "Not friends"));
        friendships.delete(f);
    }

    // ---------- lists ----------

    public List<UserCard> friends(Long me) { return cards(friendIds(me), me, false); }

    public List<UserCard> incoming(Long me) {
        return cards(friendships.findByAddresseeIdAndStatusOrderByCreatedAtDesc(me, FriendshipStatus.PENDING).stream()
                .map(Friendship::getRequesterId).toList(), me, true);
    }

    public List<UserCard> outgoing(Long me) {
        return cards(friendships.findByRequesterIdAndStatusOrderByCreatedAtDesc(me, FriendshipStatus.PENDING).stream()
                .map(Friendship::getAddresseeId).toList(), me, true);
    }

    public List<UserCard> blockedUsers(Long me) {
        return cards(blocks.findByBlockerId(me).stream().map(UserBlock::getBlockedId).toList(), me, false);
    }

    private List<UserCard> cards(Collection<Long> ids, Long me, boolean withMutual) {
        Set<Long> mine = withMutual ? friendIds(me) : Set.of();
        Map<Long, User> byId = users.findAllById(ids).stream().collect(Collectors.toMap(User::getId, u -> u));
        List<UserCard> out = new ArrayList<>();
        for (Long id : ids) {
            User u = byId.get(id);
            if (u == null) continue;
            int mutual = 0;
            if (withMutual) { Set<Long> s = friendIds(id); s.retainAll(mine); mutual = s.size(); }
            out.add(card(u, mutual));
        }
        return out;
    }

    /** "People you may know": friends-of-friends ranked by mutual friend count, topped up with newest users. */
    public List<UserCard> suggestions(Long me, int limit) {
        Set<Long> friends = friendIds(me);
        Set<Long> exclude = new HashSet<>(friends);
        exclude.add(me);
        exclude.addAll(blockedIds(me));
        friendships.findByAddresseeIdAndStatusOrderByCreatedAtDesc(me, FriendshipStatus.PENDING).forEach(f -> exclude.add(f.getRequesterId()));
        friendships.findByRequesterIdAndStatusOrderByCreatedAtDesc(me, FriendshipStatus.PENDING).forEach(f -> exclude.add(f.getAddresseeId()));

        Map<Long, Integer> score = new HashMap<>();
        if (!friends.isEmpty()) {
            for (Friendship f : friendships.findFriendshipsOfAny(friends)) {
                Long a = f.getUserA(), b = f.getUserB();
                if (friends.contains(a) && !exclude.contains(b)) score.merge(b, 1, Integer::sum);
                if (friends.contains(b) && !exclude.contains(a)) score.merge(a, 1, Integer::sum);
            }
        }
        List<Long> ranked = score.entrySet().stream()
                .sorted(Map.Entry.<Long, Integer>comparingByValue().reversed())
                .limit(limit).map(Map.Entry::getKey).toList();
        Map<Long, User> byId = users.findAllById(ranked).stream().collect(Collectors.toMap(User::getId, u -> u));
        List<UserCard> out = new ArrayList<>();
        for (Long id : ranked) if (byId.containsKey(id)) out.add(card(byId.get(id), score.get(id)));

        if (out.size() < limit) {
            for (User u : users.findTop50ByOrderByCreatedAtDesc()) {
                if (out.size() >= limit) break;
                if (exclude.contains(u.getId()) || score.containsKey(u.getId())) continue;
                out.add(card(u, 0));
            }
        }
        return out;
    }

    // ---------- blocking ----------

    /** Blocking removes any friendship, pending request and follows in both directions. */
    @Transactional
    public void block(Long me, Long target) {
        if (me.equals(target)) throw err(HttpStatus.BAD_REQUEST, "You cannot block yourself");
        user(target);
        if (!blocks.existsByBlockerIdAndBlockedId(me, target)) blocks.save(new UserBlock(me, target));
        pair(me, target).ifPresent(friendships::delete);
        follows.deleteByFollowerIdAndFolloweeId(me, target);
        follows.deleteByFollowerIdAndFolloweeId(target, me);
    }

    @Transactional
    public void unblock(Long me, Long target) { blocks.deleteByBlockerIdAndBlockedId(me, target); }
}
