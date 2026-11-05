import pytest
from pydantic import ValidationError

from shared.contracts.actions import ActionProposal, ActionStatus, RiskLevel
from shared.contracts.permissions import (
    PermissionCheckResult,
    PermissionDecision,
)


def test_action_proposal_defaults():
    proposal = ActionProposal(action_type="file.open", target="notes.txt")
    assert proposal.status == ActionStatus.PROPOSED
    assert proposal.risk_level == RiskLevel.LOW
    assert proposal.required_permissions == []
    assert "file.open" in proposal.summary()


def test_action_proposal_validation_missing_target():
    with pytest.raises(ValidationError):
        ActionProposal(action_type="app.launch")  # target is required


def test_permission_decision_enum_values():
    assert PermissionDecision.ALLOW.value == "allow"
    assert PermissionDecision.DENY.value == "deny"
    assert PermissionDecision.REQUIRES_CONFIRMATION.value == "requires_confirmation"


def test_permission_check_result():
    result = PermissionCheckResult(
        decision=PermissionDecision.DENY,
        missing_permissions=["storage.read"],
        reason="Storage read permission not granted",
    )
    assert result.decision == PermissionDecision.DENY
    assert "storage.read" in result.missing_permissions
