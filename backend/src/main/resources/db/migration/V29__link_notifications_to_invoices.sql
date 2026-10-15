ALTER TABLE notifications
    ADD COLUMN IF NOT EXISTS invoice_id UUID;

CREATE INDEX IF NOT EXISTS idx_notifications_invoice_id
    ON notifications (invoice_id);

CREATE UNIQUE INDEX IF NOT EXISTS idx_notifications_invoice_in_app_unique
    ON notifications (invoice_id)
    WHERE invoice_id IS NOT NULL AND channel = 'IN_APP';
