CREATE TABLE processed_event (
    event_id UUID PRIMARY KEY,
    event_type VARCHAR(255) NOT NULL,
    topic VARCHAR(255) NOT NULL,
    partition INTEGER NOT NULL,
    record_offset BIGINT NOT NULL,
    processed_at TIMESTAMP NOT NULL DEFAULT now()
);
