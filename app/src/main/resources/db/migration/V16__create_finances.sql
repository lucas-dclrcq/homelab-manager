CREATE TABLE member
(
    id           UUID PRIMARY KEY,
    authentik_pk INT UNIQUE,
    username     VARCHAR(150) NOT NULL,
    display_name VARCHAR(255) NOT NULL,
    email        VARCHAR(255),
    active       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP
);

CREATE TABLE finance_recurring_rule
(
    id           UUID PRIMARY KEY,
    type         VARCHAR(20)  NOT NULL,
    label        VARCHAR(255) NOT NULL,
    amount_cents INT          NOT NULL CHECK (amount_cents > 0),
    -- 1..28 : évite les mois sans 29/30/31
    day_of_month INT          NOT NULL CHECK (day_of_month BETWEEN 1 AND 28),
    member_id    UUID REFERENCES member (id),
    vendor       VARCHAR(255),
    active       BOOLEAN      NOT NULL DEFAULT TRUE,
    start_date   DATE         NOT NULL,
    end_date     DATE,
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE finance_entry
(
    id           UUID PRIMARY KEY,
    type         VARCHAR(20)  NOT NULL,
    source       VARCHAR(20)  NOT NULL,
    label        VARCHAR(255) NOT NULL,
    vendor       VARCHAR(255),
    amount_cents INT          NOT NULL CHECK (amount_cents > 0),
    entry_date   DATE         NOT NULL,
    member_id    UUID REFERENCES member (id),
    rule_id      UUID REFERENCES finance_recurring_rule (id) ON DELETE SET NULL,
    period       VARCHAR(7),
    notes        TEXT,
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- Idempotence des écritures générées : une occurrence par règle et par mois,
-- une écriture d'énergie par mois
CREATE UNIQUE INDEX ux_finance_entry_rule_period ON finance_entry (rule_id, period) WHERE rule_id IS NOT NULL;
CREATE UNIQUE INDEX ux_finance_entry_energy_period ON finance_entry (period) WHERE source = 'ENERGY';
CREATE INDEX ix_finance_entry_date ON finance_entry (entry_date);

CREATE TABLE finance_settings
(
    id         INT PRIMARY KEY DEFAULT 1 CHECK (id = 1),
    kwh_price  NUMERIC(8, 5),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

INSERT INTO finance_settings (id, kwh_price)
VALUES (1, NULL);
