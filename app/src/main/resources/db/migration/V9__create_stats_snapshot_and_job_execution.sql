CREATE TABLE stats_snapshot
(
    source        VARCHAR(50) PRIMARY KEY,
    movie_count   INT,
    series_count  INT,
    episode_count INT,
    disks         JSONB     NOT NULL,
    collected_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE job_execution
(
    job_identity     VARCHAR(100) PRIMARY KEY,
    last_run_at      TIMESTAMP   NOT NULL,
    last_status      VARCHAR(20) NOT NULL,
    last_duration_ms BIGINT,
    last_error       TEXT,
    manual           BOOLEAN     NOT NULL DEFAULT FALSE
);
