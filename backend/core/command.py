from dataclasses import dataclass


@dataclass
class Command:
    action: str
    object: str
    raw_text: str

    def summary(self) -> str:
        return f"{self.action} {self.object}"
