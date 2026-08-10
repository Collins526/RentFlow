CREATE TABLE tenancies (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    organization_id UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    unit_id UUID NOT NULL REFERENCES units(id),
    
    start_date DATE NOT NULL,
    end_date DATE,
    
    status VARCHAR(50) NOT NULL,
    
    rent_amount NUMERIC(14, 2),
    security_deposit_amount NUMERIC(14, 2)
);

CREATE INDEX idx_tenancies_organization_id ON tenancies(organization_id);
CREATE INDEX idx_tenancies_tenant_id ON tenancies(tenant_id);
CREATE INDEX idx_tenancies_unit_id ON tenancies(unit_id);
