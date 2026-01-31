from enum import Enum
from Command import Command


class Intent(Enum):
    CHAT = "chat"
    COMMAND = "command"
    INCOMPLETE_COMMAND = "incomplete_command"
    UNKNOWN = "unknown"


ACTIONS = {
    "open",
    "close",
    "start",
    "stop",
    "run",
    "launch",
    "shutdown",
    "restart",
    "create",
    "delete",
    "move",
    "copy",
    "send"
}

OBJECTS = {
    "file",
    "folder",
    "chrome",
    "browser",
    "email",
    "mail",
    "application",
    "app"
}


def parse_command(user_input: str) -> Command | None:
    text = user_input.lower().strip()
    words = text.split()

    found_action = None
    found_object = None

    for word in words:
        if word in ACTIONS and not found_action:
            found_action = word
        if word in OBJECTS and not found_object:
            found_object = word

    if found_action and found_object:
        return Command(
            action=found_action,
            object=found_object,
            raw_text=user_input
        )

    return None


def detect_intent(user_input: str) -> tuple[Intent, Command | None]:
    text = user_input.lower().strip()

    if not text:
        return Intent.UNKNOWN, None

    command = parse_command(user_input)

    if command is not None:
        return Intent.COMMAND, command

    # action present but object missing → incomplete
    for word in text.split():
        if word in ACTIONS:
            return Intent.INCOMPLETE_COMMAND, None

    # default fallback
    return Intent.CHAT, None
