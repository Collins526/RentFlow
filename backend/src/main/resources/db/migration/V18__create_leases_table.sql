CREATE TABLE IF NOT EXISTS leases (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL,
    tenancy_id UUID,
    tenant_id UUID NOT NULL,
    unit_id UUID NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE,
    status VARCHAR(50) NOT NULL,
    rent_amount NUMERIC(14,2),
    security_deposit_amount NUMERIC(14,2),
    terms VARCHAR(4000),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by UUID,
    updated_by UUID,
    deleted_at TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_leases_organization_id ON leases (organization_id);
CREATE INDEX IF NOT EXISTS idx_leases_tenant_id ON leases (tenant_id);
CREATE INDEX IF NOT EXISTS idx_leases_unit_id ON leases (unit_id);
CREATE INDEX IF NOT EXISTS idx_leases_tenancy_id ON leases (tenancy_id);
