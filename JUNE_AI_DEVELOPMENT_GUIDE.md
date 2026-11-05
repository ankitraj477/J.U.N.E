# J.U.N.E. — Product, Architecture & AI Development Guide

> **Purpose of this document:** This is the single source of truth for AI coding agents and human contributors working on J.U.N.E. Read it before proposing architecture, changing code, creating files, or implementing a feature. Follow the product principles, development workflow, stage gates, and engineering standards below.
>
> **Project status:** Product direction and high-level architecture have been agreed. The existing Python prototype has been inspected at source level, but the new architecture has not yet been implemented. Do not claim that planned components already exist.

---

## 1. What we are building

**J.U.N.E. is a personal AI assistant for Android, Windows, and the web.** It should understand the user's preferences, routines, priorities, communication style, and working patterns; help manage information and tasks; and carry out approved workflows across the user's devices and services.

J.U.N.E. is not just a chatbot. It is a personal assistant with:
- A conversational interface (text and voice).
- A privacy-conscious, persistent memory system.
- A permission and approval system.
- A task planner and controlled action-execution engine.
- Integrations with calendars, email, contacts, files, notifications, and supported device capabilities.
- Proactive reminders and recommendations.
- Cross-device synchronization.
- Android, Windows, and web clients.

### The central principle

> **“THE AI SHOULD LEARN ITS USER, but don't become the user.”**

J.U.N.E. should learn how the user thinks and works, but it must never assume the user's identity, authority, consent, or permission to act.

**Understanding the user is not authorization to act as the user.**

J.U.N.E. must:
- Identify itself as an AI when communicating with other people, including phone calls.
- Never impersonate the user or claim to be the user.
- Never treat learned preferences, past behavior, or inferred intent as permission.
- Never grant itself new permissions.
- Explain what it is doing and report the actual outcome.
- Let the user inspect, correct, export, and delete their data.

---

## 2. Product principles and non-negotiable constraints

1. **User remains in control.** AI proposes; a separate policy/permission layer authorizes.
2. **Privacy-first and local-first.** Keep private data on the user's device when practical. Send only the context needed for a cloud task.
3. **Hybrid operation.** Core functions should remain useful offline; cloud reasoning and remote services require connectivity.
4. **Honesty.** Never pretend an action succeeded, a test passed, a message was sent, or data was retrieved when it did not.
5. **Explicit permissions.** Permissions are scoped, understandable, revocable, and logged.
6. **Least privilege.** An integration receives only the access needed for its feature.
7. **Safe autonomy.** Routine low-risk workflows may run automatically only within user-defined rules.
8. **No impersonation.** J.U.N.E. can communicate on the user's behalf only where specifically authorized, and must disclose that it is an AI.
9. **Data ownership.** Provide usable export, backup, device revocation, and account/data deletion controls.
10. **Reliability over impressive demos.** Prefer a small, dependable feature over a broad, unreliable one.
11. **Zero-cost starting point.** Initial development and operation should target ₹0. Use free/open-source options and optional BYOK (bring your own key). Do not hide costs or assume free tiers provide unlimited or reliable service.
12. **Build incrementally.** Complete and test each stage before starting the next.

### Budget and service reality

The initial budget target is strictly **₹0**. Users may optionally provide their own API key for OpenAI, Gemini, Anthropic, or a compatible local model endpoint. Local models and free tiers should be preferred where suitable.

Do not promise that ₹0 can provide unlimited cloud AI, reliable carrier telephony, always-on background execution, or unlimited free hosting. Design transparent limits, local fallbacks, and provider portability. Never add a paid dependency or service without identifying the cost and getting approval.

---

## 3. Product capabilities

### 3.1 Conversation and personality
- Text and voice conversations.
- Adaptive tone: professional at work, friendly in personal contexts, direct in urgent situations, and transparent/polite when speaking to callers.
- Honesty, safety, privacy, and permission boundaries never change with personality.
- Female voice preference: calm, natural, intelligent, clear, and adjustable in speed.
- Prioritize English and Hindi, including mixed Hindi-English/code-switching. Add more languages as supported by the chosen speech/model stack.
- The user can interrupt speech and switch between voice and text.

### 3.2 Memory and learning
J.U.N.E. should learn useful patterns automatically, while keeping facts and assumptions distinct.

