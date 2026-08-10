-- Creates rent billing, payment, ledger and advance payment tables.

CREATE TABLE rent_invoices (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    organization_id UUID NOT NULL REFERENCES organizations(id),
    tenancy_id UUID REFERENCES tenancies(id),
    lease_id UUID,
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    unit_id UUID NOT NULL REFERENCES units(id),
    period_start DATE,
    period_end DATE,
    due_date DATE,
    amount NUMERIC(14,2),
    status VARCHAR(50) NOT NULL,
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by UUID,
    updated_by UUID,
    deleted_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_rent_invoices_organization_id ON rent_invoices(organization_id);
CREATE INDEX idx_rent_invoices_tenant_id ON rent_invoices(tenant_id);
CREATE INDEX idx_rent_invoices_unit_id ON rent_invoices(unit_id);
CREATE INDEX idx_rent_invoices_status ON rent_invoices(status);

CREATE TABLE payments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    organization_id UUID NOT NULL REFERENCES organizations(id),
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    unit_id UUID REFERENCES units(id),
    invoice_id UUID REFERENCES rent_invoices(id),
    amount NUMERIC(14,2) NOT NULL,
    method VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    reference VARCHAR(500),
    payment_date DATE,
    paid_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by UUID,
    updated_by UUID,
    deleted_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_payments_organization_id ON payments(organization_id);
CREATE INDEX idx_payments_tenant_id ON payments(tenant_id);
CREATE INDEX idx_payments_invoice_id ON payments(invoice_id);
CREATE INDEX idx_payments_status ON payments(status);

CREATE TABLE rent_ledger_entries (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    organization_id UUID NOT NULL REFERENCES organizations(id),
    tenancy_id UUID REFERENCES tenancies(id),
    lease_id UUID,
    invoice_id UUID REFERENCES rent_invoices(id),
    payment_id UUID REFERENCES payments(id),
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    unit_id UUID REFERENCES units(id),
    entry_date DATE NOT NULL,
    type VARCHAR(50) NOT NULL,
    description TEXT,
    amount NUMERIC(14,2) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by UUID,
    updated_by UUID,
    deleted_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_rent_ledger_entries_organization_id ON rent_ledger_entries(organization_id);
CREATE INDEX idx_rent_ledger_entries_tenant_id ON rent_ledger_entries(tenant_id);
CREATE INDEX idx_rent_ledger_entries_unit_id ON rent_ledger_entries(unit_id);
CREATE INDEX idx_rent_ledger_entries_entry_date ON rent_ledger_entries(entry_date);
CREATE INDEX idx_rent_ledger_entries_type ON rent_ledger_entries(type);

CREATE TABLE advance_payments (
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

CREATE INDEX idx_advance_payments_organization_id ON advance_payments(organization_id);
CREATE INDEX idx_advance_payments_tenant_id ON advance_payments(tenant_id);
CREATE INDEX idx_advance_payments_status ON advance_payments(status);
