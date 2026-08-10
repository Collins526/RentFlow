CREATE TABLE move_in_inspections (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    organization_id UUID NOT NULL REFERENCES organizations(id),
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    tenancy_id UUID REFERENCES tenancies(id),
    lease_id UUID,
    unit_id UUID REFERENCES units(id),
    inspection_date DATE,
    inspector_name VARCHAR(150),
    status VARCHAR(50) NOT NULL,
    findings TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by UUID,
    updated_by UUID,
    deleted_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_move_in_inspections_organization_id ON move_in_inspections(organization_id);
CREATE INDEX idx_move_in_inspections_tenant_id ON move_in_inspections(tenant_id);
CREATE INDEX idx_move_in_inspections_tenancy_id ON move_in_inspections(tenancy_id);
CREATE INDEX idx_move_in_inspections_unit_id ON move_in_inspections(unit_id);
CREATE INDEX idx_move_in_inspections_status ON move_in_inspections(status);
