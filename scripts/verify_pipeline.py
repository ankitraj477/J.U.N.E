import sys
from pathlib import Path

# Ensure project root is in sys.path
sys.path.insert(0, str(Path(__file__).resolve().parent.parent))

from fastapi.testclient import TestClient

from backend.api.app import app

client = TestClient(app)

print("=" * 60)
print(" LIVE VERIFICATION: J.U.N.E. BACKEND PIPELINE")
print("=" * 60)

# 1. Health & Config
print("\n[1] Health & Configuration")
health = client.get("/health").json()
config = client.get("/config").json()
print(" -> Health:", health)
print(" -> Config:", config)

# 2. Chat Processing
print("\n[2] Conversational Chat Turn")
chat = client.post("/api/v1/intent", json={"text": "Hello J.U.N.E."}).json()
print(" -> Intent:", chat["intent"])
print(" -> Reply:", chat["reply"])
assert chat["action"] is None

# 3. Low-Risk Action
print("\n[3] Low-Risk Action (Note Creation)")
plan = client.post("/api/v1/intent", json={"text": "create note Buy groceries"}).json()
action = plan["action"]
print(f" -> Plan: {action['action_type']} on '{action['target']}' (Risk: {action['risk_level']})")

auth = client.post("/api/v1/actions/authorize", json=action).json()
print(f" -> Authorization Decision: {auth['decision']}")
assert auth["decision"] == "allow"

exec_res = client.post("/api/v1/actions/execute", json={"action": action}).json()
print(f" -> Execution: success={exec_res['success']} | result='{exec_res['message']}'")
assert exec_res["success"] is True

# 4. Permission Safeguards
print("\n[4] Permission Safeguards")

# 4a: Missing permission outright denied
print(" [4a] High-risk action without permission granted:")
high_risk = client.post("/api/v1/intent", json={"text": "delete production.db"}).json()
high_action = high_risk["action"]
print(
    f"  -> Plan: {high_action['action_type']} on '{high_action['target']}' (Risk: {high_action['risk_level']})"
)
auth_denied = client.post("/api/v1/actions/authorize", json=high_action).json()
print(f"  -> Authorization: {auth_denied['decision']} (Reason: {auth_denied['reason']})")
assert auth_denied["decision"] == "deny"
assert "storage.delete" in auth_denied["missing_permissions"]

# 4b: High-risk action WITH permission granted -> requires explicit user confirmation
print(" [4b] High-risk action WITH permission granted:")
from backend.api.app import permission_engine

permission_engine.granted_permissions.add("storage.delete")

auth_high = client.post("/api/v1/actions/authorize", json=high_action).json()
print(f"  -> Authorization: {auth_high['decision']} (Reason: {auth_high['reason']})")
assert auth_high["decision"] == "requires_confirmation"

# 4c: Execution without user approval -> blocked
exec_blocked = client.post(
    "/api/v1/actions/execute", json={"action": high_action, "user_approved": False}
).json()
print(
    f"  -> Execution without user approval: success={exec_blocked['success']} | message='{exec_blocked['message']}'"
)
assert exec_blocked["success"] is False

# 5. High-Risk Action Executed WITH Approval
print("\n[5] High-Risk Action Explicit User Approval")
exec_approved = client.post(
    "/api/v1/actions/execute", json={"action": high_action, "user_approved": True}
).json()
print(
    f" -> Execution with user approval: success={exec_approved['success']} | status={exec_approved['action']['status']}"
)
assert exec_approved["success"] is True


# 6. Audit Trail
print("\n[6] Verification of Audit Log Trail")
audit = client.get("/api/v1/audit").json()
print(f" -> Total recorded audit events: {len(audit)}")
for idx, ev in enumerate(audit, 1):
    event_type = ev["event_type"]
    action_id = ev.get("action_id", "none")
    details = ev.get("details", {})
    print(f"    {idx}. [{event_type}] action_id={action_id[:8]}... details={details}")

print("\n" + "=" * 60)
print(" ALL PIPELINE CHECKS VERIFIED SUCCESSFULLY")
print("=" * 60)
