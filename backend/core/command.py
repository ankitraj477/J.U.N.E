from dataclasses import dataclass
from typing import Optional


@dataclass
class Command:
    action: str
    object: str
    raw_text: str

    def summary(self) -> str:
        return f"{self.action} {self.object}"
