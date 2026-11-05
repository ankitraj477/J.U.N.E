from shared.contracts.memory import MemoryItem, MemorySource, MemoryType


class MemoryStore:
    """Layered, privacy-preserving memory store for J.U.N.E."""

    def __init__(self):
        self._memories: dict[str, MemoryItem] = {}

    def save(self, memory: MemoryItem, user_consented_sensitive: bool = False) -> MemoryItem:
        if memory.sensitive and not user_consented_sensitive:
            raise ValueError("Sensitive memory cannot be retained without explicit user consent.")

        # Ensure inferred memories are capped in confidence
        if memory.source == MemorySource.INFERRED and memory.confidence > 0.85:
            memory.confidence = 0.85

        self._memories[memory.id] = memory
        return memory

    def get(self, memory_id: str) -> MemoryItem | None:
        return self._memories.get(memory_id)

    def list_all(self, memory_type: MemoryType | None = None) -> list[MemoryItem]:
        items = list(self._memories.values())
        if memory_type:
            items = [m for m in items if m.memory_type == memory_type]
        return sorted(items, key=lambda m: m.created_at, reverse=True)

    def delete(self, memory_id: str) -> bool:
        if memory_id in self._memories:
            del self._memories[memory_id]
            return True
        return False

    def clear(self):
        self._memories.clear()