Memory layers:
- **Working memory:** current conversation and active task.
- **Episodic memory:** important past interactions and events.
- **Semantic memory:** stable facts about the user and relevant world knowledge.
- **Preference memory:** communication style, likes/dislikes, routines, and preferences.
- **Procedural memory:** how the user prefers recurring tasks to be performed.

Memory requirements:
- Store provenance/source, timestamp, and confidence where applicable.
- Distinguish user-stated facts from AI-inferred assumptions.
- Allow review, correction, and deletion.
- Do not automatically retain sensitive information without explicit consent.
- Apply configurable retention to conversations, extracted memories, call records, and logs.
- Memory never grants action permission.
- Retrieve only relevant memory for a task; do not automatically upload the entire memory store to a cloud model.

### 3.3 Tasks, reminders, and proactive assistance
- Create, update, complete, and track tasks and reminders.
- Read authorized calendars and identify conflicts.
- Notify the user about deadlines, pending replies, events, overdue tasks, and meaningful changes.
- Be proactive but not noisy: prioritize urgent items, group routine updates, learn notification preferences, and respect Focus/Office/Home/Sleep modes.
- Recommend actions; do not silently make commitments outside configured rules.

### 3.4 Email and communication
Initial Gmail/Outlook baseline:
- Read and summarize authorized mail.
- Categorize messages.
- Draft replies.
- Detect unanswered messages and track follow-ups.
- Maintain a history of follow-ups.

Sending, forwarding, deleting, archiving, and other state-changing actions require separate permissions. Auto-send is allowed only for explicitly pre-approved categories and configured templates/rules. Ask for approval outside those rules.

### 3.5 Phone-call assistant
Intended design: use the user's existing phone number with carrier conditional call forwarding (busy, unanswered, or unreachable), if the carrier and plan support it, to a J.U.N.E. telephony service.

The call assistant should:
- Clearly identify itself as an AI assistant.
- Understand the caller's reason for calling.
- Answer only questions covered by approved information/rules.
- Take messages and callback details.
- Handle predefined requests.
- Offer approved callback or call-transfer options.
- Summarize calls for the user.
- Escalate calls that match configured urgent-contact/rule criteria.

It must not impersonate the user. Follow applicable privacy, recording, and consent requirements. A claim of urgency from a caller is not automatically proof of an emergency. Carrier call answering requires an internet-connected telephony service; local voice processing alone cannot answer forwarded carrier calls.

### 3.6 Calendar management
- Read Google Calendar and Outlook Calendar when connected.
- Detect scheduling conflicts and suggest available times.
- Create or reschedule events only within explicitly configured rules.
- Ask before accepting/declining invitations or handling exceptions outside those rules.
- Consider working hours, travel time, and commitments only when the relevant data and permissions are available.
- A busy calendar alone does not prove the user is unavailable for a phone call.

### 3.7 Files and device operations
Within user-authorized locations, J.U.N.E. may search, index, create, edit, move, rename, organize, and (when authorized) delete files. Provide audit history and undo/versioning where practical. Confirm destructive bulk operations and irreversible changes.

**Windows:** deep but controlled integration may include application launching/management, file/folder operations, window management, supported system settings, PowerShell/command execution, and desktop automation. Never execute arbitrary AI-generated shell commands without validation and policy checks. Administrator access, installations, destructive operations, and sensitive actions require explicit approval.

**Android:** use native Android APIs for supported operations, app launching, notifications, authorized files, and supported communication/device features. Use Accessibility Service only for a specific capability that cannot be achieved through a normal API, and only after clear user activation. No root access. Android does not allow unrestricted control of every app.

### 3.8 Location awareness
If the user explicitly enables it:
- Support background location updates and geofencing/reminders within Android's platform rules.
- Use location for contextual suggestions, arrival estimates, and routines where useful.
- Show clear controls/indicators, pause/resume, and retention settings.
- Do not share location with external AI providers by default.
- Make battery impact and Android background-location limitations clear.
- Location access is optional and independently revocable.

### 3.9 Integrations
Planned initial integration categories:
- Google Calendar.
- Outlook Calendar.
- Gmail and Outlook Mail.
- Contacts.
- Location.
- Authorized files/storage.
- Android notifications.
- Built-in tasks and reminders.

Each integration has a separate permission scope. Connecting Gmail does not automatically grant access to contacts, files, or calendars.

### 3.10 Web control center
The web app should eventually provide:
- Assistant chat and supported voice interaction.
- Memory review and management.
- Integration connections and permissions.
- Tasks, reminders, and automations.
- Activity/audit history.
- Device management and revocation.
- Security and recovery settings.
- System health and usage information.

