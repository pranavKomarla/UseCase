# TeamFlow Lite Architecture

## Purpose

TeamFlow Lite is a lightweight collaboration backend for small teams. The goal is to support a realistic workflow without overcomplicating the domain:

- create and manage tasks
- assign owners and reporters
- track task status and priority
- group work into teams and sprints
- add comments to tasks
- optionally create reminders for calendar or email delivery

This document describes the intended architecture of the backend, how the domain model maps to persistence, and the design decisions that should remain visible as the project grows.

## High-Level Architecture

The codebase follows a layered structure:

- Domain: pure business objects, enums, and rules
- Application: use cases and orchestration
- API: REST controllers, request/response DTOs, and exception translation
- Infrastructure: persistence, external integrations, mappers, and scheduled jobs

The current source tree already reflects this split, even though only the domain layer is populated today.

## Domain Model

The domain layer centers on a few core concepts:

- `Team`: owns members and sprints
- `Employee`: abstract base type for people in the system, with role-specific subtypes
- `Task`: the main work item, assigned to an employee and associated with a team and optional sprint
- `Sprint`: time-boxed work container with a goal and status
- `Comment`: discussion attached to a task
- `Reminder`: notification trigger attached to a task

### Important relationships

- A team has many employees.
- A team has many sprints.
- A sprint belongs to one team and contains many tasks.
- A task belongs to one team, may belong to one sprint, and may have one assignee and one reporter.
- A task has many comments.
- A reminder belongs to one task.

### Production-grade boundary

- The domain model should not depend on JPA annotations.
- Persistence-specific objects should live in the infrastructure layer.
- Mappers should translate between domain objects and database-backed data objects.
- This keeps the business model stable if the persistence strategy changes later.

### Recommended object split

- Domain objects: business-focused classes with rules and relationships
- Persistence data objects: JPA-backed classes mapped to PostgreSQL tables
- DTOs: request and response shapes used by the API layer

The current code uses JPA annotations directly on the domain classes, which is fine for a prototype, but the longer-term target should be this separation.

### Employee capability contract

The `EmployeeFunctionality` interface should be treated as the role capability contract for the domain.

That means:

- it declares the operations every employee type must support
- concrete employee classes such as manager, developer, and team lead implement or inherit those behaviors
- application services can ask an employee what they are allowed to do without hardcoding role checks everywhere

This is a good fit for the current model because each employee subtype can express its own rules for task creation, assignment, status movement, sprint closure, and commenting.

If the domain later moves away from JPA annotations, the interface should stay in the domain layer and remain independent from persistence details.

## Persistence and Database Integration

The backend should use PostgreSQL as the production database, with Spring Data JPA or another persistence adapter confined to the infrastructure layer.

For local development, H2 can remain a convenience database, but it should not define the architecture.

Recommended persistence split:

- PostgreSQL for persistent storage
- Infrastructure-layer data objects for table mapping
- Repositories that work with data objects rather than domain classes directly
- Mappers in the infrastructure layer to move data between persistence and domain models

If you keep JPA in the project, the annotations should sit on the persistence data objects, not the domain model.

That transition matters because this domain depends on:

- relational links between tasks, teams, employees, and sprints
- indexed lookups by assignee, team, sprint, status, and due date
- reliable transaction handling for comment and reminder workflows

### Tables and mapping considerations

The persistence model should likely map to the following tables:

- one table for `team`
- one table for `employee` using single-table inheritance
- one table for `task`
- one table for `sprint`
- one table for `comment`
- one table for `reminder`

Key schema concerns to document early:

- unique email constraint for employees
- foreign keys for task ownership and team membership
- indexes on task status, assignee, team, sprint, and due date
- enum storage as strings rather than ordinals
- cascade behavior only where child records are truly owned by the parent
- audit columns such as `created_at` and `updated_at` where useful

## Application Flow

The intended request flow is:

1. API layer receives a request and validates input.
2. Application layer executes the use case and enforces business rules.
3. Domain objects carry state and relationships.
4. Infrastructure layer maps to persistence data objects and persists changes or triggers integration side effects.

This separation keeps controllers thin and makes the business workflow easier to test.

### Example use cases

- create team
- add employee to team
- create sprint for a team
- create task and assign owner
- change task status
- add comment to task
- schedule reminder for a task
- mark reminder as sent after delivery

## Reminder and Integration Strategy

Reminder handling should be treated as an integration concern rather than a core task mutation.

Recommended approach:

- store reminder intent in the database
- use a scheduler to find due reminders
- dispatch reminders through email, calendar, or both depending on configuration
- mark reminders as sent only after successful delivery

This makes reminder delivery idempotent and easier to retry.

## Necessities to Decide Early

These are the topics that are most important to document and settle early:

- authentication and authorization model
- whether employees are managed as human users, system identities, or both
- how task ownership and reporting rules should work
- whether a task can move across teams or only within one team
- whether sprint membership is exclusive for tasks
- what happens when a team, sprint, or employee is deleted
- how reminder retries and failures are handled
- whether comment editing and deletion are allowed
- whether audit fields are required for all entities

## Near-Term Implementation Plan

The next practical backend steps are:

- define repositories for the persistence data objects
- add domain objects and mappers if the current model stays JPA-free in the long term
- add DTOs for task, sprint, team, comment, and reminder operations
- implement application use cases for the main workflows
- expose REST controllers under the API layer
- add exception handling and validation responses
- configure PostgreSQL alongside a local H2 profile for development

## Summary

The architecture should be intentionally simple but more production-ready: keep the domain model free of persistence concerns, use PostgreSQL as the real database, place JPA data objects and repositories in infrastructure, and translate through mappers so the business model stays stable as the storage layer evolves.