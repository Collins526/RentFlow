CREATE TABLE IF NOT EXISTS documents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id UUID NOT NULL,
    tenant_id UUID NOT NULL,
    tenancy_id UUID,
    lease_id UUID,
    unit_id UUID,
    title VARCHAR(150) NOT NULL,
    description TEXT,
    document_type VARCHAR(100),
    document_url VARCHAR(1000),
    file_name VARCHAR(255),
    file_type VARCHAR(100),
    file_size BIGINT,
    uploaded_date DATE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_documents_organization_id ON documents (organization_id);
CREATE INDEX IF NOT EXISTS idx_documents_tenant_id ON documents (tenant_id);
CREATE INDEX IF NOT EXISTS idx_documents_unit_id ON documents (unit_id);
CREATE INDEX IF NOT EXISTS idx_documents_tenancy_id ON documents (tenancy_id);
CREATE INDEX IF NOT EXISTS idx_documents_document_type ON documents (document_type);
