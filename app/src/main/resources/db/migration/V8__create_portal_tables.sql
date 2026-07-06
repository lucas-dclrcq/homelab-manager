CREATE TABLE application
(
    id                UUID PRIMARY KEY,
    name              VARCHAR(255)  NOT NULL,
    category          VARCHAR(100)  NOT NULL,
    description       TEXT          NOT NULL,
    url               VARCHAR(1024) NOT NULL,
    requires_vpn      BOOLEAN       NOT NULL DEFAULT FALSE,
    logo              BYTEA,
    logo_content_type VARCHAR(100),
    created_at        TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE TABLE homelab_event
(
    id          BIGSERIAL PRIMARY KEY,
    event_type  VARCHAR(50)  NOT NULL,
    title       VARCHAR(512) NOT NULL,
    details     JSONB,
    occurred_at TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_homelab_event_occurred_at ON homelab_event (occurred_at DESC);
