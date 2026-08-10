ALTER TABLE payments
    ADD COLUMN phone_number VARCHAR(32),
    ADD COLUMN external_reference VARCHAR(255);

CREATE INDEX idx_payments_phone_number ON payments(phone_number);