---

## 4. Autonomy and permission model

Autonomy is a product goal, but it is **bounded autonomy**, not unrestricted control.

### Permission tiers

- **Low risk:** may be pre-approved for routine actions, such as creating a local reminder or organizing a specifically authorized folder under configured rules.
- **Medium risk:** approval can be configured by action, app, contact, destination, or context.
- **High risk:** require individual approval or strong authentication. Examples include sending messages outside approved rules, financial transactions, destructive operations, administrator access, and sensitive data sharing.

The exact risk classification must be explicit and testable. Do not let the model decide its own authority.

### Required action flow

Every action that changes something outside a conversation must follow this pipeline:

1. **Understand:** identify the user's request and relevant context.
2. **Plan:** produce a structured proposed action, not executable arbitrary code.
3. **Validate:** validate action type, target, parameters, and constraints.
4. **Authorize:** ask the independent permission engine whether the action is allowed.
5. **Approve:** obtain user approval or strong authentication if policy requires it.
6. **Execute:** call a narrow, allowlisted integration/tool adapter.
7. **Verify:** check the external system's result where possible.
8. **Audit:** record a privacy-safe event with outcome and relevant metadata.
9. **Report:** tell the user what actually happened, including failures or partial completion.

If permission is missing, expired, revoked, or ambiguous, do not execute. Ask for the minimum necessary authorization.

### Security and identity
- Support Google sign-in and email/password, linked to avoid duplicate accounts.
- Use device authentication for routine access and biometric/device authentication for sensitive actions where supported.
- Re-authenticate for high-risk operations or suspicious sessions.
- Voice recognition alone is never proof of identity.
- Support session/device revocation.
- Trusted contacts are for emergency-only workflows; they do not gain access to private email, files, conversations, or memories.
- Emergency sharing must be limited to predefined information and explicitly authorized rules.

---

## 5. Architecture

### 5.1 Overall architecture

Use a **hybrid, local-first, modular monolith** initially.

- **Android client:** native Kotlin app, local database, local voice capability, device integrations, permission prompts, offline queue.
- **Windows client:** native desktop app and controlled system-operation layer.
- **Web client:** assistant and control center.
- **Backend:** Python/FastAPI modular monolith for account services, orchestration, integrations, sync, and background jobs.
- **Local storage:** Room/SQLite on Android; suitable secure local storage on Windows.
- **Cloud database:** PostgreSQL for account/device metadata, task metadata, integration metadata, and service state.
- **Memory sync:** encrypted payloads; cloud should hold ciphertext where end-to-end encryption is promised.
- **AI providers:** pluggable local and BYOK/cloud model adapters behind a model router.
- **Workers:** background jobs for scheduled tasks and remote workflows.
- **Voice:** local speech components where feasible; telephony is a separate internet-connected service.

Do not start with microservices, Kafka, Kubernetes, or other infrastructure without evidence that the product needs them. Keep module boundaries clean so extraction can happen later if justified.

### 5.2 Technology choices

| Area | Preferred technology |
|---|---|
| Android | Kotlin, Jetpack Compose |
| Android local DB | Room + SQLite |
| Android background work | WorkManager; Foreground Service only where required |
| Android secrets | Android Keystore |
| Backend | Python + FastAPI |
| Backend database | PostgreSQL |
| Vector retrieval | pgvector or local vector index, subject to encryption design |
| Windows | C# + .NET |
| Web | React + TypeScript |
| AI orchestration | Custom Python modules |
| Local inference | Ollama or compatible local inference endpoint where practical |
| Voice/STT | Evaluate sherpa-onnx, Vosk, and platform capabilities |
| TTS | Evaluate Piper and Android platform TTS |
| Authentication | OAuth 2.0 plus email/password |
| Deployment | Provider-portable, free/self-hosted first |

These are preferred starting choices, not permission to add every dependency immediately. Confirm compatibility, license, maintenance, device performance, and cost before adoption.

### 5.3 AI orchestration components
The backend should contain distinct components for:
- Intent classification and request understanding.
- Context and memory retrieval.
- Model routing based on task, privacy, connectivity, and cost.
- Task planning.
- Structured action proposals.
- Tool/integration registry.
- Permission and approval evaluation.
- Controlled execution.
- Result verification.
- Error handling, retries, and idempotency.
- Audit events.

