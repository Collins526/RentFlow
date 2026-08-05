# AI Development Rules

## Architectural Guidelines
1. **Clean Architecture**: Strictly enforce separation of concerns in the backend (Controller -> Service -> Repository).
2. **Feature-Based Architecture**: Use standalone components, signals, and lazy loading in the frontend.
3. **Data Protection**: Expose DTOs only; entities must remain hidden behind the Service layer.

## Development Constraints
1. **No Placeholders**: Write fully functional, production-ready code. No TODOs.
2. **Quality over Speed**: Ensure scalability, performance, and maintainability.
3. **Sequential Delivery**: Build ONE module at a time following the specified order. Wait for approval before generating code.
4. **Database Integrity**: UUID primary keys, standardized audit fields (id, createdAt, updatedAt, createdBy, updatedBy, status, deletedAt). Soft deletes everywhere possible.
