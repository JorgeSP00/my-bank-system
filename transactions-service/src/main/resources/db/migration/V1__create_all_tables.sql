-- Migration: create transaction table
CREATE TABLE transaction (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    from_account_id UUID NOT NULL,
    to_account_id UUID NOT NULL,
    amount NUMERIC(19, 2) NOT NULL,
    type VARCHAR(20) NOT NULL,
    description VARCHAR(255) NOT NULL,
    from_account_version_id BIGINT NOT NULL,
    to_account_version_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    observations VARCHAR(255) NOT NULL
);

-- Migration: create account table
CREATE TABLE account (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    account_number VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL,
    version_id BIGINT NOT NULL       
);


-- Migration: create outbox_event table for Outbox pattern
CREATE TABLE IF NOT EXISTS outbox_event (
    id UUID PRIMARY KEY,
    aggregate_type VARCHAR(255) NOT NULL,
    aggregate_id UUID NOT NULL,
    type VARCHAR(255) NOT NULL,
    topic VARCHAR(255),
    payload TEXT NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    sent_at TIMESTAMP
);