The model must not bypass the permission engine or directly access unrestricted operating-system commands.

### 5.4 Memory and encryption caveat
The intended cross-device memory sync is end-to-end encrypted:
- Encrypt/decrypt on authorized user devices.
- Cloud stores encrypted memory payloads.
- Devices are explicitly authorized and can be revoked.
- User-controlled recovery key is generated during setup and stored by the user.
- The backend must not be able to decrypt personal memories.

**Important engineering constraint:** server-side semantic search over plaintext memory conflicts with end-to-end encryption. Do not quietly weaken encryption to make pgvector work. Prefer client-side retrieval/indexing or propose a clearly explained privacy-preserving alternative. If the recovery key and all authorized devices are lost, data may be unrecoverable; explain this during setup.

### 5.5 Sync model
Synchronize by priority:
- **Critical:** permission changes, security events, device revocation.
- **High:** active tasks, reminders, urgent notifications, calendar changes.
- **Normal:** memories, preferences, conversation summaries.
- **Low:** historical logs and nonessential background data.

Offline changes enter a local queue and sync when connectivity returns. Use versioning/conflict resolution; never silently overwrite conflicting user data. Device revocation and permission changes must propagate with priority.

### 5.6 Offline and cloud behavior
Offline core:
- Local memories/preferences.
- Local tasks, reminders, and notes.
- Basic intent/command handling.
- Supported local device actions.
- Available local speech recognition and text-to-speech.
- Queued changes for later sync.

Cloud-dependent:
- Complex AI reasoning when using cloud models.
- Web research.
- Remote email follow-ups.
- Cross-device sync when devices are offline from one another.
- Carrier-forwarded call handling and telephony.

The app must explain when a feature is unavailable offline and must not silently send private context to a cloud service.

---

## 6. Repository / folder structure

Use a monorepo so clients, backend, contracts, tests, and documentation evolve together. Create folders as their stage is reached; do not create empty scaffolding for every future feature.

```text
J.U.N.E/
├── apps/
│   ├── android/                  # Kotlin + Jetpack Compose Android client
│   │   ├── app/
│   │   └── ...
│   ├── windows/                  # C#/.NET desktop client
│   │   └── ...
│   └── web/                      # React + TypeScript control center
│       └── ...
│
├── backend/
│   ├── api/                      # FastAPI routes, request/response handling
│   ├── core/                     # Configuration, shared types, lifecycle
│   ├── intelligence/             # Intent, context, model router, planning
│   ├── memory/                   # Memory models, retrieval, retention, sync interface
│   ├── permissions/              # Policy evaluation, approvals, scopes
│   ├── actions/                  # Structured action schemas and validation
│   ├── execution/                # Controlled execution and result verification
│   ├── integrations/             # Google, Microsoft, files, contacts, etc.
│   ├── voice/                    # Speech interfaces and telephony adapters
│   ├── sync/                     # Device registration and encrypted sync
│   ├── security/                 # Auth, key metadata, sessions, audit
│   ├── workers/                  # Scheduled/background jobs
│   └── ...
│
├── shared/
│   ├── contracts/                # API schemas and shared data contracts
│   └── protocols/                # Client/backend protocol definitions
│
├── infrastructure/
│   ├── docker/                   # Local development containers, if needed
│   ├── database/                 # Migrations and database setup
│   └── deployment/               # Reproducible deployment configuration
│
├── tests/
│   ├── unit/
│   ├── integration/
│   ├── contract/
│   ├── security/
│   └── end_to_end/
│
├── docs/
│   ├── architecture/
│   ├── decisions/                # Architecture Decision Records (ADRs)
│   ├── api/
│   ├── product/
│   └── operations/
│
├── scripts/                      # Reproducible developer/maintenance scripts
├── .env.example                  # Names and safe examples only; never real secrets
├── .gitignore
├── README.md
└── JUNE_AI_DEVELOPMENT_GUIDE.md  # This document (or the agreed final filename)
```

### Folder rules
- Keep modules cohesive; avoid dumping unrelated code into `core`, `utils`, or `helpers`.
- Do not create a file or folder until there is a real implementation need.
- Add `__init__.py` where Python package structure requires it.
- Keep API routes thin; business rules belong in domain modules/services.
- Keep provider-specific logic inside integration/model adapters.
- Keep permission checks outside the AI model and outside individual UI screens.
- Shared contracts must be versioned and tested for compatibility.
- Tests should mirror meaningful module boundaries.
- Never commit secrets, API keys, private databases, logs containing personal data, or generated build artifacts.

