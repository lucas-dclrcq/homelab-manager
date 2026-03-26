ALTER TABLE leader_election ADD COLUMN status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE';

UPDATE leader_election SET status = 'RELEASED' WHERE lock_key = 'MAIN' AND last_heartbeat < NOW() - INTERVAL '30 seconds';