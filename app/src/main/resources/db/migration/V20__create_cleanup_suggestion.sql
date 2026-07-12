CREATE TABLE cleanup_suggestion
(
    id                    UUID PRIMARY KEY,
    media_kind            VARCHAR(10)   NOT NULL,
    radarr_movie_id       INT,
    sonarr_series_id      INT,
    season_number         INT,
    title                 VARCHAR(512)  NOT NULL,
    year                  INT,
    poster_url            VARCHAR(1024),
    size_bytes            BIGINT        NOT NULL,
    suggested_by          VARCHAR(255)  NOT NULL,
    announcement_event_id VARCHAR(255),
    status                VARCHAR(20)   NOT NULL,
    delete_after          TIMESTAMP     NOT NULL,
    vetoed_by             VARCHAR(255),
    vetoed_at             TIMESTAMP,
    deleted_at            TIMESTAMP,
    freed_bytes           BIGINT,
    failure_reason        TEXT,
    created_at            TIMESTAMP     NOT NULL,
    updated_at            TIMESTAMP     NOT NULL
);

-- listPending / listDue / listRecent filtrent par statut et trient par delete_after
CREATE INDEX ix_cleanup_suggestion_status ON cleanup_suggestion (status, delete_after);

-- findPendingByAnnouncementEvent : résolution d'un veto par réaction Matrix
CREATE INDEX ix_cleanup_suggestion_announcement ON cleanup_suggestion (announcement_event_id)
    WHERE announcement_event_id IS NOT NULL;
