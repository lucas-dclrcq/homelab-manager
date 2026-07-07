CREATE TABLE media_download
(
    id             BIGSERIAL PRIMARY KEY,
    source         VARCHAR(20)  NOT NULL,
    external_id    VARCHAR(255) NOT NULL,
    media_type     VARCHAR(20)  NOT NULL,
    title          VARCHAR(512) NOT NULL,
    season_number  INT,
    episode_number INT,
    episode_title  VARCHAR(512),
    quality        VARCHAR(100),
    language       VARCHAR(100),
    provider       VARCHAR(100),
    downloaded_at  TIMESTAMP    NOT NULL,
    created_at     TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_media_download_source_external UNIQUE (source, external_id)
);

CREATE INDEX idx_media_download_downloaded_at ON media_download (downloaded_at DESC, id DESC);

DROP TABLE homelab_event;
