from backend.core.intent import Intent, detect_intent, extract_action


def test_detect_command_intent():
    intent, command = detect_intent("open chrome")
    assert intent == Intent.COMMAND
    assert command is not None
    assert command.action == "open"
    assert command.object == "chrome"


def test_detect_incomplete_command_intent():
    intent, command = detect_intent("open")
    assert intent == Intent.INCOMPLETE_COMMAND
    assert command is None


def test_detect_chat_intent():
    intent, command = detect_intent("hello how are you")
    assert intent == Intent.CHAT
    assert command is None


def test_extract_action():
    action = extract_action("please close the window")
    assert action == "close"
