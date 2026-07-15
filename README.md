# ⚔️ Fantasy LoL — LoL Fantasy Maker

**A production-grade fantasy esports platform for the LCK, built and deployed end-to-end by a solo full-stack engineer.**

[![Live](https://img.shields.io/badge/Live-lolfantasymaker.com-2ea44f?style=flat-square)](https://lolfantasymaker.com)
![Java](https://img.shields.io/badge/Java-17-orange?style=flat-square)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.5-6DB33F?style=flat-square&logo=springboot)
![React](https://img.shields.io/badge/React-18-61DAFB?style=flat-square&logo=react)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-DB-4169E1?style=flat-square&logo=postgresql)
![Redis](https://img.shields.io/badge/Redis-Cache-DC382D?style=flat-square&logo=redis)
![AWS](https://img.shields.io/badge/AWS-EC2%20%7C%20SSM%20%7C%20S3-FF9900?style=flat-square&logo=amazonaws)
![CI/CD](https://img.shields.io/badge/CI%2FCD-GitHub%20Actions%20%2B%20OIDC-2088FF?style=flat-square&logo=githubactions)

**Live product:** [lolfantasymaker.com](https://lolfantasymaker.com)

---

## Overview

Fantasy LoL lets users draft a fantasy roster of real LCK (League of Legends Champions Korea) pro players and compete against other users based on those players' **actual match performance**. Every score on the leaderboard is derived from real match data synced from a third-party esports data API — this is not a toy CRUD app with mock data, it's a system that ingests, transforms, and scores live external data on an ongoing basis.

The project was built to demonstrate what a single engineer can own end-to-end: schema design, backend business logic, a data pipeline against a third-party API, a React frontend, and a secure, automated cloud deployment — the kind of full-lifecycle ownership expected in a small/mid-size product team.

---

## What This Project Demonstrates

For anyone reviewing this as a portfolio piece, the areas of engineering depth are:

| Area | What was built |
|---|---|
| **Backend architecture** | Layered Spring Boot service (controller/service/repository/entity/dto), JPA entity modeling for a relational domain with 7+ interrelated tables |
| **External data integration** | A resilient client against the Leaguepedia Cargo API, with duplicate-prevention (unique key constraints) and per-game stat ingestion |
| **Business logic / scoring** | A deterministic fantasy scoring engine translating raw match stats into weighted user scores, plus a projected-score model using rolling averages and opponent-strength adjustment |
| **Auth & security** | Google OAuth2 login via Spring Security, CORS policy, secrets kept out of version control |
| **Caching strategy** | Redis-backed leaderboard caching with PostgreSQL as the source of truth (cache is never authoritative) |
| **Cloud deployment** | Credential-less, SSH-less, push-to-deploy CI/CD pipeline on AWS (details below) — this is the part most side projects skip entirely |
| **Frontend** | React 18 + Vite SPA with client-side routing, consuming the REST API |

---

## Live Demo

🔗 **[lolfantasymaker.com](https://lolfantasymaker.com)**

Sign in with Google, build an 8-player roster within position constraints, set your weekly 5-player starting lineup, and track your score against real LCK match results.

---

## Core Features

- **Google OAuth Login** — sign in with an existing Google account, no separate credential system to manage
- **Fantasy Roster Builder** — draft an 8-player roster with one required player per position (Top / Jungle / Mid / Bot / Support, plus bench)
- **Weekly Starters** — set a 5-player starting lineup each week
- **Live Match Data Sync** — pulls real LCK match results and per-player stats from Leaguepedia
- **Actual Score Engine** — every player's in-game performance is converted into a fantasy score:

  ```
  score = kills×3 + assists×1 − deaths×1 + win_bonus×5
        + cs×0.01 + damage×0.001 + vision_score×0.2
  ```

- **Projected Score** — forecasts a player's next-match score from their trailing 5-game average, adjusted for opponent strength
- **Lineup Recommendation** — suggests the optimal weekly starting five by projected score
- **Weekly & Seasonal Leaderboards** — Redis-cached rankings across all users

---

## Architecture

```
React (Vite SPA)  ──HTTP──▶  Spring Boot REST API  ──JPA──▶  PostgreSQL
                                     │
                                     ├──▶ Redis (leaderboard cache)
                                     │
                                     └──▶ Leaguepedia Cargo API (match/player data)
```

**Project structure**

```
fantasy-lol/
├── backend/          Spring Boot API server
│   └── src/main/java/com/fantasylol/backend/
│       ├── config/       Security, CORS, Web config
│       ├── controller/   REST API endpoints
│       ├── service/      Business logic
│       ├── repository/   JPA repositories
│       ├── entity/       JPA entities
│       └── dto/          Request / Response DTOs
├── frontend/         React + Vite client
│   └── src/pages/    RosterPage, StarterPage, ...
└── docker-compose.yml
```

**Database schema**

| Table | Description |
|---|---|
| `users` | Google OAuth user accounts |
| `players` | Real LCK pro players |
| `teams` | User-created fantasy teams |
| `team_roster` | Junction table — players on a team (8 max) |
| `matches` | LCK match records synced from Leaguepedia |
| `player_stats` | Per-game stats and calculated actual score |
| `user_scores` | Weekly and seasonal scores per user |

---

## Production Deployment & Infrastructure

Shipping this wasn't just `git push` to a server — the deployment pipeline was deliberately engineered around four principles: **no long-lived cloud credentials, no SSH-based deployments, least-privilege access, and fully automated deploys triggered by a git push.**

```
Developer → Git Push → GitHub Actions → GitHub OIDC Auth → AWS IAM Role
                                                                │
                            ┌───────────────────────────────────┤
                            ▼                                   ▼
                     Private GHCR                          Private S3
                   (backend image)                   (frontend build artifact)
                            └──────────────┬────────────────────┘
                                           ▼
                         AWS Systems Manager (Run Command)
                                           ▼
                                     Amazon EC2
                              ┌────────────┴────────────┐
                              ▼                          ▼
                     Pull backend image         Download frontend artifact
                              └────────────┬────────────┘
                                          ▼
                                  Docker Compose up
                                          ▼
                                    Health Check
                                          ▼
                                    Nginx Reload
```

**Security design**

- **GitHub OIDC** — GitHub Actions authenticates to AWS via OpenID Connect instead of long-lived Access Keys stored in secrets. Every workflow run gets short-lived, scoped credentials.
- **Least-privilege IAM**, split by responsibility:
  - *GitHub Actions* can only upload frontend artifacts to S3, publish backend images, and trigger SSM commands.
  - *EC2* can only pull frontend artifacts, read deployment secrets from Parameter Store, and pull images from the private registry.
- **Secrets management** — production secrets (registry tokens, future API keys) live in AWS Systems Manager Parameter Store (SecureString). GitHub Actions never has direct access to production secrets.
- **No SSH** — deployments are executed via AWS Systems Manager Run Command, so there is no open SSH port and no standing shell access to production.

**Deployment flow**

| Stage | Steps |
|---|---|
| Backend | Build Spring Boot app → build Docker image → push versioned image to private GHCR |
| Frontend | Build React app → package `dist/` as a versioned archive → upload to private S3 |
| EC2 | Receive SSM command → pull artifacts/image → authenticate to GHCR → update containers via Docker Compose → run health check → reload Nginx |

**How the pipeline evolved**

| Phase | Approach | Purpose |
|---|---|---|
| v0.1 | Manual deployment | Initial development |
| v0.2 | GitHub Actions + SSH | First CI/CD implementation |
| v0.3 | GitHub OIDC | Removed permanent AWS credentials |
| v0.4 | AWS Systems Manager | Eliminated SSH-based deployment |
| v0.5 | S3 artifact deployment | Versioned, reproducible frontend releases |
| **Current** | **OIDC + SSM + private GHCR** | Production-oriented, zero-touch pipeline |

This incremental hardening — starting from manual deploys and eliminating SSH, then eliminating permanent credentials — was a deliberate exercise in AWS security best practices, not the starting design.

---

## Tech Stack

**Backend**
- Java 17, Spring Boot 3.4.5
- Spring Security + OAuth2 (Google Login)
- Spring Data JPA, PostgreSQL
- Spring Data Redis (leaderboard caching)
- Leaguepedia Cargo API integration

**Frontend**
- React 18 + Vite
- React Router

**Infrastructure**
- Docker Compose (local: PostgreSQL, Redis)
- AWS EC2, S3, Systems Manager, Parameter Store
- GitHub Actions + OIDC, GitHub Container Registry (private)
- Nginx

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

## Getting Started

**Prerequisites:** Docker, Java 17, Node.js 20+

```bash
# 1. Start PostgreSQL and Redis
docker-compose up -d postgres redis

# 2. Set local credentials (never committed)
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

# 3. Run backend
cd backend
./gradlew bootRun -Dspring.profiles.active=local

# 4. Run frontend
cd frontend
npm install
npm run dev
```

Open `http://localhost:5173`

---

## Roadmap

**Shipped:** OAuth login, full roster/starter management, live Leaguepedia data pipeline, automated zero-SSH deployment pipeline.

**In progress:** the scoring engine that converts synced match results into weekly/seasonal user scores, and the leaderboard API built on top of it.

**Next up:**
- Observability (CloudWatch logs/metrics, deployment notifications)
- Automated PostgreSQL backups and rollback automation
- HTTPS via Let's Encrypt, Route53, CloudFront, rate limiting, WAF evaluation
- Scalability path: ECS migration, Application Load Balancer, Auto Scaling, RDS, ElastiCache

---

## Disclaimer

This is an unofficial fan project. Fantasy LoL is not affiliated with Riot Games or the LCK. All match data is used to power simulation/fantasy content only.
