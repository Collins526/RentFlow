-- Completes the Tenants and Tenancies schema.
--
-- V6 and V7 created their tables without the audit columns every entity inherits
-- from BaseEntity. With spring.jpa.hibernate.ddl-auto=validate that mismatch stops
-- the application from starting, so the columns are added here rather than by
-- rewriting migrations that may already have been applied locally.

ALTER TABLE tenants
    ADD COLUMN IF NOT EXISTS created_by UUID,
    ADD COLUMN IF NOT EXISTS updated_by UUID,
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP WITH TIME ZONE;

ALTER TABLE tenancies
    ADD COLUMN IF NOT EXISTS created_by UUID,
    ADD COLUMN IF NOT EXISTS updated_by UUID,
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP WITH TIME ZONE;

-- A unit can be physically occupied by at most one tenancy. The service layer also
-- rejects overlapping UPCOMING tenancies, but ACTIVE is the invariant that drives
-- unit occupancy and rent billing, so it is enforced by the database as well.
CREATE UNIQUE INDEX IF NOT EXISTS uq_tenancies_one_active_per_unit
    ON tenancies (unit_id) WHERE status = 'ACTIVE' AND deleted_at IS NULL;

-- Supports the overlap probe run on every create/update: unit + date window.
CREATE INDEX IF NOT EXISTS idx_tenancies_unit_window
    ON tenancies (unit_id, start_date, end_date) WHERE deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_tenancies_org_status
    ON tenancies (organization_id, status) WHERE deleted_at IS NULL;
