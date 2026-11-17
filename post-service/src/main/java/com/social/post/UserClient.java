package com.social.post;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

/** Calls user-service by name (Eureka) and relays the caller's JWT, so user-service authorizes it too. */
@Component
public class UserClient {
    private final RestClient client;

    public UserClient(RestClient.Builder lbBuilder) {
        this.client = lbBuilder.baseUrl("http://user-service").build();
    }

    public List<Long> followingIds(Long userId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getCredentials() instanceof String token)) return List.of();
        try {
            List<Long> ids = client.get().uri("/api/users/{id}/connection-ids", userId)
                    .headers(h -> h.setBearerAuth(token))
                    .retrieve().body(new ParameterizedTypeReference<List<Long>>() {});
            return ids == null ? List.of() : ids;
        } catch (Exception e) {
            return List.of();
        }
    }
}
