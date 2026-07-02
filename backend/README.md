# ⚔️ Fantasy LoL

A fantasy esports web application based on the LCK (League of Legends Champions Korea) league. Users build their own fantasy roster from real pro players and compete on weekly and seasonal leaderboards based on actual in-game performance.

---

## Tech Stack

**Backend**
- Java 17, Spring Boot 3.4.5
- Spring Security + OAuth2 (Google Login)
- Spring Data JPA, PostgreSQL
- Spring Data Redis (leaderboard caching)
- Leaguepedia Cargo API (LCK data)

**Frontend**
- React 18 + Vite
- React Router

**Infrastructure**
- Docker Compose (PostgreSQL, Redis)

---

## Features

- **Google OAuth Login** — sign in with your Google account
- **Player Roster** — build a fantasy team of 8 LCK players (one per position required)
- **Weekly Starters** — pick 5 starters each week (Top / Jungle / Mid / Bot / Support)
- **Match Data Sync** — pull real LCK match results from Leaguepedia and store player stats
- **Actual Score Calculation** — score computed per player per game:
  ```
  score = kills×3 + assists×1 - deaths×1 + win_bonus×5
        + cs×0.01 + damage×0.001 + vision_score×0.2
  ```
- **Projected Score** — predicted score based on recent 5-game average with opponent adjustment
- **Lineup Recommendation** — suggests the best weekly lineup by projected score
- **Leaderboard** — weekly and seasonal user rankings cached with Redis

---

## Project Structure

```
fantasy-lol/
├── backend/          # Spring Boot API server
│   └── src/main/java/com/fantasylol/backend/
│       ├── config/       # Security, CORS, Web config
│       ├── controller/   # REST API endpoints
│       ├── service/      # Business logic
│       ├── repository/   # JPA repositories
│       ├── entity/       # JPA entities
│       └── dto/          # Request / Response DTOs
├── frontend/         # React + Vite client
│   └── src/
│       └── pages/    # RosterPage, StarterPage
└── docker-compose.yml
```

---

## Database Schema

| Table | Description |
|---|---|
| `users` | Google OAuth user accounts |
| `players` | Real LCK pro players |
| `teams` | User-created fantasy teams |
| `team_roster` | Junction table — players in a team (8 max) |
| `matches` | LCK match records synced from Leaguepedia |
| `player_stats` | Per-game stats and calculated actual score |
| `user_scores` | Weekly and seasonal scores per user |

---

## Getting Started

**Prerequisites:** Docker, Java 17, Node.js 20+

```bash
# 1. Start PostgreSQL and Redis
docker-compose up -d postgres redis

# 2. Set local credentials (create this file, never commit)
# backend/src/main/resources/application-local.yaml
# leaguepedia:
#   username: <wiki_username@bot_name>
#   password: <bot_password>
# spring:
#   security:
#     oauth2:
#       client:
#         registration:
#           google:
#             client-id: <GOOGLE_CLIENT_ID>
#             client-secret: <GOOGLE_CLIENT_SECRET>

# 3. Run backend (IntelliJ or Gradle)
cd backend
./gradlew bootRun -Dspring.profiles.active=local

# 4. Run frontend
cd frontend
npm install
npm run dev
```

Open `http://localhost:5173`

---

## API Overview

| Method | Endpoint | Description |
|---|---|---|
| GET | `/users/me` | Get current logged-in user |
| GET | `/players` | List all LCK players |
| GET | `/teams/me` | Get my fantasy team |
| PUT | `/teams/roster` | Submit or update roster (8 players) |
| PUT | `/teams/{id}/starters` | Set weekly starters (5 players) |
| GET | `/teams/me/starters` | Get current starters |
| POST | `/matches/sync` | Sync LCK match data by date |
| POST | `/matches/players/sync` | Sync LCK player roster from Leaguepedia |
| GET | `/leaderboard/weekly` | Weekly leaderboard |
| GET | `/leaderboard/seasonal` | Seasonal leaderboard |

---

## Notes

- Roster is locked once the season begins (`rosterLocked = true`), managed by admin
- Match data is synced manually via API or automatically via scheduler (planned)
- Leaguepedia Bot Password required for API access — see [Leaguepedia API docs](https://lol.fandom.com/wiki/Help:Leaguepedia_API)
