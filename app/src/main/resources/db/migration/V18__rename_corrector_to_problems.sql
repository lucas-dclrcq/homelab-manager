ALTER TABLE corrector_workflow RENAME TO problem_workflow;
ALTER INDEX idx_corrector_workflow_user RENAME TO idx_problem_workflow_user;
ALTER INDEX idx_corrector_workflow_awaiting RENAME TO idx_problem_workflow_awaiting;
ALTER TABLE problem_workflow RENAME COLUMN movie_title TO media_title;
ALTER TABLE problem_workflow ADD COLUMN sonarr_series_id INT;

UPDATE problem_workflow
SET state = (state - 'movie') || jsonb_build_object('media', state -> 'movie')
WHERE state ? 'movie';
