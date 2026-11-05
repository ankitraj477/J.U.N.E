import uuid
from datetime import UTC, datetime
from enum import Enum
from typing import Any

from pydantic import BaseModel, Field


class AuditEventType(str, Enum):
    ACTION_PROPOSED = "action_proposed"
    PERMISSION_EVALUATED = "permission_evaluated"
    USER_APPROVED = "user_approved"
    USER_DENIED = "user_denied"
    EXECUTION_STARTED = "execution_started"
    EXECUTION_COMPLETED = "execution_completed"
    EXECUTION_FAILED = "execution_failed"


class AuditEvent(BaseModel):
    id: str = Field(default_factory=lambda: str(uuid.uuid4()))
    timestamp: datetime = Field(default_factory=lambda: datetime.now(UTC))
    event_type: AuditEventType
    action_id: str | None = None
    actor: str = "system"
    details: dict[str, Any] = Field(default_factory=dict)
