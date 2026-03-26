CREATE TABLE series_notification_thread (
    series_id        VARCHAR PRIMARY KEY,
    event_id         VARCHAR NOT NULL,
    last_notified_at TIMESTAMP NOT NULL
);