---

## 7. Development stages and delivery gates

**Work in order.** Each stage has a defined outcome and must pass its exit checks before the next stage becomes the main focus. Small preparatory work for a later stage is acceptable only when it does not destabilize the current stage.

### Stage 0 — Repository audit and foundation
**Goal:** understand the existing prototype and establish a clean, testable foundation.

Tasks:
- Inspect the actual local checkout and compare it with GitHub `main`; do not assume they match.
- Preserve useful prototype ideas, but do not preserve weak architecture just because it exists.
- Establish the monorepo layout gradually.
- Add dependency management, formatting/linting, test setup, environment configuration, and CI basics.
- Define core domain contracts and architecture decisions.
- Update README to reflect Android-first development.

Exit checks:
- Reproducible setup instructions work on a clean environment.
- No secrets are tracked.
- Tests and static checks run.
- Architecture and scope are documented.
- Existing prototype behavior is understood and either retained deliberately or replaced deliberately.

### Stage 1 — Android foundation and safe assistant core
**Goal:** deliver a working Android-first assistant with a dependable backend foundation.

Tasks:
- Kotlin + Jetpack Compose Android shell.
- FastAPI backend skeleton with health/config endpoints.
- Authentication foundation and device registration design.
- Room/SQLite local persistence.
- Conversation UI and text input.
- Basic deterministic command/intent handling.
- Structured action schema.
- Independent permission engine and approval UI.
- Audit event model.
- Offline-first behavior for basic local functions.

Exit checks:
- App starts and handles basic conversations.
- Local data persists across restart.
- Unauthorized actions are blocked.
- Approval and denial paths are tested.
- Offline core functions work.
- No action is reported as successful without verification.

### Stage 2 — Memory, voice, and personal assistant basics
**Goal:** make J.U.N.E. useful as a personal assistant.

Tasks:
- Layered memory model and retrieval.
- Explicit versus inferred memory distinction, confidence, provenance, timestamps.
- Memory review/correction/deletion controls.
- Local speech-to-text, text-to-speech, and optional on-device wake word “Hey J.U.N.E.”
- Tap-to-talk, text input, and interruption.
- Tasks, notes, reminders, and notifications.
- WorkManager for scheduled Android work.
- Focus/Office/Home/Sleep context modes.
- Local fallback when offline.

Exit checks:
- Memory is inspectable and does not grant permissions.
- Sensitive memory is not stored without consent.
- Voice can be disabled and manual interaction remains available.
- Reminders survive restart and behave correctly offline.
- Wake-word behavior is opt-in and battery-conscious.

### Stage 3 — Calendar and communication integrations
**Goal:** connect useful personal services with narrow permissions.

Tasks:
- Google and Outlook calendar integrations.
- Gmail and Outlook mail read/draft workflows.
- Contacts integration as a separate permission.
- Pending-reply detection and follow-up drafting.
- Configurable auto-follow-up rules for explicitly approved categories/templates.
- Calendar conflict detection and scheduling suggestions.
- Integration token security and revocation.

Exit checks:
- Each integration has independently scoped permissions.
- Read-only access cannot accidentally mutate data.
- Sending/accepting/declining requires the configured approval.
- OAuth failures, expired tokens, rate limits, and retries are handled.
- Actions are verified and audited.

### Stage 4 — Controlled automation and file management
**Goal:** execute routine workflows safely within user-defined boundaries.

Tasks:
- Task planner and execution state machine.
- Allowlisted tool registry.
- File search, creation, editing, moving, renaming, and approved deletion.
- User-configured automation rules.
- Idempotency, retries, timeouts, cancellation, and recovery.
- Undo/versioning where practical.
- Web control-center foundations for permissions, automations, and audit history.

Exit checks:
- AI cannot invoke arbitrary code or bypass authorization.
- Destructive and bulk operations are protected.
- Partial failures are visible and recoverable.
- Every automation can be stopped/revoked.
- Audit history shows what happened without leaking unnecessary private content.

### Stage 5 — Phone-call assistant
**Goal:** provide an AI receptionist through supported carrier forwarding and telephony.

Tasks:
- Confirm carrier/plan support and forwarding behavior before implementation.
- Integrate a telephony provider or compatible self-hosted service, with costs made explicit.
- Configure busy/unanswered/unreachable forwarding where supported.
- AI disclosure, caller intent handling, approved answers, message-taking, callback requests, and summaries.
- Trusted-contact and urgent-call rules.
- Recording/transcription consent and retention controls.

