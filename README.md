# CP Hub

I built CP Hub to encourage the competitive programming culture in my college. Users connect their Codeforces, LeetCode and CodeChef profiles, and CP Hub ranks them against each other on a shared leaderboard. There's also a blog section for discussing contests and problems.

**Live:** [cp-hub-frontend.vercel.app](https://cp-hub-frontend.vercel.app)

This repository is the **backend** (Spring Boot REST API). The frontend lives in [cp-hub-frontend](https://github.com/preetishramola/cp-hub-frontend).

## Features

- **One profile, three platforms:** link your Codeforces, LeetCode and CodeChef handles.
- **Leaderboards:** a global ranking plus one per platform, showing current rating, max rating, problems solved and score.
- **Automatic stats tracking:** stats are fetched from each platform, cached for an hour, and refreshed for everyone every 6 hours. Users can also refresh on demand.
- **Blogs:** write posts in Markdown, upvote them, and discuss them in threaded comments.
- **Profiles:** logged-in users can view `/profile/<username>` with another user's stats and blogs.
- **JWT authentication:** stateless login, with passwords hashed using BCrypt.

## Tech stack

| Layer | Technology |
|---|---|
| Backend | Java 21, Spring Boot 4, Spring Security, Spring Data JPA |
| Auth | JWT (jjwt), BCrypt |
| Database | PostgreSQL |
| Scraping | Jsoup (CodeChef has no public API) |
| Frontend | Next.js 16, React 19, Tailwind CSS 4 |
| Hosting | Render (backend, Docker), Neon (Postgres), Vercel (frontend) |

## How it works

```
Next.js (Vercel)  ──HTTPS + JWT──▶  Spring Boot API (Render)  ──▶  PostgreSQL (Neon)
                                           │
                                           ├──▶ Codeforces API   (rating, max rating, solved)
                                           ├──▶ LeetCode GraphQL (contest rating, solved, rank)
                                           └──▶ CodeChef profile page, scraped (rating, max rating, solved)
```

Stats are stored per user and platform in `platform_stats`. A request is served from that cache if it's under an hour old, otherwise the platform is queried again. A scheduled job refreshes every connected handle every 6 hours.

### Scoring

| Board | Score |
|---|---|
| Codeforces | `rating × 0.7 + solved × 0.3` |
| CodeChef | `rating × 0.7 + solved × 0.3` |
| LeetCode | `solved` (contest rating is shown but not scored, since many users never do contests) |
| Global | average of the platform scores for the platforms the user has connected |

The global score uses an average so that users with only one platform connected aren't penalised. Users with equal scores share a rank.

## API

The base URL is the backend's address, e.g. `http://localhost:8080`. 🔒 means the endpoint needs an `Authorization: Bearer <token>` header.

### Auth
| Method | Path | Description |
|---|---|---|
| POST | `/auth/signup` | Create an account (name, username, email, password, optional handles); returns a JWT |
| POST | `/auth/login` | Log in with email and password; returns a JWT |

### Users
| Method | Path | Description |
|---|---|---|
| GET 🔒 | `/users/me` | Your profile |
| PUT 🔒 | `/users/me` | Update your name and platform handles |
| GET 🔒 | `/users/{username}` | Another user's profile |

### Stats tracker
| Method | Path | Description |
|---|---|---|
| GET 🔒 | `/tracker/stats` | Your stats on every connected platform |
| POST 🔒 | `/tracker/refresh/{platform}` | Force-refresh one platform (`CODEFORCES`, `LEETCODE`, `CODECHEF`) |

### Leaderboard
| Method | Path | Description |
|---|---|---|
| GET | `/leaderboard` | All four boards in one response |
| GET | `/leaderboard/global` | Global board only |
| GET | `/leaderboard/codeforces` | Codeforces board only |
| GET | `/leaderboard/leetcode` | LeetCode board only |
| GET | `/leaderboard/codechef` | CodeChef board only |

### Blogs
| Method | Path | Description |
|---|---|---|
| GET | `/blogs` | All blogs |
| GET | `/blogs/{id}` | One blog with its comments |
| GET | `/blogs/user/{username}` | Blogs by a user |
| POST 🔒 | `/blogs` | Create a blog |
| PUT 🔒 | `/blogs/{id}` | Edit your blog |
| DELETE 🔒 | `/blogs/{id}` | Delete your blog |
| POST 🔒 | `/blogs/{id}/upvote` | Toggle your upvote |
| POST 🔒 | `/blogs/{id}/comments` | Comment, or reply by passing `parentId` |
| DELETE 🔒 | `/blogs/comments/{commentId}` | Delete your comment |

### Health
| Method | Path | Description |
|---|---|---|
| GET | `/health` | Returns `{"status":"ok"}` without touching the database, for uptime monitors |

## Running locally

**You'll need:** Java 21 and PostgreSQL. Maven comes bundled as `./mvnw`.

1. Create a database:
   ```bash
   createdb campusrank
   ```
2. Set the environment variables (see `.env.example`):
   ```bash
   export DB_URL=jdbc:postgresql://localhost:5432/campusrank
   export DB_USERNAME=postgres
   export DB_PASSWORD=your_local_password
   export JWT_SECRET=$(openssl rand -base64 48)
   ```
3. Start the API:
   ```bash
   ./mvnw spring-boot:run
   ```

The API runs on `http://localhost:8080`, and the tables are created automatically on first start. To run the frontend against it, see [cp-hub-frontend](https://github.com/preetishramola/cp-hub-frontend).

## Configuration

All configuration comes from environment variables. Never commit real values.

| Variable | Required | Default | Description |
|---|---|---|---|
| `DB_URL` | yes | `jdbc:postgresql://localhost:5432/campusrank` | JDBC URL of the Postgres database |
| `DB_USERNAME` | yes | `postgres` | Database user |
| `DB_PASSWORD` | yes | | Database password |
| `JWT_SECRET` | yes | | Secret for signing tokens, at least 32 characters |
| `JWT_EXPIRATION` | no | `86400000` (24 h) | Token lifetime in milliseconds |
| `CORS_ALLOWED_ORIGINS` | no | `http://localhost:3000,https://cp-hub-frontend.vercel.app` | Comma-separated frontend URLs allowed to call the API |
| `PORT` | no | `8080` | Port to listen on (set automatically by Render) |
| `SHOW_SQL` | no | `false` | Log SQL statements |

## Deployment

The live site runs entirely on free tiers:

1. **Database (Neon):** create a Postgres project and turn its connection string into a JDBC URL: `jdbc:postgresql://<host>/<db>?sslmode=require`.
2. **Backend (Render):** create a web service from this repo using the included `Dockerfile`, then set the environment variables above. The Dockerfile limits the Java heap to fit Render's 512 MB free instance.
3. **Frontend (Vercel):** deploy `cp-hub-frontend` with `NEXT_PUBLIC_API_URL` set to the Render URL.
4. **Keep-alive (optional):** have UptimeRobot ping `/health` every 5 minutes, so Render's free server doesn't sleep and the 6-hourly refresh keeps running.

## Project structure

```
src/main/java/com/example/cp/hub/
├── Config/        Security, JWT filter chain, CORS
├── Controller/    REST endpoints
├── DTO/           Request and response objects
├── Exception/     Global error handler
├── Repository/    Spring Data JPA repositories
├── Service/       Business logic (users, blogs, tracker, leaderboard)
├── jwt/           Token creation and validation
├── model/         JPA entities
└── tracker/       Codeforces, LeetCode and CodeChef clients
```

## Known limitations

- **CodeChef scraping can break:** CodeChef stats come from scraping the profile page, so changes to CodeChef's HTML can break them. Solved counts are currently unreliable.
- **Leaderboard speed:** the leaderboard is recalculated on every request with one query per user. That's fine for a few hundred users, but it would need batching or caching to grow beyond that.
- **Cold starts:** on Render's free tier, the first request after a period of no traffic takes 30–60 seconds unless a keep-alive ping is set up.

## Roadmap ideas

- Batch and branch filters on the leaderboard
- An upcoming-contests calendar
- Rating history graphs and weekly "biggest climbers"
- Badges and solving streaks
