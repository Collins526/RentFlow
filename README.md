# RentFlow

Smart Rent Payment & Property Management System

## Overview
RentFlow is a cloud-based Software-as-a-Service (SaaS) platform that enables landlords, property managers, and tenants to manage the complete rental lifecycle digitally. The platform automates rent collection, tenant management, inspections, maintenance, reporting, security deposits, and communication.

## Core Features
- Multi-Tenant SaaS Design (Organization isolation)
- Role-Based Access Control
- Automated Rent Billing & Invoicing
- M-Pesa Integration for Payments
- Security Deposit Management & Refunds
- Property, Block, and Unit Management
- Maintenance and Inspection Tracking
- Reporting and Analytics

## Tech Stack
**Backend**: Java 21, Spring Boot 3/4, Spring Security, Spring Data JPA, PostgreSQL
**Frontend**: Angular 21, TypeScript, Tailwind CSS, Angular Material
**Database**: PostgreSQL
**Infrastructure**: Docker, Nginx, GitHub Actions

## Local Supabase Database Setup

The backend loads environment variables from `backend/.env`. A placeholder file is included there; replace its values with your own credentials and never commit the populated file.

1. In the Supabase Dashboard, open your project and select **Connect**.
2. Choose **Session pooler** and copy the host, port, database name, and user. The session pooler is suitable for Spring Boot and Flyway migrations.
3. Update `backend/.env`:
	- `SPRING_DATASOURCE_URL`: use `jdbc:postgresql://<host>:<port>/<database>?sslmode=require`.
	- `SPRING_DATASOURCE_USERNAME`: use the Supabase pooler user, usually `postgres.<project-ref>`.
	- `SPRING_DATASOURCE_PASSWORD`: use the database password configured for the Supabase project.
4. Fill the M-Pesa values if you are using payment integration. `OPENAI_API_KEY` is optional.

Start the backend from its directory so Spring Dotenv finds the file:

```powershell
Set-Location backend
.\mvnw spring-boot:run
```

If the Maven wrapper is not present, use `mvn spring-boot:run` instead. Supabase database passwords can be rotated from **Project Settings -> Database**.

Start the frontend from its directory:

```powershell
Set-Location frontend
npm start
```
Admin logins
admin@rentflow.com
RentFlow123!

colinskibet526@gmail.com
Colins@526

mannukiptoo@gmail.com
qvqV4Y(a+A1y