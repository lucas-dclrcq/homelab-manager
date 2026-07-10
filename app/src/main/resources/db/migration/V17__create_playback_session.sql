CREATE TABLE playback_session
(
    id                    UUID PRIMARY KEY,
    user_id               VARCHAR(64)  NOT NULL,
    user_name             VARCHAR(255) NOT NULL,
    item_id               VARCHAR(64)  NOT NULL,
    item_name             VARCHAR(512) NOT NULL,
    series_id             VARCHAR(64),
    series_name           VARCHAR(512),
    season_number         INT,
    episode_number        INT,
    media_type            VARCHAR(16)  NOT NULL,
    client                VARCHAR(255),
    device_name           VARCHAR(255),
    platform              VARCHAR(64),
    started_at            TIMESTAMP    NOT NULL,
    ended_at              TIMESTAMP    NOT NULL,
    play_duration_seconds INT          NOT NULL,
    runtime_seconds       INT,
    progress_percent      NUMERIC(5, 2),
    completed             BOOLEAN      NOT NULL DEFAULT FALSE,
    source                VARCHAR(16)  NOT NULL,
    import_key            VARCHAR(64),
    created_at            TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- Dédup de l'import Jellystat : rejouer le même backup ne crée pas de doublons
CREATE UNIQUE INDEX ux_playback_session_import_key ON playback_session (import_key) WHERE import_key IS NOT NULL;
CREATE INDEX ix_playback_session_started_at ON playback_session (started_at);
CREATE INDEX ix_playback_session_user ON playback_session (user_id, started_at);
CREATE INDEX ix_playback_session_series ON playback_session (series_id) WHERE series_id IS NOT NULL;
CREATE INDEX ix_playback_session_media_type ON playback_session (media_type, started_at);
