ALTER TABLE documents
    ADD COLUMN IF NOT EXISTS cloudinary_public_id VARCHAR(500),
    ADD COLUMN IF NOT EXISTS cloudinary_resource_type VARCHAR(20);