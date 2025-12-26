-- Migration: create outbox_event table for Outbox pattern
CREATE TABLE IF NOT EXISTS outbox_event (
    id BIGSERIAL PRIMARY KEY,
    aggregate_type VARCHAR(255) NOT NULL,
    aggregate_id UUID NOT NULL,
    type VARCHAR(255) NOT NULL,
    topic VARCHAR(255),
    payload TEXT NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    sent_at TIMESTAMP
);

-- Optional index to efficiently query pending events
CREATE INDEX IF NOT EXISTS idx_outbox_event_status_created_at ON outbox_event (status, created_at);
