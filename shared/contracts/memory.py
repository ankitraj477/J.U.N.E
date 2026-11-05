import uuid
from datetime import UTC, datetime
from enum import Enum

from pydantic import BaseModel, Field


class MemoryType(str, Enum):
    WORKING = "working"
    EPISODIC = "episodic"
    SEMANTIC = "semantic"
    PREFERENCE = "preference"
    PROCEDURAL = "procedural"


class MemorySource(str, Enum):
    USER_STATED = "user_stated"
    INFERRED = "inferred"


class ContextMode(str, Enum):
    FOCUS = "focus"
    OFFICE = "office"
    HOME = "home"
    SLEEP = "sleep"


class MemoryItem(BaseModel):
    id: str = Field(default_factory=lambda: str(uuid.uuid4()))
    memory_type: MemoryType = MemoryType.SEMANTIC
    source: MemorySource = MemorySource.USER_STATED
    key: str
    content: str
    confidence: float = Field(default=1.0, ge=0.0, le=1.0)
    sensitive: bool = False
    created_at: datetime = Field(default_factory=lambda: datetime.now(UTC))
    provenance: str = "direct_input"

    def summary(self) -> str:
        return f"[{self.memory_type.value}|{self.source.value}] {self.key}: {self.content}"
