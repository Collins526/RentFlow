ALTER TABLE maintenance_requests
    ADD COLUMN IF NOT EXISTS attachment_data TEXT,
    ADD COLUMN IF NOT EXISTS attachment_name VARCHAR(255),
    ADD COLUMN IF NOT EXISTS attachment_type VARCHAR(100),
    ADD COLUMN IF NOT EXISTS attachment_size BIGINT;