# ADR-0001: Monorepo Structure & Architecture Foundation

- **Status:** Accepted
- **Date:** 2026-11-05
- **Stage:** Stage 0

## Context
J.U.N.E. is an offline-first personal AI assistant targeted at Android, Windows, and the Web. The project started with a minimal console-based Python prototype (`backend/main.py`), which lacked testing, typed contracts, modularity, an independent permission model, and multi-client support.

## Decision
1. **Monorepo Architecture:** Use a single repository containing `apps/` (starting with Android in Stage 1), `backend/` (FastAPI modular monolith), `shared/` (data contracts and protocol schemas), `tests/`, and `docs/`.
2. **Android-First Strategy:** Focus client development primarily on Android (Kotlin + Jetpack Compose) for mobile utility, offline capability, and device integrations, with Windows and Web interfaces to follow in Stage 7.
3. **Independent Permission Engine:** Decouple intent understanding from action execution. The AI proposes structured actions; an independent policy engine validates and authorizes them.
4. **₹0 Budget Baseline:** Rely on open-source libraries, local SQLite/Room storage, and local or BYOK (Bring-Your-Own-Key) inference models.

## Consequences & Trade-offs
- Monorepo requires structured boundaries to avoid coupling. Python packages (`backend`, `shared`) are strictly scoped.
- Android-first prioritizes mobile/offline UX over immediate cross-platform parity.
- An independent permission engine adds an explicit authorization step before any side-effect execution.

## Security, Privacy, and Cost Implications
- Privacy-first: Raw private data is not forwarded to external models without explicit context needs.
- Cost: ₹0 initial operational cost by leveraging local persistence, local inference endpoints, and free/BYOK APIs.
- Permissions: Every state-changing action requires an auditable authorization check.

