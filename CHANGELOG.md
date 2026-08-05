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
