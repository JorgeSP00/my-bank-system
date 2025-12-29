CREATE UNIQUE INDEX IF NOT EXISTS ux_account_account_number
ON account (account_number);

CREATE INDEX IF NOT EXISTS idx_outbox_event_status_created_at 
ON outbox_event (status, created_at);