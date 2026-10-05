ALTER TABLE organizations
    ADD COLUMN plan_code VARCHAR(20),
    ADD COLUMN subscription_status VARCHAR(20),
    ADD COLUMN trial_started_at TIMESTAMPTZ,
    ADD COLUMN trial_ends_at TIMESTAMPTZ;