Exit checks:
- J.U.N.E. always identifies itself as AI.
- It cannot impersonate the user.
- It does not disclose unapproved personal information.
- Urgent escalation follows explicit rules.
- Call failures and forwarding status are observable.
- Cost and service limitations are documented.

### Stage 6 — Encrypted sync, security, and reliability
**Goal:** safely synchronize the user's experience across authorized devices.

Tasks:
- End-to-end encrypted memory/data sync.
- User-controlled recovery key and recovery education.
- Device enrollment and revocation.
- Priority-based sync queue and conflict resolution.
- Session management, biometric re-authentication, and security events.
- Configurable retention, export, backup, and deletion.
- Reliability monitoring and privacy-safe diagnostics.

Exit checks:
- Backend cannot decrypt E2E-protected personal memory.
- Device revocation is enforced.
- Offline changes reconcile without silent data loss.
- Recovery and data-loss implications are clearly explained.
- Export and deletion workflows are tested.

### Stage 7 — Windows and full web control center
**Goal:** expand J.U.N.E. to desktop and browser workflows using the same central contracts and permission model.

Tasks:
- C#/.NET Windows client.
- Controlled application, file, window, and system operations.
- Safe PowerShell/command execution through validated allowlisted operations.
- React + TypeScript web app.
- Assistant chat, memory, permissions, integrations, automations, devices, security, audit, and system health.
- Shared API contracts and compatibility tests.

Exit checks:
- Windows operations obey the same permission engine.
- No unrestricted AI-generated shell execution.
- Web controls accurately reflect backend state.
- Cross-device revocation and sync work.
- Keyboard/accessibility and error states are tested.

### Stage 8 — Intelligence, optimization, and expansion
**Goal:** improve quality after the core product is reliable.

Tasks:
- Better intent understanding and context resolution.
- Model router and BYOK provider adapters.
- Local/cloud model selection by privacy, task complexity, connectivity, and cost.
- Research agent with source citations and clear limits.
- Adaptive notification prioritization.
- More languages and voice improvements.
- Additional integrations only when there is a clear user benefit.
- Performance, battery, cost, and reliability optimization.

Exit checks:
- Model selection is observable and user-configurable where appropriate.
- Provider outages and quota limits have graceful fallbacks.
- Added capabilities do not weaken privacy or permission boundaries.
- Improvements are supported by tests and measurable evaluation.

---

## 8. Existing prototype: known facts and migration guidance

The earlier repository review found the following on GitHub `main`:
- `backend/main.py`
- `backend/core/intent.py`
- `backend/core/command.py`
- `README.md`

The prototype is a small console loop with basic deterministic intent detection, command parsing, confirmation prompts, pending-action clarification, and simple reference resolution such as “close it” using the last command.

Limitations identified in the source review:
- No Android, Windows, or web application.
- No persistent memory system.
- No AI model integration.
- No real action execution layer.
- No permission engine independent from the conversation loop.
- No test suite or complete dependency-management setup was found in the inspected structure.
- The `dev` branch had older broken/case-sensitive imports; `main` was the better reference at the time of review.

The review was structural/source-level only; code was not executed as part of that audit. Do not claim runtime verification.

Migration guidance:
- Preserve useful concepts such as explicit confirmation and short-term context resolution.
- Redesign intent detection; word matching alone is not sufficient for production.
- Replace the minimal command dataclass with a validated structured action contract containing action type, target, parameters, risk metadata, required permissions, and lifecycle status.
- Move authorization into an independent permission engine.
- Replace the console loop with API/client architecture.
- Do not treat the prototype as a production foundation without tests and redesign.
- The local checkout at `C:\Users\KIIT\Desktop\J.U.N.E` was not confirmed to match GitHub `main`; verify before editing or merging.

---

## 9. AI coding-agent operating procedure

Every AI coding agent must follow this procedure.

### Before coding
1. Read this entire guide and the relevant README/architecture docs.
2. Inspect the repository tree, current branch, working tree, and relevant files.
3. Check existing patterns and dependencies before introducing new ones.
4. Identify the exact user-requested outcome and current development stage.
5. If a decision or required input is genuinely missing, ask one concise question. Do not ask questions whose answers are already documented here.
6. For significant changes, provide a short implementation plan and identify risks.

