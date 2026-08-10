CREATE TABLE IF NOT EXISTS maintenance_requests (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL,
    tenant_id UUID NOT NULL,
    unit_id UUID,
    tenancy_id UUID,
    title VARCHAR(150) NOT NULL,
    description TEXT,
    priority VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    requested_date DATE,
    scheduled_date DATE,
    completed_date DATE,
    assigned_to VARCHAR(150),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_maintenance_requests_organization_id ON maintenance_requests (organization_id);
CREATE INDEX IF NOT EXISTS idx_maintenance_requests_tenant_id ON maintenance_requests (tenant_id);
CREATE INDEX IF NOT EXISTS idx_maintenance_requests_unit_id ON maintenance_requests (unit_id);
CREATE INDEX IF NOT EXISTS idx_maintenance_requests_tenancy_id ON maintenance_requests (tenancy_id);
CREATE INDEX IF NOT EXISTS idx_maintenance_requests_status ON maintenance_requests (status);
