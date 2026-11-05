from shared.contracts.actions import ActionProposal, RiskLevel
from shared.contracts.permissions import PermissionCheckResult, PermissionDecision


class PermissionEngine:
    """Independent policy engine for authorizing structured action proposals."""

    def __init__(
        self,
        granted_permissions: set[str] | None = None,
        preapproved_actions: set[str] | None = None,
    ):
        self.granted_permissions = granted_permissions or set()
        self.preapproved_actions = preapproved_actions or set()

    def evaluate(self, action: ActionProposal) -> PermissionCheckResult:
        # 1. Check for missing required permissions
        missing = [p for p in action.required_permissions if p not in self.granted_permissions]
        if missing:
            return PermissionCheckResult(
                decision=PermissionDecision.DENY,
                missing_permissions=missing,
                reason=f"Missing required permissions: {', '.join(missing)}",
            )

        # 2. High-risk actions always require explicit user confirmation
        if action.risk_level == RiskLevel.HIGH:
            return PermissionCheckResult(
                decision=PermissionDecision.REQUIRES_CONFIRMATION,
                reason="High-risk action requires explicit user confirmation",
            )

        # 3. Medium-risk actions require confirmation unless pre-approved
        if (
            action.risk_level == RiskLevel.MEDIUM
            and action.action_type not in self.preapproved_actions
        ):
            return PermissionCheckResult(
                decision=PermissionDecision.REQUIRES_CONFIRMATION,
                reason="Medium-risk action not in pre-approved list",
            )

        # 4. Low-risk (or pre-approved medium-risk) with all permissions granted
        return PermissionCheckResult(
            decision=PermissionDecision.ALLOW,
            reason="Action authorized by policy",
        )
