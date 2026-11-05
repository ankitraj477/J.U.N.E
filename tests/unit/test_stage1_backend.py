from fastapi.testclient import TestClient

from backend.api.app import app
from backend.execution.runner import ExecutionRunner
from backend.permissions.engine import PermissionEngine
from shared.contracts.actions import ActionProposal, ActionStatus, RiskLevel
from shared.contracts.permissions import PermissionDecision

client = TestClient(app)


def test_health_endpoint():
    res = client.get("/health")
    assert res.status_code == 200
    data = res.json()
    assert data["status"] == "ok"
    assert data["stage"] == "Stage 1"


def test_config_endpoint():
    res = client.get("/config")
    assert res.status_code == 200
    data = res.json()
    assert data["offline_first"] is True
    assert "J.U.N.E." in data["identity_disclosure"]


def test_permission_engine_allow_low_risk():
    engine = PermissionEngine(granted_permissions={"notes.write"})
    action = ActionProposal(
        action_type="notes.create",
        target="Buy groceries",
        risk_level=RiskLevel.LOW,
        required_permissions=["notes.write"],
    )
    result = engine.evaluate(action)
    assert result.decision == PermissionDecision.ALLOW


def test_permission_engine_deny_missing_permission():
    engine = PermissionEngine(granted_permissions=set())
    action = ActionProposal(
        action_type="notes.create",
        target="Buy groceries",
        risk_level=RiskLevel.LOW,
        required_permissions=["notes.write"],
    )
    result = engine.evaluate(action)
    assert result.decision == PermissionDecision.DENY
    assert "notes.write" in result.missing_permissions


def test_permission_engine_high_risk_requires_confirmation():
    engine = PermissionEngine(granted_permissions={"storage.delete"})
    action = ActionProposal(
        action_type="file.delete",
        target="important.pdf",
        risk_level=RiskLevel.HIGH,
        required_permissions=["storage.delete"],
    )
    result = engine.evaluate(action)
    assert result.decision == PermissionDecision.REQUIRES_CONFIRMATION


def test_execution_runner_blocks_unauthorized():
    runner = ExecutionRunner()
    action = ActionProposal(
        action_type="notes.create",
        target="secret note",
        status=ActionStatus.PROPOSED,
    )
    success, msg = runner.execute(action)
    assert success is False
    assert action.status == ActionStatus.FAILED
    assert "missing authorization" in msg


def test_api_chat_flow():
    res = client.post("/api/v1/intent", json={"text": "Hello J.U.N.E."})
    assert res.status_code == 200
    data = res.json()
    assert data["intent"] == "chat"
    assert data["action"] is None

    # Test word-boundary trigger matching: words starting with trigger prefix should not trigger commands
    res_notes = client.post("/api/v1/intent", json={"text": "Notes are useful"})
    assert res_notes.json()["intent"] == "chat"
    assert res_notes.json()["action"] is None

    res_open = client.post("/api/v1/intent", json={"text": "Opening hours?"})
    assert res_open.json()["intent"] == "chat"
    assert res_open.json()["action"] is None


def test_api_command_execution_pipeline():
    # 1. Plan
    res = client.post("/api/v1/intent", json={"text": "create note Meeting at 10am"})
    assert res.status_code == 200
    data = res.json()
    assert data["intent"] == "command"
    action = data["action"]
    assert action["action_type"] == "notes.create"
    assert action["target"] == "meeting at 10am"

    # 2. Authorize
    res_auth = client.post("/api/v1/actions/authorize", json=action)
    assert res_auth.status_code == 200
    assert res_auth.json()["decision"] == "allow"

    # 3. Execute
    res_exec = client.post("/api/v1/actions/execute", json={"action": action})
    assert res_exec.status_code == 200
    exec_data = res_exec.json()
    assert exec_data["success"] is True
    assert exec_data["action"]["status"] == "completed"

    # 4. Audit
    res_audit = client.get("/api/v1/audit")
    assert res_audit.status_code == 200
    audit_events = res_audit.json()
    event_types = [e["event_type"] for e in audit_events]
    assert "action_proposed" in event_types
    assert "execution_completed" in event_types


def test_api_execute_unauthorized_action_denied_even_if_user_approved():
    # Attempting to execute an ungranted permission (storage.delete) with user_approved: True
    action = {
        "id": "unauth-1",
        "action_type": "file.delete",
        "target": "secret.txt",
        "risk_level": "high",
        "required_permissions": ["storage.delete"],
        "status": "proposed",
    }
    res_exec = client.post(
        "/api/v1/actions/execute", json={"action": action, "user_approved": True}
    )
    assert res_exec.status_code == 200
    exec_data = res_exec.json()
    assert exec_data["success"] is False
    assert exec_data["decision"] == "deny"


def test_api_execute_client_status_authorized_does_not_bypass_policy():
    # Client sends status: "authorized" claiming storage.delete
    action = {
        "id": "spoof-1",
        "action_type": "file.delete",
        "target": "secret.txt",
        "risk_level": "high",
        "required_permissions": ["storage.delete"],
        "status": "authorized",
    }
    res_exec = client.post(
        "/api/v1/actions/execute", json={"action": action, "user_approved": False}
    )
    assert res_exec.status_code == 200
    exec_data = res_exec.json()
    assert exec_data["success"] is False
    assert exec_data["decision"] == "deny"
