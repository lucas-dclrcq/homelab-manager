-- Les signalements de médias ne sont plus une entité dédiée : ils passent désormais par les
-- workflows du module « problems » (fiche média → « Signaler un problème » ouvre un workflow).
-- On retire la table media_issue introduite en V24.
DROP TABLE IF EXISTS media_issue;
