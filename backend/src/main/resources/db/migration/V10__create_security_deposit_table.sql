CREATE TABLE security_deposits (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    organization_id UUID NOT NULL REFERENCES organizations(id),
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    tenancy_id UUID REFERENCES tenancies(id),
    lease_id UUID,
    unit_id UUID REFERENCES units(id),
    amount NUMERIC(14,2) NOT NULL,
    remaining_amount NUMERIC(14,2) NOT NULL,
    status VARCHAR(50) NOT NULL,
    note TEXT,
    received_date DATE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by UUID,
    updated_by UUID,
    deleted_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_security_deposits_organization_id ON security_deposits(organization_id);
CREATE INDEX idx_security_deposits_tenant_id ON security_deposits(tenant_id);
CREATE INDEX idx_security_deposits_tenancy_id ON security_deposits(tenancy_id);
CREATE INDEX idx_security_deposits_status ON security_deposits(status);
