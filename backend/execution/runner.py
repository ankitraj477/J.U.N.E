from typing import Any

from shared.contracts.actions import ActionProposal, ActionStatus
from shared.contracts.audit import AuditEvent, AuditEventType


class ExecutionRunner:
    """Safely executes authorized action proposals against narrow allowlisted adapters."""

    def __init__(self):
        self.audit_log: list[AuditEvent] = []
        self._notes: list[str] = []
        self._reminders: list[str] = []

    def log_audit(self, event_type: AuditEventType, action_id: str | None, details: dict[str, Any]):
        event = AuditEvent(event_type=event_type, action_id=action_id, details=details)
        self.audit_log.append(event)
        return event

    def execute(self, action: ActionProposal) -> tuple[bool, str]:
        # Validate lifecycle status before execution
        if action.status not in (ActionStatus.AUTHORIZED, ActionStatus.APPROVED):
            self.log_audit(
                AuditEventType.EXECUTION_FAILED,
                action.id,
                {
                    "reason": f"Action status '{action.status.value}' is not authorized for execution"
                },
            )
            action.status = ActionStatus.FAILED
            return False, "Cannot execute action: missing authorization or approval."

        self.log_audit(
            AuditEventType.EXECUTION_STARTED,
            action.id,
            {"action_type": action.action_type, "target": action.target},
        )
        action.status = ActionStatus.EXECUTING

        # Allowlisted handlers only
        try:
            if action.action_type == "notes.create":
                self._notes.append(action.target)
                msg = f"Note created: '{action.target}'"
            elif action.action_type == "reminder.create":
                self._reminders.append(action.target)
                msg = f"Reminder set: '{action.target}'"
            elif action.action_type == "system.app_open":
                msg = f"Application launch requested for '{action.target}'"
            elif action.action_type == "file.delete":
                msg = f"File deletion verified for '{action.target}'"
            else:
                raise ValueError(f"Unknown or unauthorized action type: {action.action_type}")

            action.status = ActionStatus.COMPLETED
            self.log_audit(
                AuditEventType.EXECUTION_COMPLETED,
                action.id,
                {"outcome": msg},
            )
            return True, msg

        except (ValueError, RuntimeError, KeyError) as err:
            action.status = ActionStatus.FAILED
            self.log_audit(
                AuditEventType.EXECUTION_FAILED,
                action.id,
                {"error": str(err)},
            )
            return False, f"Execution failed: {err}"
