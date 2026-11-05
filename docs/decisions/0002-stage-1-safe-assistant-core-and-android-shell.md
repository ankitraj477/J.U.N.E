# ADR-0002: Stage 1 Safe Assistant Core & Android Shell

- **Status:** Accepted
- **Date:** 2026-11-05
- **Stage:** Stage 1

## Context
Stage 1 requires delivering a working Android-first assistant shell with an independent backend permission engine, structured action proposal lifecycle, audit logging, and local-first persistence.

## Decision
1. **Independent Policy Engine:** Implemented `PermissionEngine` in `backend/permissions/engine.py`. It strictly gates actions based on risk level and explicitly granted scopes, remaining completely independent of LLM or conversational loops.
2. **Controlled Execution & Audit Model:** Actions can only be dispatched if authorized or explicitly approved. The `ExecutionRunner` records append-only `AuditEvent` records for all lifecycle events (`ACTION_PROPOSED`, `PERMISSION_EVALUATED`, `USER_APPROVED`, `EXECUTION_STARTED`, `EXECUTION_COMPLETED`, `EXECUTION_FAILED`).
3. **Android Shell & Local Persistence:** The mobile client (`apps/android`) is built with Kotlin and Jetpack Compose, utilizing Room/SQLite (`JuneDatabase`, `MessageEntity`, `ActionEntity`) for local offline data persistence.
4. **Interactive Action Approvals:** Actions tagged with `RiskLevel.HIGH` or unapproved `RiskLevel.MEDIUM` present interactive approval/cancellation controls to the user rather than auto-executing.

## Consequences & Trade-offs
- Offline actions persist locally in Room and can function without network connectivity.
- Backend and Android models use identical state transitions (`PROPOSED` -> `AUTHORIZED` -> `APPROVED` -> `COMPLETED`).

