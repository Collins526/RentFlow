CREATE TABLE tenants (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    organization_id UUID NOT NULL REFERENCES organizations(id) ON DELETE CASCADE,
    tenant_type VARCHAR(50) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    company_name VARCHAR(200),
    
    email VARCHAR(255) NOT NULL,
    phone_number VARCHAR(50) NOT NULL,
    
    identification_type VARCHAR(50),
    identification_number VARCHAR(100),
    
    emergency_contact_name VARCHAR(150),
    emergency_contact_phone VARCHAR(50),
    
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE'
);

CREATE INDEX idx_tenants_organization_id ON tenants(organization_id);
CREATE INDEX idx_tenants_email ON tenants(email);
