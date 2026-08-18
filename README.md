그대로 복사해서 쓰시면 됩니다:

```markdown
# ⚔️ Fantasy LoL

**A fantasy esports app for the LCK, built and run solo from backend to deployment.**

[![Live](https://img.shields.io/badge/Live-lolfantasymaker.com-2ea44f?style=flat-square)](https://lolfantasymaker.com)
![Java](https://img.shields.io/badge/Java-17-orange?style=flat-square)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.5-6DB33F?style=flat-square&logo=springboot)
![React](https://img.shields.io/badge/React-19-61DAFB?style=flat-square&logo=react)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-DB-4169E1?style=flat-square&logo=postgresql)
![Redis](https://img.shields.io/badge/Redis-Cache-DC382D?style=flat-square&logo=redis)
![AWS](https://img.shields.io/badge/AWS-EC2%20%7C%20SSM%20%7C%20S3-FF9900?style=flat-square&logo=amazonaws)
![CI/CD](https://img.shields.io/badge/CI%2FCD-GitHub%20Actions%20%2B%20OIDC-2088FF?style=flat-square&logo=githubactions)

**Live:** [lolfantasymaker.com](https://lolfantasymaker.com)

---

## What it is

Fantasy LoL lets you draft a fantasy roster of real LCK pro players and score points based on how they actually perform in real matches. Every score comes from real match data I sync from a third party esports API. There's no mock data behind any of this, the leaderboard is scored off live results.

I built the whole thing myself: schema design, backend business logic, a data pipeline against a third party API, the React frontend, and a secure automated cloud deployment.

---

## Where the real engineering is

If you're looking at this as a portfolio piece, this is where the depth is:

| Area | What's there |
|---|---|
| Backend architecture | Layered Spring Boot service (controller/service/repository/entity/dto), 15 interrelated JPA entities covering users, teams, matches, seasons, weekly settlement, and withdrawal feedback |
| External data integration | A client against the Leaguepedia Cargo API with duplicate prevention (unique key constraints) and per game stat ingestion |
| Business logic / scoring | A deterministic fantasy scoring engine that turns raw match stats into weighted user scores, plus weekly and season level settlement |
| Auth & security | Google OAuth2 via Spring Security, CORS policy, secrets kept out of version control |
| Caching | Redis backed leaderboard caching with PostgreSQL as the source of truth, cache is never authoritative |
| Cloud deployment | Credential-less, SSH-less, push to deploy CI/CD on AWS, most side projects skip this part entirely |
| Frontend | React 19 + Vite SPA with client side routing and Korean/English support |

---

## Try it

[lolfantasymaker.com](https://lolfantasymaker.com)

Sign in with Google, draft an 8 player roster within position limits, pick your 5 starters each week, and track your score against real LCK results.

---

## Features

- Google OAuth login, no separate account system to manage
- Draft an 8 player roster with at least one player per position (Top, Jungle, Mid, Bot, Support), the rest sit on the bench
- Set your 5 starters every week
- Match results and player stats sync automatically from Leaguepedia
- Score formula:

  ```
  score = kills*3 + assists*1 - deaths*1 + winBonus*5
        + cs*0.01 + damage*0.001 + visionScore*0.2
  ```

- Weekly and seasonal leaderboards, cached in Redis, with a per user detail view showing starters and bench
- Player ranking page to browse scores across all positions
- Profile page for changing your nickname or deleting your account, with an exit survey on withdrawal
- Privacy policy and score policy pages
- Korean and English support
- Admin dashboard for managing seasons, pro teams, and player pricing

---

## Architecture

```
React (Vite SPA)  --HTTP-->  Spring Boot REST API  --JPA-->  PostgreSQL
                                     |
                                     |--> Redis (leaderboard cache)
                                     |
                                     `--> Leaguepedia Cargo API (match/player data)
```

**Project structure**

