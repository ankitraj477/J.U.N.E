# ADR-0003: Stage 2 Layered Memory, Local Voice & Context Modes

- **Status:** Accepted
- **Date:** 2026-11-05
- **Stage:** Stage 2

## Context
Stage 2 adds personal assistant capabilities: layered memory (working, episodic, semantic, preference, procedural), local voice input/output, scheduled background reminders, and context modes.

## Decision
1. **Layered Memory Model & Non-Authorization:** Memory items are classified by type (`working`, `episodic`, `semantic`, `preference`, `procedural`) and provenance (`user_stated` vs `inferred`). Per core philosophy, memory never grants action authorization. Inferred memories are capped in confidence.
2. **Explicit Consent for Sensitive Data:** Sensitive memories require explicit user consent flags before retention.
3. **Android Platform TTS & Speech Recognition:** Leverages Android's built-in `TextToSpeech` and `SpeechRecognizer` for zero-cost, offline-capable voice interactions. Voice responses can be toggled on or off at any time.
4. **Android WorkManager Reminders:** Schedules reminders using `WorkManager` so that notification alarms persist and fire reliably even after the app is backgrounded or the device is restarted.
5. **Context Modes:** Supports explicit context modes (`Focus`, `Office`, `Home`, `Sleep`) to adapt notification priority and assistant tone.

## Security, Privacy, and Cost Implications
- Cost: ₹0 maintained using Android platform TTS/STT and local SQLite/Room storage.
- Privacy: Full review, deletion, and consent gates for memory data.
- Safety: Reminders and speech operations do not bypass permission boundaries.