### While coding
1. Make the smallest complete change that solves the requirement.
2. Keep changes focused; do not rewrite unrelated files.
3. Follow established project conventions.
4. Validate all external input and all action parameters.
5. Keep business logic out of UI components and thin API routes.
6. Use typed, structured contracts at module boundaries.
7. Keep permissions independent of model output.
8. Use narrow integration adapters; never expose unrestricted system access.
9. Add tests for behavior, failure cases, and security boundaries.
10. Update documentation and migrations when behavior or data contracts change.
11. Do not add dependencies without explaining why, maintenance impact, license, and cost.
12. Do not commit, push, deploy, delete user data, or modify production systems unless explicitly asked.

### After coding
1. Run relevant tests, linters, type checks, and build checks.
2. Inspect the final diff for unrelated changes, secrets, generated files, and accidental data exposure.
3. Report exactly what was changed.
4. Report commands/tests actually run and their real results.
5. Clearly list anything not tested or not verified.
6. Identify risks, limitations, migrations, and required manual steps.
7. Never claim completion if a required check failed.

### When starting a new version or major feature
First inspect the current implementation and repository state. Ask the user whether anything is needed from their side only when a real dependency exists (for example, credentials, device access, provider choice, or a product decision). If nothing is needed, proceed without unnecessary questions.

---

## 10. Code quality: no AI slop

**Write concise, production-quality code. Do not confuse more code with better engineering.**

- If a clear, correct, maintainable solution takes one line, do not write 100 lines for it.
- Prefer the simplest solution that satisfies the real requirements.
- Avoid overengineering, speculative abstractions, unnecessary classes, excessive comments, and needless design patterns.
- Do not compress code so aggressively that it becomes cryptic. Concise must still be readable.
- Do not duplicate logic; extract shared logic only when there is real reuse or a clear boundary.
- Do not add wrappers that merely forward arguments without adding value.
- Do not add dependencies for trivial functionality.
- No fake implementations, placeholder success responses, TODO-only features presented as complete, hardcoded demo data in production paths, or mock behavior hidden behind real interfaces.
- No broad `except` blocks that swallow errors.
- No silent fallback that changes security or data semantics.
- No arbitrary shell execution or unsafe dynamic evaluation.
- No hardcoded secrets, tokens, passwords, or personal data.
- No logging of raw private conversations, emails, call content, location history, or credentials by default.
- No unnecessary comments that restate obvious code. Explain why when a non-obvious decision needs explanation.
- Use clear names and small functions with one responsibility.
- Handle errors at the layer that can respond meaningfully; preserve useful error context.
- Do not claim an API, library, Android capability, or provider behavior exists without checking its official documentation or the actual installed version.

### Production engineering requirements
- Input validation and safe serialization.
- Explicit types and stable contracts.
- Correct authentication and authorization.
- Secure secret storage and redaction.
- Timeouts, bounded retries, backoff, and idempotency for external calls.
- Resource cleanup and cancellation.
- Database migrations and transaction boundaries.
- Concurrency safety where applicable.
- Structured, privacy-safe logging and useful observability.
- Unit, integration, contract, and security tests as appropriate.
- Reproducible setup and deployment.
- Clear documentation for configuration and operational behavior.

---

## 11. Language- and platform-specific standards

### Python / FastAPI
- Use type hints for public functions and data contracts.
- Use Pydantic models for API validation.
- Keep route handlers thin.
- Use async only where it is appropriate and supported end-to-end.
- Use dependency injection for shared resources such as database sessions.
- Keep configuration in environment/settings objects; validate required configuration at startup.
- Use structured exceptions and consistent API error responses.
- Use database transactions deliberately.
- Pin/manage dependencies reproducibly.
- Add tests for success, validation, authorization, and failure paths.

### Kotlin / Android
- Use Kotlin and Jetpack Compose.
- Separate UI, state, domain logic, and data access.
- Use lifecycle-aware state collection.
- Use Room for local persistence and migrations.
- Use Android Keystore for cryptographic key protection.
- Use WorkManager for deferrable scheduled work.
- Use Foreground Services only when Android policy requires a visible ongoing operation.
- Request runtime permissions only when needed and explain why.
- Respect battery, background execution, and platform privacy restrictions.
- Accessibility Service must be optional, disclosed, narrowly scoped, and user-enabled.
- No root assumptions.

