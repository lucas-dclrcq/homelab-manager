DROP TABLE series_notification_thread;

CREATE TABLE media_notification_thread (
    id               BIGSERIAL PRIMARY KEY,
    media_id         VARCHAR NOT NULL,
    media_type       VARCHAR NOT NULL,
    media_key        VARCHAR,
    event_id         VARCHAR NOT NULL,
    last_notified_at TIMESTAMP NOT NULL,
    UNIQUE(media_id, media_type)
);

CREATE INDEX idx_media_notification_thread_media_key ON media_notification_thread(media_key);
