ALTER TABLE users
    ADD COLUMN IF NOT EXISTS tenant_id UUID,
    ADD COLUMN IF NOT EXISTS unit_id UUID;

ALTER TABLE users
    ADD CONSTRAINT IF NOT EXISTS fk_users_tenant
    FOREIGN KEY (tenant_id) REFERENCES tenants(id);

ALTER TABLE users
    ADD CONSTRAINT IF NOT EXISTS fk_users_unit
    FOREIGN KEY (unit_id) REFERENCES units(id);

CREATE INDEX IF NOT EXISTS idx_users_tenant_id ON users(tenant_id);
CREATE INDEX IF NOT EXISTS idx_users_unit_id ON users(unit_id);