### C# / Windows
- Use supported .NET APIs and least-privilege execution.
- Separate UI from system-operation services.
- Validate all paths, arguments, and targets.
- Use allowlisted operations rather than unrestricted model-generated commands.
- Require approval for administrator access, installations, destructive changes, and sensitive operations.
- Log outcomes without exposing private content.

### TypeScript / React
- Use strict TypeScript.
- Keep components focused and accessible.
- Separate API/data logic from presentation.
- Validate server responses at trust boundaries.
- Represent loading, empty, error, and success states.
- Never rely on the frontend alone for authorization; the backend enforces permissions.

---

## 12. Testing strategy

Tests are part of implementation, not optional cleanup.

At minimum, test:
- Intent classification, ambiguity, negation, and context resolution.
- Structured action validation.
- Permission allow/deny/approval/revocation/expiry.
- User authentication and device revocation.
- Memory provenance, confidence, retention, deletion, and sensitive-data rules.
- Offline queues, retries, duplicate delivery, and conflict resolution.
- Integration token expiry, rate limits, provider errors, and partial failure.
- File path validation and destructive-operation safeguards.
- Email send/draft boundaries.
- Call disclosure and approved-information boundaries.
- Cross-platform contract compatibility.
- Database migrations and restart persistence.

Never test destructive operations against real user data or production services without explicit authorization and a safe test plan. Use mocks/fakes for unit tests and controlled test accounts for integration tests.

---

## 13. Privacy, data retention, and user control

- Collect only data required for a feature.
- Request each integration permission separately.
- Keep cloud context minimal and task-specific.
- Make cloud-provider use visible when relevant.
- Encrypt data in transit and at rest; use E2E encryption for the data categories promised to be E2E encrypted.
- Provide configurable retention for conversations, extracted memories, call recordings/transcripts, call summaries, and audit logs.
- Allow manual deletion and disable-storage options where feasible.
- Provide full data export in usable formats.
- Provide encrypted backup and account deletion workflows.
- Explain any legally required retention.
- Do not claim deletion from third-party providers unless it has actually been performed and verified.
- Keep security/audit logs useful but privacy-safe.
- Trusted contacts receive only the exact emergency information the user pre-authorized.

---

## 14. Documentation and change management

For significant architectural or data-model decisions, add an Architecture Decision Record under `docs/decisions/` with:
- Context/problem.
- Options considered.
- Decision.
- Consequences and trade-offs.
- Security, privacy, cost, and migration implications.

Keep these documents current:
- Root README: what J.U.N.E. is, setup, supported features, limitations, and current stage.
- Architecture docs: component responsibilities and data flows.
- API docs/contracts: request/response schemas and versioning.
- Operations docs: configuration, deployment, backups, recovery, and troubleshooting.
- Product docs: stage scope and acceptance criteria.

Do not let documentation claim a planned feature is implemented. Label planned, in progress, implemented, and verified accurately.

---

## 15. Definition of done

A feature is done only when all applicable points are true:

- It solves the requested user problem.
- It belongs to the correct development stage.
- Its design follows the product principles.
- Permissions and privacy boundaries are enforced by trusted code, not just prompts.
- Inputs and outputs are validated.
- Errors and partial failures are handled honestly.
- Tests cover expected behavior and important failure/security cases.
- Relevant tests/build/lint/type checks have actually been run.
- Data migrations and backward compatibility are addressed.
- Documentation and configuration examples are updated.
- No secrets or personal data are accidentally committed/logged.
- The diff contains no unrelated changes.
- Remaining limitations are stated clearly.

### Required AI completion report

At the end of each coding task, provide:

1. **Implemented:** what now works.
2. **Files changed:** concise list with purpose.
3. **Verification:** exact commands/tests run and results.
4. **Not verified:** anything not run or not possible to verify.
5. **Risks/limitations:** security, compatibility, cost, platform, or operational concerns.
6. **Next step:** only the most useful next action.

---

## 16. Final instruction to every AI agent

Build J.U.N.E. as serious, maintainable software—not as a quick AI demo.

Understand the existing code before changing it. Respect the agreed product decisions. Follow the stages. Keep the implementation concise but the engineering rigorous. Do not overbuild. Do not invent capabilities. Do not weaken privacy or security to make a feature easier. Never allow the AI model to authorize itself. Never impersonate the user. Never claim an action or test succeeded unless it did.

**The goal is not an AI that takes over the user's life. The goal is an assistant that understands the user, reduces their workload, and remains accountable to them.**
