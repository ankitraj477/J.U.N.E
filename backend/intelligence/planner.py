from typing import ClassVar

from shared.contracts.actions import ActionProposal, RiskLevel


class ActionPlanner:
    """Deterministic parser and planner for translating user intent into structured proposals."""

    ACTION_MAP: ClassVar[dict[str, tuple[str, RiskLevel, list[str]]]] = {
        "open": ("system.app_open", RiskLevel.MEDIUM, ["system.launch"]),
        "launch": ("system.app_open", RiskLevel.MEDIUM, ["system.launch"]),
        "create note": ("notes.create", RiskLevel.LOW, ["notes.write"]),
        "note": ("notes.create", RiskLevel.LOW, ["notes.write"]),
        "set reminder": ("reminder.create", RiskLevel.LOW, ["reminders.write"]),
        "remind": ("reminder.create", RiskLevel.LOW, ["reminders.write"]),
        "delete": ("file.delete", RiskLevel.HIGH, ["storage.delete"]),
    }

    def plan(self, text: str) -> ActionProposal | None:
        normalized = text.strip().lower()
        if not normalized:
            return None

        # Check for phrase-based triggers first, then single-word triggers
        for trigger, (action_type, risk, perms) in sorted(
            self.ACTION_MAP.items(), key=lambda x: len(x[0]), reverse=True
        ):
            if normalized == trigger or normalized.startswith(f"{trigger} "):
                target = normalized[len(trigger) :].strip()
                if not target:
                    # Incomplete target, cannot form a valid proposal without clarification
                    return None
                return ActionProposal(
                    action_type=action_type,
                    target=target,
                    risk_level=risk,
                    required_permissions=perms,
                    reason=f"Planned from intent trigger '{trigger}'",
                )
        return None