```
fantasy-lol/
├── backend/          Spring Boot API server
│   └── src/main/java/com/fantasylol/backend/
│       ├── config/       security, CORS, web config
│       ├── controller/   REST endpoints
│       ├── service/      business logic
│       ├── repository/   JPA repositories
│       ├── entity/       JPA entities
│       └── dto/          request/response DTOs
├── frontend/         React + Vite client
│   └── src/pages/    Home, RosterPage, MyRosterPage, RegisterTeamPage,
│                      StarterPage, MyTeamPage, LeaderboardPage,
│                      LeaderboardDetailPage, InfoPage, ProfilePage,
│                      WithdrawPage, WithdrawConfirmPage,
│                      PrivacyPolicyPage, ScorePolicyPage,
│                      AdminLoginPage, AdminDashboardPage
└── docker-compose.yml
```

**Database**

15 tables:

| Table | What it holds |
|---|---|
| `users` | Google OAuth accounts |
| `pro_teams` | LCK pro teams |
| `players` | Real LCK players |
| `seasons` | Season and round definitions |
| `season_weeks` | Weeks within a season, including lock state |
| `teams` | User created fantasy teams |
| `team_roster` | Players on a team, 8 max |
| `weekly_starters` | Which players a user started each week |
| `matches` | LCK match records synced from Leaguepedia |
| `player_stats` | Per game stats and calculated score |
| `weekly_player_scores` | Computed player scores per week |
| `user_scores` | Aggregated user scores |
| `weekly_settlements` | Weekly score settlement per user |
| `season_settlements` | Season level final standings per user |
| `withdrawal_feedbacks` | Exit survey answers on account deletion |

---

## Production deployment

I didn't just `git push` to a box somewhere. The pipeline is built around four things: no long lived cloud credentials, no SSH, least privilege access, and a fully automated deploy on every push.

```
Developer -> Git Push -> GitHub Actions -> GitHub OIDC -> AWS IAM Role
                                                              |
                          +-----------------------------------+
                          |                                   |
                     Private GHCR                        Private S3
                   (backend image)                (frontend build artifact)
                          +-----------------+-----------------+
                                            |
                        AWS Systems Manager (Run Command)
                                            |
                                      Amazon EC2
                            +----------------+----------------+
                            |                                 |
                    Pull backend image            Download frontend artifact
                            +----------------+-----------------+
                                             |
                                     Docker Compose up
                                             |
                                       Health check
                                             |
                                       Nginx reload
```

**Security**

- GitHub Actions authenticates to AWS with OIDC instead of storing long lived access keys as secrets. Every run gets short lived, scoped credentials.
- IAM is split by responsibility. GitHub Actions can only push frontend artifacts to S3, publish backend images, and trigger SSM commands. EC2 can only pull artifacts, read secrets from Parameter Store, and pull images from the private registry.
- Production secrets (registry tokens, future API keys) live in AWS Systems Manager Parameter Store as SecureStrings. GitHub Actions never touches production secrets directly.
- No SSH. Deploys run through AWS Systems Manager Run Command, so there's no open SSH port and no standing shell access into production.

**How the deploy pipeline got here**

| Stage | What changed |
|---|---|
| v0.1 | Manual deploys |
| v0.2 | GitHub Actions + SSH |
| v0.3 | Switched to GitHub OIDC, dropped permanent AWS credentials |
| v0.4 | Switched to AWS Systems Manager, dropped SSH |
| v0.5 | Frontend ships as a versioned S3 artifact |
| Now | OIDC + SSM + private GHCR, no manual steps |

This was built up in stages on purpose, starting from manual deploys and removing SSH and permanent credentials one step at a time, not designed this way from day one.

Right now Postgres and Redis run as plain Docker containers on the same EC2 instance as the backend, not RDS or ElastiCache. That move is next.

---

## Tech stack

**Backend**
- Java 17, Spring Boot 3.4.5
- Spring Security + OAuth2 (Google login)
- Spring Data JPA, PostgreSQL
- Spring Data Redis (leaderboard cache)
- Leaguepedia Cargo API integration

**Frontend**
- React 19 + Vite
- React Router
- react-i18next (Korean/English)

**Infrastructure**
- Docker Compose (Postgres, Redis, backend all on one EC2 box)
- AWS EC2, S3, Systems Manager, Parameter Store
- GitHub Actions + OIDC, GitHub Container Registry (private)
- Nginx

