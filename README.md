# Social Media App: Spring Boot Microservices + Angular

## Architecture
```
Angular (4200) ──> API Gateway (8080, JWT check) ──┬─> user-service (8081) ─> userdb
                         │                         └─> post-service (8082) ─> postdb
                         └── Eureka discovery (8761) <── all services register here
post-service ──(lb://user-service)──> user-service   (feed = posts of people you follow)
```
- **Security (Spring Security, no OAuth2)**: user-service issues an HS256 JWT (`sub`=user id, `username`, `roles`) signed with `jjwt`.
  The gateway rejects bad tokens early with a plain `GlobalFilter`; user-service and post-service each verify the token again
  with their own `OncePerRequestFilter` and put an `AuthUser` (id, username, roles) in the security context
  (stateless, CSRF off). Controllers use `@AuthenticationPrincipal AuthUser`; authorization uses roles with
  `@EnableMethodSecurity` + `@PreAuthorize`. post-service relays the caller's token when it calls user-service.
- Default admin: `admin` / `Admin@12345` (set `ADMIN_PASSWORD`). Admin can `PUT /api/users/{id}/role` and delete any post.
- **user-service** (Facebook-style): register/login (BCrypt + JWT); profiles (display name, bio, location, avatar, cover,
  birthday) with privacy `PUBLIC / FRIENDS / ONLY_ME`; **friend requests** (send, accept, decline, cancel, unfriend; one row
  per pair, so duplicates/crossed requests are impossible); **mutual friends**; **People you may know**; **block**
  (removes friendship + follows, hides both users from each other); plus follow/unfollow.
  The feed shows posts from friends and followed users.
- **post-service**: posts, feed, likes (toggle), comments, delete own post.

## Run
Requirements: Java 17, Maven, Docker, Node 18+.

Backend (all services + Postgres):
    docker compose up --build

Or without containers: start Postgres (create DBs `userdb`, `postdb` via db/init.sql), then run in order:
discovery-server, user-service, post-service, api-gateway (`mvn -pl <module> spring-boot:run`).

Frontend:
    cd frontend && npm install && npm start     # http://localhost:4200

## API (via gateway, port 8080)
| Method | Path | Notes |
|---|---|---|
| POST | /api/auth/register, /api/auth/login | public |
| GET | /api/users/{id}/profile | profile (respects privacy and blocks) |
| PUT | /api/users/me/profile | edit own profile |
| GET | /api/friends, /api/friends/requests, /api/friends/requests/sent, /api/friends/suggestions | lists |
| POST | /api/friends/requests/{id} | send request (auto-accepts if they already asked you) |
| POST | /api/friends/requests/{id}/accept | accept |
| DELETE | /api/friends/requests/{id} | decline or cancel |
| DELETE | /api/friends/{id} | unfriend |
| GET | /api/friends/mutual/{id} | mutual friend ids |
| GET/POST/DELETE | /api/blocks, /api/blocks/{id} | list / block / unblock |
| GET | /api/users/search?q= | |
| POST/DELETE | /api/users/{id}/follow | |
| POST | /api/posts | `{content}` |
| GET | /api/posts/feed?page= | own + followed |
| GET | /api/posts/user/{id} | |
| POST | /api/posts/{id}/like | toggle |
| GET/POST | /api/posts/{id}/comments | |
| DELETE | /api/posts/{id} | author only |

## Before production
Set `JWT_SECRET` (32+ chars) everywhere, use a secrets manager, add Flyway instead of `ddl-auto: update`,
add rate limiting, and add tests.
Natural next steps: media uploads (S3), notifications via Kafka/RabbitMQ, Redis-cached feeds, refresh tokens.
