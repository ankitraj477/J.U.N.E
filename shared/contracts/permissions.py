from enum import Enum

from pydantic import BaseModel, Field


class PermissionDecision(str, Enum):
    ALLOW = "allow"
    DENY = "deny"
    REQUIRES_CONFIRMATION = "requires_confirmation"


class PermissionCheckRequest(BaseModel):
    action_id: str
    action_type: str
    required_permissions: list[str] = Field(default_factory=list)
    granted_permissions: list[str] = Field(default_factory=list)
    risk_level: str = "low"


class PermissionCheckResult(BaseModel):
    decision: PermissionDecision
    missing_permissions: list[str] = Field(default_factory=list)
    reason: str = ""
