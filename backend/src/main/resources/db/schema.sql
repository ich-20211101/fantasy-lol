CREATE TABLE IF NOT EXISTS users (
    user_id     BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    username    VARCHAR(100) NOT NULL,
    email       VARCHAR(255) NOT NULL UNIQUE,
    google_id   VARCHAR(255) UNIQUE,        -- 추가: Google OAuth 식별자
    profile_image_url VARCHAR(500),         -- 추가: 구글 프로필 사진
    role        VARCHAR(20) DEFAULT 'USER', -- 추가: 권한관리
    password VARCHAR(255),
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS pro_teams (
    pro_team_id  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    full_name    VARCHAR(100) NOT NULL UNIQUE,
    short_name   VARCHAR(20) NOT NULL,
    status       VARCHAR(20) NOT NULL DEFAULT 'CURRENT',
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS players (
    player_id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    player_name          VARCHAR(100) NOT NULL,
    position             VARCHAR(20) NOT NULL,
    team_name            VARCHAR(100) NOT NULL,
    current_season_name  VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS teams (
    team_id     BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id     BIGINT NOT NULL,
    team_name   VARCHAR(100) NOT NULL,
    roster_locked BOOLEAN DEFAULT FALSE,
    current_season_name VARCHAR(100),
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE TABLE IF NOT EXISTS team_roster (
    team_roster_id  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    team_id         BIGINT NOT NULL,
    player_id       BIGINT NOT NULL,
    is_starter      BOOLEAN DEFAULT FALSE,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (team_id) REFERENCES teams(team_id),
    FOREIGN KEY (player_id) REFERENCES players(player_id)
);

CREATE TABLE IF NOT EXISTS matches (
    match_id    BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    leaguepedia_match_id VARCHAR(255) UNIQUE,
    season_name VARCHAR(100),
    team1   VARCHAR(100) NOT NULL,
    team2   VARCHAR(100) NOT NULL,
    match_date  TIMESTAMP NOT NULL,
    status      VARCHAR(20) DEFAULT 'SCHEDULED',
    winner VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS player_stats (
    player_stat_id      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    leaguepedia_game_id VARCHAR(255) UNIQUE,
    match_id            BIGINT NOT NULL,
    game_number         INT NOT NULL,
    player_id           BIGINT NOT NULL,
    team                VARCHAR(100),
    role                VARCHAR(20),
    champion            VARCHAR(100),
    kills               INT DEFAULT 0,
    deaths              INT DEFAULT 0,
    assists             INT DEFAULT 0,
    gold                INT DEFAULT 0,
    cs                  INT DEFAULT 0,
    damage_to_champions INT DEFAULT 0,
    vision_score        INT DEFAULT 0,
    player_win          BOOLEAN DEFAULT FALSE,
    actual_score        DOUBLE PRECISION DEFAULT 0.0,
    FOREIGN KEY (match_id) REFERENCES matches(match_id),
    FOREIGN KEY (player_id) REFERENCES players(player_id)
);

CREATE TABLE IF NOT EXISTS user_scores (
    user_score_id   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    week_number     INT NOT NULL,
    season_name     VARCHAR(100) NOT NULL,
    weekly_score    DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    seasonal_score  DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    updated_at      TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    UNIQUE (user_id, week_number, season_name)
);

CREATE TABLE IF NOT EXISTS withdrawal_feedbacks (
    withdrawal_feedback_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id     BIGINT,
    reason      VARCHAR(50) NOT NULL,
    note        VARCHAR(500),
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS weekly_starters (
    weekly_starter_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    team_id      BIGINT NOT NULL,
    player_id    BIGINT NOT NULL,
    week_number  INT NOT NULL,
    season_name  VARCHAR(100) NOT NULL,
    locked_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (team_id) REFERENCES teams(team_id),
    FOREIGN KEY (player_id) REFERENCES players(player_id),
    UNIQUE (team_id, week_number, season_name, player_id)
);

CREATE TABLE IF NOT EXISTS seasons (
    season_id    BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    season_name  VARCHAR(100) NOT NULL UNIQUE,
    start_date   DATE NOT NULL,
    end_date     DATE,
    status       VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS season_weeks (
    season_week_id     BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    season_id          BIGINT NOT NULL,
    week_number         INT NOT NULL,
    week_start_date     DATE NOT NULL,
    week_end_date       DATE NOT NULL,
    starter_locked_at   TIMESTAMP,
    finalized_at        TIMESTAMP,
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (season_id) REFERENCES seasons(season_id),
    UNIQUE (season_id, week_number)
);

CREATE TABLE IF NOT EXISTS weekly_settlements (
    weekly_settlement_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id       BIGINT NOT NULL,
    season_name   VARCHAR(100) NOT NULL,
    week_number   INT NOT NULL,
    total_score   DOUBLE PRECISION NOT NULL,
    rank          INT NOT NULL,
    settled_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    UNIQUE (user_id, season_name, week_number)
);

CREATE TABLE IF NOT EXISTS season_settlements (
    season_settlement_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id       BIGINT NOT NULL,
    season_name   VARCHAR(100) NOT NULL,
    total_score   DOUBLE PRECISION NOT NULL,
    rank          INT NOT NULL,
    settled_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    UNIQUE (user_id, season_name)
);

CREATE INDEX IF NOT EXISTS idx_teams_user_id ON teams(user_id);
CREATE INDEX IF NOT EXISTS idx_team_roster_team_id ON team_roster(team_id);
CREATE INDEX IF NOT EXISTS idx_player_stats_match_id ON player_stats(match_id);
CREATE INDEX IF NOT EXISTS idx_player_stats_player_id ON player_stats(player_id);
CREATE INDEX IF NOT EXISTS idx_user_scores_user_id ON user_scores(user_id);