---

## API

User facing:

| Method | Endpoint | What it does |
|---|---|---|
| POST | `/users` | Register a user |
| GET | `/users/me` | Current logged in user |
| POST | `/users/logout` | Log out |
| PATCH | `/users/me/nickname` | Change nickname |
| GET | `/users/me/scores` | Score history for the current user |
| DELETE | `/users/me` | Delete account |
| GET | `/players` | List LCK players |
| GET | `/players/rankings` | Player ranking list |
| GET | `/players/purchase-list` | Players available to draft, with price |
| GET | `/teams/me` | My fantasy team |
| PUT | `/teams/roster` | Submit or update my roster |
| GET | `/teams/me/starters` | Current starters |
| PUT | `/teams/{teamId}/starters` | Set weekly starters |
| DELETE | `/teams/me` | Delete my team |
| GET | `/leaderboard` | Leaderboard, filterable by week and season, paginated |
| GET | `/leaderboard/rounds` | Available rounds/weeks for filtering |
| GET | `/leaderboard/{userId}` | One user's score breakdown, starters and bench |

Data sync and ops, used internally, not exposed in the UI:

| Method | Endpoint | What it does |
|---|---|---|
| POST | `/matches/sync` | Sync match results for a date |
| POST | `/matches/sync-season` | Sync a full season |
| POST | `/matches/players/sync` | Sync player roster from Leaguepedia |
| POST | `/matches/starters/lock` | Lock starters for a week |
| GET | `/matches/recent-results` | Recent match results |
| GET | `/matches/week` | Current week info |
| POST | `/players/pricing/calculate` | Recalculate player prices |
| PATCH | `/players/{playerId}/status` | Update a player's active status |
| GET | `/pro-teams` | List pro teams |
| POST | `/pro-teams/sync-from-players` | Backfill pro teams from player data |
| PUT | `/pro-teams/{proTeamId}` | Edit a pro team |
| DELETE | `/pro-teams/{proTeamId}` | Delete a pro team |
| GET | `/seasons` | List seasons |
| POST | `/seasons` | Create a season |
| POST | `/seasons/feature` | Feature a season |
| POST | `/seasons/roster-source` | Set which season's stats price the roster |
| POST | `/seasons/ranking-min-games` | Set minimum games for ranking eligibility |
| GET | `/seasons/detect-new` | Detect new seasons from Leaguepedia |
| POST | `/seasons/weeks/lock` | Lock a season week |
| POST | `/seasons/activate-due` | Activate seasons that are due |
| POST | `/seasons/end` | End a season |

Admin:

| Method | Endpoint | What it does |
|---|---|---|
| POST | `/admin/login` | Admin login |
| GET | `/admin/me` | Current admin |
| PUT | `/admin/me/password` | Change admin password |

---

## Running it locally

**You need:** Docker, Java 17, Node.js 20+

```bash
# 1. start postgres and redis
docker-compose up -d postgres redis

# 2. set local credentials (never committed)
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

# 3. run backend
cd backend
./gradlew bootRun -Dspring.profiles.active=local

# 4. run frontend
cd frontend
npm install
npm run dev
```

Open `http://localhost:5173`

---

## Roadmap

**Shipped:** OAuth login, roster and starter management, live Leaguepedia data sync, the scoring engine, weekly and seasonal leaderboards with per user detail, player rankings, profile management and account deletion, an admin dashboard, Korean/English support, and a zero SSH automated deploy pipeline.

**Not built yet:**
- Projected score, forecasting a player's next match score from recent form
- Lineup recommendation, suggesting the best 5 starters by projected score
- CloudWatch logs and metrics, deploy notifications
- Automated PostgreSQL backups
- HTTPS through Let's Encrypt, Route53, CloudFront, rate limiting, and a look at WAF
- Moving Postgres and Redis off the EC2 box onto RDS and ElastiCache, and moving the app itself onto ECS with an ALB and auto scaling

---

## Disclaimer

This is an unofficial fan project. It's not affiliated with Riot Games or the LCK. All match data is used for simulation and fantasy content only.
```