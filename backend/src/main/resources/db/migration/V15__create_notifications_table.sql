CREATE TABLE IF NOT EXISTS notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL,
    tenant_id UUID,
    tenancy_id UUID,
    unit_id UUID,
    title VARCHAR(150) NOT NULL,
    message TEXT NOT NULL,
    channel VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    recipient_address VARCHAR(255),
    sent_at TIMESTAMPTZ,
    delivered_at TIMESTAMPTZ,
    read_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_notifications_organization_id ON notifications (organization_id);
CREATE INDEX IF NOT EXISTS idx_notifications_tenant_id ON notifications (tenant_id);
CREATE INDEX IF NOT EXISTS idx_notifications_unit_id ON notifications (unit_id);
CREATE INDEX IF NOT EXISTS idx_notifications_status ON notifications (status);
