-- Ledger: append-only. Rows are never updated or deleted; a mistake gets a reversal row.
CREATE TABLE ledger_entries (
    id             UUID PRIMARY KEY,
    business_id    UUID         NOT NULL,
    type           VARCHAR(10)  NOT NULL CHECK (type IN ('INCOME', 'EXPENSE')),
    amount_cents   BIGINT       NOT NULL CHECK (amount_cents > 0),
    description    VARCHAR(140) NOT NULL,
    category       VARCHAR(40)  NOT NULL,
    payment_method VARCHAR(20)  NOT NULL,
    occurred_on    DATE         NOT NULL,
    recorded_at    TIMESTAMPTZ  NOT NULL,
    -- UNIQUE: an entry can be reversed at most once, even under concurrent requests.
    reversal_of    UUID UNIQUE REFERENCES ledger_entries (id)
);

CREATE INDEX ledger_entries_business_date ON ledger_entries (business_id, occurred_on);
