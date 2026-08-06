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

- Domain: core entities, enums, and business rules
- Application: use cases and orchestration
- API: REST controllers, request/response DTOs, and exception translation
- Infrastructure: persistence, external integrations, and scheduled jobs

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

### Current entity approach

- `Employee` uses single-table inheritance with a discriminator column.
- Roles are modeled with the `Role` enum.
- Task, sprint, reminder, and comment timestamps are stored in the entity layer to support auditing and timeline views.
- Collections are lazy by default to avoid loading more data than the current use case needs.

## Persistence and Database Integration

The backend currently uses Spring Data JPA with an H2 in-memory database for local development and fast iteration.

Current persistence configuration:

- `spring.jpa.hibernate.ddl-auto=update` to evolve the schema during development
- `spring.h2.console.enabled=true` for quick inspection while building the app
- `spring.jpa.show-sql=true` to make ORM behavior visible during development

### Recommended database direction

For local development, H2 is fine. For anything beyond the prototype stage, the application should move to a production-grade relational database such as PostgreSQL.

That transition matters because this domain depends on:

- relational links between tasks, teams, employees, and sprints
- indexed lookups by assignee, team, sprint, status, and due date
- reliable transaction handling for comment and reminder workflows

### Tables and mapping considerations

The current entities suggest the following persistence pattern:

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

## Application Flow

The intended request flow is:

1. API layer receives a request and validates input.
2. Application layer executes the use case and enforces business rules.
3. Domain entities carry state and relationships.
4. Infrastructure layer persists changes or triggers integration side effects.

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

- define repositories for the core entities
- add DTOs for task, sprint, team, comment, and reminder operations
- implement application use cases for the main workflows
- expose REST controllers under the API layer
- add exception handling and validation responses
- replace H2 with a production database profile when deployment work begins

## Summary

The architecture is intentionally simple: model the collaboration domain in JPA entities, keep business orchestration in the application layer, expose the workflow through REST, and isolate persistence and integrations in infrastructure. That gives TeamFlow Lite a clean path from prototype to a maintainable collaboration backend.