CREATE TABLE units (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    property_id UUID NOT NULL REFERENCES properties(id),
    block_id UUID REFERENCES blocks(id) ON DELETE SET NULL,
    unit_number VARCHAR(50) NOT NULL,
    type VARCHAR(50) NOT NULL,
    floor_number INT,
    bedrooms INT NOT NULL DEFAULT 0,
    bathrooms INT NOT NULL DEFAULT 0,
    size_sq_ft NUMERIC(10,2),
    rent_amount NUMERIC(14,2) NOT NULL,
    deposit_amount NUMERIC(14,2),
    occupancy_status VARCHAR(30) NOT NULL DEFAULT 'VACANT',
    furnished BOOLEAN NOT NULL DEFAULT FALSE,
    water_meter_number VARCHAR(50),
    electricity_meter_number VARCHAR(50),
    description TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE,
    created_by UUID,
    updated_by UUID,
    deleted_at TIMESTAMP WITH TIME ZONE
);

-- Unit numbers are unique per property, but only among live rows so that a
-- soft-deleted "A101" does not permanently reserve that number.
CREATE UNIQUE INDEX uq_units_property_number
    ON units (property_id, LOWER(unit_number)) WHERE deleted_at IS NULL;

CREATE INDEX idx_units_property_id ON units (property_id);
CREATE INDEX idx_units_block_id ON units (block_id);
CREATE INDEX idx_units_occupancy_status ON units (occupancy_status);
