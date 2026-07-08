ALTER TABLE application ADD COLUMN managed_by VARCHAR(50);
ALTER TABLE application ADD COLUMN external_id VARCHAR(512);
CREATE UNIQUE INDEX idx_application_external_id ON application (external_id) WHERE external_id IS NOT NULL;
