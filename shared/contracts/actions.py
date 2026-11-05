import uuid
from enum import Enum
from typing import Any

from pydantic import BaseModel, Field


class RiskLevel(str, Enum):
    LOW = "low"
    MEDIUM = "medium"
    HIGH = "high"


class ActionStatus(str, Enum):
    PROPOSED = "proposed"
    VALIDATED = "validated"
    AUTHORIZED = "authorized"
    APPROVED = "approved"
    EXECUTING = "executing"
    COMPLETED = "completed"
    FAILED = "failed"
    CANCELLED = "cancelled"


class ActionProposal(BaseModel):
    id: str = Field(default_factory=lambda: str(uuid.uuid4()))
    action_type: str
    target: str
    parameters: dict[str, Any] = Field(default_factory=dict)
    risk_level: RiskLevel = RiskLevel.LOW
    required_permissions: list[str] = Field(default_factory=list)
    status: ActionStatus = ActionStatus.PROPOSED
    reason: str = ""

    def summary(self) -> str:
        return f"{self.action_type} on '{self.target}' (risk: {self.risk_level.value})"
