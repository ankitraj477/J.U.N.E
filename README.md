# J.U.N.E.

**J.U.N.E.** is an offline-first, privacy-conscious personal AI assistant for Android, Windows, and the Web.

> **Central Principle:** *“THE AI SHOULD LEARN ITS USER, but don't become the user.”*  
> Understanding the user is not authorization to act as the user. The AI proposes; an independent permission engine authorizes.

---

## Key Characteristics

- **Android-First:** Built native-first on Android (Kotlin + Jetpack Compose) for daily mobile assistance, local storage, and contextual awareness.
- **Privacy & Local-First:** Core capabilities (reminders, tasks, notes, local commands) work offline with Room/SQLite. Personal memories are end-to-end encrypted when synced.
- **Safe Autonomy:** Actions follow a strict 9-step pipeline (Understand ➔ Plan ➔ Validate ➔ Authorize ➔ Approve ➔ Execute ➔ Verify ➔ Audit ➔ Report). The model cannot grant itself permissions or run arbitrary shell commands.
- **₹0 Baseline:** Designed to run with zero mandatory service cost using local models or free tiers, with optional BYOK (Bring-Your-Own-Key) for cloud LLMs.

---

## Project Status

- **Current Stage:** `Stage 2 — Memory, Voice, and Personal Assistant Basics` (Complete)
- **Next Stage:** `Stage 3 — Calendar and Communication Integrations`
- **Development Roadmap:** Detailed in [JUNE_AI_DEVELOPMENT_GUIDE.md](JUNE_AI_DEVELOPMENT_GUIDE.md).



---

## Local Development Setup

### Prerequisites
- Python 3.11+
- Virtual environment (`venv`)

### Setup Instructions

```bash
# 1. Create and activate virtual environment
python -m venv .venv

# Windows PowerShell:
.\.venv\Scripts\Activate.ps1
# Linux/macOS:
source .venv/bin/activate

# 2. Install dependencies in editable mode
pip install -e ".[dev]"

# 3. Copy example environment configuration
cp .env.example .env

# 4. Run tests and linter
pytest
ruff check .
```
