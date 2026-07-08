CREATE TABLE corrector_workflow
(
    id              UUID PRIMARY KEY,
    username        VARCHAR(255) NOT NULL,
    media_type      VARCHAR(20)  NOT NULL,
    problem_type    VARCHAR(50),
    status          VARCHAR(30)  NOT NULL,
    radarr_movie_id INT,
    movie_title     VARCHAR(512),
    grabbed_at      TIMESTAMP,
    state           JSONB        NOT NULL,
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    completed_at    TIMESTAMP
);

CREATE INDEX idx_corrector_workflow_user ON corrector_workflow (username, updated_at DESC);
CREATE INDEX idx_corrector_workflow_awaiting ON corrector_workflow (status, radarr_movie_id);
