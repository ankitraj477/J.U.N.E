from fastapi import FastAPI, HTTPException
from pydantic import BaseModel

from backend.execution.runner import ExecutionRunner
from backend.intelligence.planner import ActionPlanner
from backend.memory.store import MemoryStore
from backend.permissions.engine import PermissionEngine
from shared.contracts.actions import ActionProposal, ActionStatus
from shared.contracts.audit import AuditEvent, AuditEventType
from shared.contracts.memory import ContextMode, MemoryItem, MemoryType
from shared.contracts.permissions import PermissionCheckResult, PermissionDecision

app = FastAPI(title="J.U.N.E. Safe Assistant Core", version="0.1.0")

# Default granted permissions and core engines for Stage 1 & 2
DEFAULT_PERMISSIONS = {"notes.write", "reminders.write", "system.launch"}
active_proposals: dict[str, ActionProposal] = {}
permission_engine = PermissionEngine(granted_permissions=DEFAULT_PERMISSIONS)
action_planner = ActionPlanner()
execution_runner = ExecutionRunner()
memory_store = MemoryStore()
current_context_mode = ContextMode.HOME


class MessageRequest(BaseModel):
    text: str


class MessageResponse(BaseModel):
    intent: str
    reply: str
    action: ActionProposal | None = None


class ExecuteRequest(BaseModel):
    action: ActionProposal
    user_approved: bool = False


class MemoryCreateRequest(BaseModel):
    memory: MemoryItem
    consent_sensitive: bool = False


class ContextModeRequest(BaseModel):
    mode: ContextMode


@app.get("/health")
def health():
    return {"status": "ok", "stage": "Stage 1", "version": "0.1.0"}


@app.get("/config")
def config():
    return {
        "env": "development",
        "offline_first": True,
        "identity_disclosure": "J.U.N.E. AI Assistant",
        "granted_permissions": sorted(permission_engine.granted_permissions),
    }


@app.post("/api/v1/intent", response_model=MessageResponse)
def process_intent(req: MessageRequest):
    proposal = action_planner.plan(req.text)
    if proposal:
        active_proposals[proposal.id] = proposal
        execution_runner.log_audit(
            AuditEventType.ACTION_PROPOSED,
            proposal.id,
            {"action_type": proposal.action_type, "target": proposal.target},
        )
        return MessageResponse(
            intent="command",
            reply=f"Proposed action: {proposal.summary()}",
            action=proposal,
        )

    return MessageResponse(
        intent="chat",
        reply=f"I hear you: '{req.text}'. How can I assist?",
        action=None,
    )


@app.post("/api/v1/actions/authorize", response_model=PermissionCheckResult)
def authorize_action(action: ActionProposal):
    canonical = active_proposals.get(action.id)
    target_action = canonical if canonical is not None else action
    result = permission_engine.evaluate(target_action)
    execution_runner.log_audit(
        AuditEventType.PERMISSION_EVALUATED,
        target_action.id,
        {"decision": result.decision.value, "reason": result.reason},
    )
    if result.decision == PermissionDecision.ALLOW:
        target_action.status = ActionStatus.AUTHORIZED
    return result


@app.post("/api/v1/actions/execute")
def execute_action(req: ExecuteRequest):
    canonical = active_proposals.get(req.action.id)
    action = canonical if canonical is not None else req.action

    # Never trust client-supplied status. Re-evaluate policy on every execute call.
    action.status = ActionStatus.PROPOSED
    auth_result = permission_engine.evaluate(action)

    if auth_result.decision == PermissionDecision.ALLOW:
        action.status = ActionStatus.AUTHORIZED
    elif auth_result.decision == PermissionDecision.REQUIRES_CONFIRMATION and req.user_approved:
        action.status = ActionStatus.APPROVED
        execution_runner.log_audit(
            AuditEventType.USER_APPROVED,
            action.id,
            {"approved": True},
        )
    else:
        return {
            "success": False,
            "message": auth_result.reason,
            "action": action,
            "decision": auth_result.decision,
        }

    success, message = execution_runner.execute(action)
    if success:
        active_proposals.pop(action.id, None)

    return {
        "success": success,
        "message": message,
        "action": action,
    }


@app.get("/api/v1/audit", response_model=list[AuditEvent])
def get_audit_trail():
    return execution_runner.audit_log


@app.get("/api/v1/memory", response_model=list[MemoryItem])
def list_memories(memory_type: MemoryType | None = None):
    return memory_store.list_all(memory_type)


@app.post("/api/v1/memory", response_model=MemoryItem)
def create_memory(req: MemoryCreateRequest):
    try:
        return memory_store.save(req.memory, user_consented_sensitive=req.consent_sensitive)
    except ValueError as err:
        raise HTTPException(status_code=400, detail=str(err)) from err


@app.delete("/api/v1/memory/{memory_id}")
def delete_memory(memory_id: str):
    if not memory_store.delete(memory_id):
        raise HTTPException(status_code=404, detail="Memory item not found")
    return {"success": True, "deleted_id": memory_id}


@app.get("/api/v1/context-mode")
def get_context_mode():
    return {"current_mode": current_context_mode}


@app.post("/api/v1/context-mode")
def set_context_mode(req: ContextModeRequest):
    global current_context_mode
    current_context_mode = req.mode
    return {"current_mode": current_context_mode}
