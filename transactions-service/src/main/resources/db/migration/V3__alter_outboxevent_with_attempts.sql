ALTER TABLE IF EXISTS outbox_event
    ADD COLUMN attempts integer;