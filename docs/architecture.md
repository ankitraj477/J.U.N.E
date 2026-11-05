# J.U.N.E. Architecture Overview

## 1. System Philosophy
J.U.N.E. operates as a hybrid, local-first, modular monolith. The system separates user understanding from action authorization.

```
+--------------------------------------------------------------------------+
|                                User / Client                             |
|              (Android Kotlin / Windows C# / Web React)                   |
+--------------------------------------------------------------------------+
                                    |
                                    v
+--------------------------------------------------------------------------+
|                            Backend Service                               |
|                                                                          |
|  1. Request Intake & Intent Classification                               |
|  2. Structured Action Proposal Generation (ActionProposal)               |
|  3. Independent Policy & Permission Engine (PermissionCheck)             |
|  4. User Approval Flow (if Medium/High Risk)                             |
|  5. Controlled Tool/Adapter Execution                                    |
|  6. Result Verification & Privacy-Safe Audit                             |
+--------------------------------------------------------------------------+
                                    |
        +---------------------------+---------------------------+
        |                           |                           |
        v                           v                           v
+---------------+           +---------------+           +---------------+
| Local Storage |           | Pluggable LLM |           | Integrations  |
| (SQLite/Room) |           | (Local/BYOK)  |           | (Google/MS)   |
+---------------+           +---------------+           +---------------+
```

## 2. Core Components

- **Client Layer (`apps/`):**
  - **Android (`apps/android`):** Primary client. Kotlin + Jetpack Compose, Room database, Keystore, WorkManager.
  - **Windows (`apps/windows`):** C# / .NET desktop client for controlled system automation.
  - **Web (`apps/web`):** React + TypeScript control center.

- **Backend Layer (`backend/`):**
  - Modular monolith built with Python and FastAPI.
  - Independent permission engine decoupled from model outputs.
  - Pluggable model router (local Ollama / ONNX and cloud BYOK).

- **Contracts Layer (`shared/contracts/`):**
  - Typed, validated schemas for actions (`ActionProposal`, `RiskLevel`, `ActionStatus`).
  - Authorization contracts (`PermissionDecision`, `PermissionCheckRequest`, `PermissionCheckResult`).

## 3. The 9-Step Action Execution Pipeline
1. **Understand:** Parse request and context.
2. **Plan:** Construct structured `ActionProposal`.
3. **Validate:** Schema, parameters, and constraints check.
4. **Authorize:** Independent permission engine checks policies and scopes.
5. **Approve:** Solicit explicit confirmation for medium/high-risk actions.
6. **Execute:** Dispatch to narrow, allowlisted adapters.
7. **Verify:** Confirm actual outcome with external systems.
8. **Audit:** Record privacy-safe event.
9. **Report:** Deliver factual result back to the user.

