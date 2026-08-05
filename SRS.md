# Software Requirements Specification (SRS) - RentFlow

## 1. Introduction
RentFlow is a smart SaaS platform designed for property management and rent collection. It supports both residential and commercial properties.

## 2. Target Audience
- Platform Administrator
- Organization Owner
- Property Manager
- Accountant
- Maintenance Technician
- Tenant

## 3. Key Business Rules
1. **Move-in requirements**: Security deposit and first month's rent paid prior to occupation.
2. **Security Deposits**: Kept strictly separate from rent. Deductions applied only upon move-out inspection. 
3. **Advance Payments**: Properly allocated to future months without duplicating invoices.
4. **Historical Data**: Never deleted (soft delete strategy applied).
5. **Multi-Tenancy**: Complete data isolation between organizations. Payments linked to tenancies, not individual tenants.

## 4. Modules
1. Authentication
2. User Management
3. Role & Permission Management
4. Organization Management
5. Property Management
6. Block Management
7. Unit Management
8. Tenant Management
9. Tenancy Management
10. Lease Management
11. Rent Billing & Ledger
12. Payments & Advance Allocation
13. Security Deposits & Refunds
14. Inspections (Move-in / Move-out)
15. Maintenance
16. Documents & Notifications
17. Reports & Analytics
