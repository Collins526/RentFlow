CREATE TABLE IF NOT EXISTS move_out_inspections (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL,
    tenant_id UUID NOT NULL,
    tenancy_id UUID,
    lease_id UUID,
    unit_id UUID,
    inspection_date DATE,
    inspector_name VARCHAR(150),
    status VARCHAR(50) NOT NULL,
    findings TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_move_out_inspections_organization_id ON move_out_inspections (organization_id);
CREATE INDEX IF NOT EXISTS idx_move_out_inspections_tenant_id ON move_out_inspections (tenant_id);
CREATE INDEX IF NOT EXISTS idx_move_out_inspections_tenancy_id ON move_out_inspections (tenancy_id);
CREATE INDEX IF NOT EXISTS idx_move_out_inspections_lease_id ON move_out_inspections (lease_id);
CREATE INDEX IF NOT EXISTS idx_move_out_inspections_unit_id ON move_out_inspections (unit_id);
