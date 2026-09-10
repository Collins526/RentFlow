CREATE TABLE IF NOT EXISTS advance_payments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    organization_id UUID NOT NULL REFERENCES organizations(id),
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    lease_id UUID,
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

CREATE INDEX IF NOT EXISTS idx_advance_payments_organization_id ON advance_payments(organization_id);
CREATE INDEX IF NOT EXISTS idx_advance_payments_tenant_id ON advance_payments(tenant_id);
CREATE INDEX IF NOT EXISTS idx_advance_payments_status ON advance_payments(status);
