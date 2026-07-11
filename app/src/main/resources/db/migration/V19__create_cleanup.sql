CREATE TABLE cleanup_campaign
(
    id                    UUID PRIMARY KEY,
    status                VARCHAR(20)  NOT NULL,
    trigger_type          VARCHAR(10)  NOT NULL,
    disk_path             VARCHAR(255) NOT NULL,
    free_bytes_at_start   BIGINT       NOT NULL,
    threshold_bytes       BIGINT       NOT NULL,
    target_bytes_to_free  BIGINT       NOT NULL,
    freed_bytes           BIGINT       NOT NULL DEFAULT 0,
    grace_ends_at         TIMESTAMP    NOT NULL,
    announcement_event_id VARCHAR(255),
    state                 JSONB        NOT NULL,
    created_at            TIMESTAMP    NOT NULL,
    updated_at            TIMESTAMP    NOT NULL,
    completed_at          TIMESTAMP
);

-- Une seule campagne active (en période de grâce) à la fois
CREATE UNIQUE INDEX ux_cleanup_campaign_active ON cleanup_campaign ((TRUE)) WHERE status = 'ANNOUNCED';

CREATE TABLE cleanup_candidate
(
    id               UUID PRIMARY KEY,
    campaign_id      UUID          NOT NULL REFERENCES cleanup_campaign (id) ON DELETE CASCADE,
    media_kind       VARCHAR(10)   NOT NULL,
    radarr_movie_id  INT,
    sonarr_series_id INT,
    season_number    INT,
    title            VARCHAR(512)  NOT NULL,
    year             INT,
    poster_url       VARCHAR(1024),
    size_bytes       BIGINT        NOT NULL,
    requester        VARCHAR(255),
    score            NUMERIC(6, 2) NOT NULL,
    score_breakdown  JSONB         NOT NULL,
    status           VARCHAR(20)   NOT NULL,
    protected_by     VARCHAR(255),
    protected_via    VARCHAR(10),
    protected_at     TIMESTAMP,
    deleted_at       TIMESTAMP,
    freed_bytes      BIGINT,
    failure_reason   TEXT,
    created_at       TIMESTAMP     NOT NULL,
    updated_at       TIMESTAMP     NOT NULL
);

CREATE INDEX ix_cleanup_candidate_campaign ON cleanup_candidate (campaign_id, status);
CREATE UNIQUE INDEX ux_cleanup_candidate_movie ON cleanup_candidate (campaign_id, radarr_movie_id)
    WHERE radarr_movie_id IS NOT NULL;
CREATE UNIQUE INDEX ux_cleanup_candidate_season ON cleanup_candidate (campaign_id, sonarr_series_id, season_number)
    WHERE sonarr_series_id IS NOT NULL;

CREATE TABLE cleanup_protection
(
    id               UUID PRIMARY KEY,
    media_kind       VARCHAR(10)  NOT NULL,
    radarr_movie_id  INT,
    sonarr_series_id INT,
    season_number    INT,
    title            VARCHAR(512) NOT NULL,
    year             INT,
    poster_url       VARCHAR(1024),
    protected_by     VARCHAR(255) NOT NULL,
    source           VARCHAR(10)  NOT NULL,
    created_at       TIMESTAMP    NOT NULL
);

CREATE UNIQUE INDEX ux_cleanup_protection_movie ON cleanup_protection (radarr_movie_id)
    WHERE media_kind = 'MOVIE';
CREATE UNIQUE INDEX ux_cleanup_protection_series ON cleanup_protection (sonarr_series_id)
    WHERE media_kind = 'SERIES';
CREATE UNIQUE INDEX ux_cleanup_protection_season ON cleanup_protection (sonarr_series_id, season_number)
    WHERE media_kind = 'SEASON';
