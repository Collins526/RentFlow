# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]
### Added
- Initial project structure created.
- Project documentation established.
- **[Module 1]** Implemented Authentication Module (Spring Security, JWT, Refresh Tokens).
- Scaffolded Angular 18 frontend with TailwindCSS and Angular Material.
- Integrated Login and Registration components with Reactive Forms and Signals.
- **[Module 4]** Implemented Organization Management (multi-tenant foundation).
- Refactored Auth Service to automatically create Organizations upon owner registration.
- Added Organization Settings frontend component with data loading/updating.
- **[Module 5]** Implemented Property Management module with multi-tenancy enforcement.
- Added properties listing and creation/editing forms on the frontend.
- **[Module 6]** Implemented Blocks Management (sub-divide properties into buildings/wings).
- Added Property Detail view with nested blocks table and block creation/editing forms.
- Implemented cascading multi-tenancy verification through parent Property ownership.
- **[Module 7]** Implemented Unit Management, the first billable entity in the hierarchy.
- Units attach to a Property directly or, optionally, to one of its Blocks.
- Added bulk unit generation (prefix + start number + count with zero padding) validated atomically against existing and in-batch duplicates.
- Introduced `UnitType` and `OccupancyStatus` enums; `OCCUPIED` is reserved for the upcoming Tenancy module and cannot be set manually.
- Applied soft deletes to Units so tenancy, ledger and inspection history is never orphaned.
- Added a per-property occupancy summary endpoint (totals, vacancy counts, potential rent).
- Added Units table with occupancy filtering, single and bulk creation dialogs, and a Block Detail page listing that block's units.
- **[Module 8]** Implemented Tenant Management for individual and corporate tenants.
- **[Module 9]** Implemented Tenancy Management, binding a Tenant to a Unit for a date range.
- Tenancies own unit occupancy: activating one marks its unit `OCCUPIED`, ending or deleting one releases it back to `VACANT`.
- Added overlap detection so a unit cannot hold two tenancies whose date windows intersect; `PAST` and `EVICTED` tenancies never block a re-let.
- Enforced status/date coherence (`ACTIVE` must have started, `UPCOMING` must start in the future, terminated tenancies require an end date) so downstream rent billing cannot derive periods from contradictory records.
- Backed the one-active-tenancy-per-unit rule with a partial unique index in addition to the service check.
- Rent and deposit are inherited from the unit when omitted, on both create and update.
- Added a dedicated end-tenancy action that records a close date and either `PAST` or `EVICTED`, kept separate from editing so it cannot silently rewrite the tenant, unit or agreed rent.
- Applied soft deletes to Tenancies so rent ledger, deposit and inspection history is never orphaned.
- Added tenancy list filtering by status, tenant or unit, with page names resolved in two queries instead of two per row.
- Added the Tenancies page with a status filter, a property-then-unit picker that hides unavailable units, and inline surfacing of server validation messages.

- **[Shell]** Replaced the Angular starter page with a real application shell.
- Added `LayoutComponent`, `SidebarComponent`, `TopbarComponent` and `BreadcrumbsComponent`; every authenticated page is now a child of the layout route, so navigation is declared once.
- The sidebar docks as a full rail or an icon-only rail on desktop (the choice is remembered) and becomes an overlay drawer on handsets that closes on navigation.
- Breadcrumbs are derived from each route's `data.breadcrumb`, so a route opts in by declaring a label rather than by registering anywhere.
- Added role-based navigation: sidebar items are filtered against the signed-in user's roles, and a section heading disappears once all of its items are hidden.
- Added `roleGuard`, mirroring each controller's `@PreAuthorize` rules, plus a `guestGuard` that keeps signed-in users off the auth pages. Signed-out users are sent to login with a `returnUrl`; signed-in users lacking a role get a Forbidden page instead of a login loop.
- Added a dashboard landing page with property, unit, tenancy and tenant tiles, an occupancy breakdown and a monthly rent roll including vacancy cost.
- Added `GET /api/v1/dashboard/summary`, aggregating the portfolio in SQL so the dashboard costs one request rather than one per property.
- Added a shared UI kit under `shared/ui`: `DataTableComponent` (declarative columns with `rfCell` template escape hatches, plus built-in loading, error, empty and pagination states), `PageHeaderComponent`, `FilterBarComponent`, `StatusChipComponent`, and loading/skeleton/empty/error state components.
- Added `ToastService` over MatSnackBar with success, info, warning and error variants, and a shared `resolveApiMessage` helper that unwraps the backend's `ApiResponse` error shape in one place.
- Migrated the Tenants and Tenancies lists onto the shared data table, filter bar and toasts.
- Added Forbidden and Not Found pages, and a wildcard route so unknown URLs no longer render blank.

### Fixed
- Added the missing `SecurityUtils` helper referenced by the Tenant and Tenancy services; the backend did not compile without it.
- Login redirected to `/dashboard`, which did not exist; the route now resolves.
- Tenant and Tenancy forms swallowed server errors silently. Both now surface the server's message inline so the dialog stays open for correction.
- Session reads no longer assume Web Storage is available or intact: a corrupt entry previously threw inside a root service constructor and took down app bootstrap.
- Added the audit columns (`created_by`, `updated_by`, `deleted_at`) omitted from the `tenants` and `tenancies` tables, which failed Hibernate's `validate` check and prevented the application from starting.
- Replaced `hasAuthority('TENANT_*')` / `hasAuthority('TENANCY_*')` checks with the project's role-based rules; those permissions were never seeded, so every tenant and tenancy endpoint returned 403.
- Corrected broken relative import paths in the Tenants and Tenancies components, which broke the frontend build.
- Standardised the Tenant and Tenancy APIs on the `ApiResponse` envelope used by the rest of the backend, removing the speculative unwrapping in the frontend.
- Removed routes that pointed at dialog-only components (`/tenants/new`, `/tenancies/:id/edit`), which threw on navigation.
