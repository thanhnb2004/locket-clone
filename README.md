# Locket Clone (Spring Boot + React)

A clone of [Locket](https://locket.camera): users add friends and share photo "moments" that
appear in a friends-only feed. A **Spring Boot monolith** backend (Java 25, HTTP Basic Auth,
PostgreSQL, S3/MinIO image storage) plus a **React frontend** styled like Locket and served by
**Nginx**. Everything runs locally with **Docker Compose**.

## Stack

| Concern        | Choice                                   |
|----------------|------------------------------------------|
| Backend        | Java 25 · Spring Boot 3.5.x (Web, Data JPA, Security, Validation) |
| Auth           | HTTP Basic (username + BCrypt password)  |
| Database       | PostgreSQL 17                            |
| Image storage  | S3-compatible object storage (MinIO local, AWS S3 in prod) |
| Frontend       | React 18 + Vite, plain CSS (Locket-style UI) |
| Web server     | Nginx (serves the SPA + reverse-proxies `/api` to the backend) |
| Run            | Docker Compose                            |

## Frontend

The React app (in `frontend/`) is a mobile-first, Locket-style UI:

- **Login / Register** — credentials are kept as a base64 Basic-Auth token in `localStorage`.
- **Capture** (home) — live webcam viewfinder with a shutter button (or pick from device),
  add a caption, and send.
- **Feed** — your moments and your friends' moments as rounded photo cards.
- **Friends** — add by username, accept/reject incoming requests, see pending ones.

Nginx reverse-proxies `/api` to the backend, so the browser only ever makes **same-origin**
requests — no CORS configuration is needed. Image endpoints require the auth header, so images
are fetched as blobs and shown via object URLs.

## Project layout

Each feature module is split into layers: `entity/`, `repository/`, `controller/`,
`dto/`, and an `interfaces/` package holding the service contract (e.g.
`IFriendshipService`) that the service class implements.

Each module's service contract lives in `service/I<X>Service.java`, with the implementation in
`service/iplm/<X>Service.java`.

```
locket-clone/
├─ docker-compose.yml             # Postgres + MinIO + app
├─ backend/
│  ├─ Dockerfile
│  ├─ pom.xml
│  └─ src/main/java/com/locket/clone/
│     ├─ user/
│     │  ├─ entity/User.java
│     │  ├─ repository/UserRepository.java
│     │  ├─ controller/UserController.java
│     │  ├─ dto/
│     │  └─ service/
│     │     ├─ IUserService.java
│     │     └─ iplm/UserService.java
│     ├─ friendship/
│     │  ├─ entity/{Friendship,FriendRequestStatus}.java
│     │  ├─ repository/FriendshipRepository.java
│     │  ├─ controller/FriendshipController.java
│     │  ├─ dto/
│     │  └─ service/
│     │     ├─ IFriendshipService.java
│     │     └─ iplm/FriendshipService.java
│     ├─ moment/
│     │  ├─ entity/Moment.java
│     │  ├─ repository/MomentRepository.java
│     │  ├─ controller/MomentController.java
│     │  ├─ dto/
│     │  └─ service/
│     │     ├─ IMomentService.java
│     │     └─ iplm/MomentService.java
│     ├─ storage/
│     │  ├─ config/S3Config.java          # S3/MinIO client bean
│     │  └─ service/
│     │     ├─ IStorageService.java
│     │     └─ iplm/StorageService.java   # stores images in S3/MinIO
│     ├─ security/                     # Spring Security config + Basic Auth
│     └─ common/                       # error handling
```

## Run locally with Docker Compose

From the repository root:

```bash
docker compose up --build
```

This starts:
- **web** — the React frontend (Nginx) at **`http://localhost:3000`** ← open this
- **app** — the API on `http://localhost:8080`
- **db** — PostgreSQL on `localhost:5432` (db/user/pass all `locket`)
- **minio** — S3-compatible storage; API on `localhost:9000`, web console on
  `http://localhost:9001` (login `minioadmin` / `minioadmin`). The app auto-creates the
  `locket-moments` bucket on startup.

> **Data is kept inside the containers** — there are no volumes or host mounts (intended for
> dev/test). Everything (Postgres data and uploaded images in MinIO) is wiped when the
> containers are removed:

```bash
docker compose down
```

## Run the backend without Docker

You need JDK 25, a running PostgreSQL, and a running MinIO (or any S3). Then:

```bash
cd backend
# Database (defaults shown)
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/locket
export SPRING_DATASOURCE_USERNAME=locket
export SPRING_DATASOURCE_PASSWORD=locket
# S3 / MinIO (defaults shown; matches `docker compose up minio`)
export APP_S3_ENDPOINT=http://localhost:9000
export APP_S3_BUCKET=locket-moments
export APP_S3_ACCESS_KEY=minioadmin
export APP_S3_SECRET_KEY=minioadmin
export APP_S3_PATH_STYLE_ACCESS=true

mvn spring-boot:run
```

## Run the frontend without Docker

You need Node 18+ and the backend running on `localhost:8080`. Then:

```bash
cd frontend
npm install
npm run dev   # opens http://localhost:5173 (Vite proxies /api to the backend)
```

> **Camera note:** the webcam viewfinder uses `getUserMedia`, which browsers only allow on a
> secure context — i.e. `http://localhost` or HTTPS. If you open the app over a plain-HTTP LAN
> IP, the camera is blocked and the app falls back to the "pick an image" file picker.

## Authentication

Every endpoint except registration requires **HTTP Basic Auth**. Send the username and
password with each request (`Authorization: Basic base64(user:pass)`), e.g. `curl -u user:pass`.

## API

### Users

| Method | Path                  | Auth | Description                |
|--------|-----------------------|------|----------------------------|
| POST   | `/api/users/register` | No   | Create an account          |
| GET    | `/api/users/me`       | Yes  | Current user's profile     |
| GET    | `/api/users/{username}` | Yes | Look up a user             |

### Friends

| Method | Path                                | Auth | Description                  |
|--------|-------------------------------------|------|------------------------------|
| GET    | `/api/friends`                      | Yes  | List accepted friends        |
| POST   | `/api/friends/requests`             | Yes  | Send a friend request        |
| GET    | `/api/friends/requests/incoming`    | Yes  | Pending requests sent to me  |
| GET    | `/api/friends/requests/outgoing`    | Yes  | Pending requests I sent       |
| POST   | `/api/friends/requests/{id}/accept` | Yes  | Accept a request             |
| POST   | `/api/friends/requests/{id}/reject` | Yes  | Reject a request             |

### Moments

| Method | Path                       | Auth | Description                              |
|--------|----------------------------|------|------------------------------------------|
| POST   | `/api/moments`             | Yes  | Post a moment (multipart: `image`, `caption`) |
| GET    | `/api/moments/feed`        | Yes  | My moments + friends' moments, newest first |
| GET    | `/api/moments/mine`        | Yes  | Only my moments                          |
| GET    | `/api/moments/{id}`        | Yes  | A single moment (if visible to me)       |
| GET    | `/api/moments/{id}/image`  | Yes  | The image bytes                          |
| POST   | `/api/moments/{id}/reactions` | Yes | React to a moment (JSON `{ "emoji": "❤️" }`); reacting again changes the emoji |
| DELETE | `/api/moments/{id}/reactions` | Yes | Remove my reaction from a moment        |

Each moment in the feed/`mine`/single responses now carries a `reactions` array (each with the
reacting `user` and `emoji`) and `myReaction` (the emoji the current user reacted with, or
`null`). A user can have at most one reaction per moment.

## Quick walkthrough (curl)

```bash
# 1. Register two users
curl -X POST localhost:8080/api/users/register \
  -H 'Content-Type: application/json' \
  -d '{"username":"alice","password":"secret123","displayName":"Alice"}'

curl -X POST localhost:8080/api/users/register \
  -H 'Content-Type: application/json' \
  -d '{"username":"bob","password":"secret123","displayName":"Bob"}'

# 2. Alice sends Bob a friend request
curl -u alice:secret123 -X POST localhost:8080/api/friends/requests \
  -H 'Content-Type: application/json' \
  -d '{"username":"bob"}'

# 3. Bob sees the incoming request and accepts it (use the returned id)
curl -u bob:secret123 localhost:8080/api/friends/requests/incoming
curl -u bob:secret123 -X POST localhost:8080/api/friends/requests/<REQUEST_ID>/accept

# 4. Alice posts a moment
curl -u alice:secret123 -X POST localhost:8080/api/moments \
  -F 'image=@photo.jpg' \
  -F 'caption=Hello from Alice'

# 5. Bob sees Alice's moment in his feed
curl -u bob:secret123 localhost:8080/api/moments/feed
```

## Notes

- Passwords are hashed with BCrypt; the DB schema is auto-created/updated by Hibernate
  (`spring.jpa.hibernate.ddl-auto=update`) for convenience in local development. For production
  you'd switch to versioned migrations (e.g. Flyway).
- Uploaded images are stored as objects in **S3-compatible storage** (MinIO locally) under a
  random UUID key; only the key, content type, and caption live in Postgres. Moments are only
  visible to the owner and accepted friends.

## Deploying the image storage to AWS S3

The app uses the AWS SDK v2 S3 client, so moving from MinIO to real S3 is config-only:

| Setting | Local (MinIO) | AWS S3 |
|---------|---------------|--------|
| `APP_S3_ENDPOINT` | `http://minio:9000` | *(leave empty — use the default S3 endpoint)* |
| `APP_S3_ACCESS_KEY` / `APP_S3_SECRET_KEY` | `minioadmin` | *(leave empty — use the IAM role / default credentials chain)* |
| `APP_S3_PATH_STYLE_ACCESS` | `true` | `false` |
| `APP_S3_REGION` | `us-east-1` | your bucket's region |
| `APP_S3_BUCKET` | `locket-moments` | your real bucket name |

When the endpoint and keys are empty, the client falls back to AWS's default endpoint and the
default credentials provider chain (env vars, profile, or the ECS/EC2 IAM role).
