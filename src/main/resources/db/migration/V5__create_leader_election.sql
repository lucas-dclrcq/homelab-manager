CREATE TABLE leader_election (
    lock_key        VARCHAR(64)  PRIMARY KEY,
    instance_id     VARCHAR(255) NOT NULL,
    elected_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    last_heartbeat  TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- Seed an expired row so heartbeat logic always uses UPDATE (no INSERT race).
INSERT INTO leader_election (lock_key, instance_id, elected_at, last_heartbeat)
VALUES ('MAIN', 'none', NOW() - INTERVAL '1 hour', NOW() - INTERVAL '1 hour');
