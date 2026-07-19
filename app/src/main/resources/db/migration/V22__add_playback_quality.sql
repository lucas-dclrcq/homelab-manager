-- Champs de qualité de lecture capturés depuis Jellyfin (polling) et les backups Jellystat (import).
-- Nullable : l'historique déjà en base reste NULL (regroupé sous « Inconnu » dans les stats Qualité).
ALTER TABLE playback_session
    ADD COLUMN play_method  VARCHAR(16),
    ADD COLUMN video_codec  VARCHAR(32),
    ADD COLUMN audio_codec  VARCHAR(32),
    ADD COLUMN video_height INT;
