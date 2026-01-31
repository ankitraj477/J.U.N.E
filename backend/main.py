from core.intent import detect_intent, Intent, extract_action
from core.command import Command





def boot():
    print("===================================")
    print(" JUNE booting up...")
    print(" Offline-first personal AI assistant")
    print(" Type 'exit' to shut down")
    print("===================================")


def ask_confirmation(command: Command) -> bool:
    while True:
        reply = input(
            f"JUNE > I understood a command to {command.summary()}. Should I proceed? (yes/no): "
        ).strip().lower()

        if reply in ["yes", "y"]:
            return True
        if reply in ["no", "n"]:
            return False

        print("JUNE > Please answer with 'yes' or 'no'.")


# def extract_action(text: str) -> str | None:
#     for word in text.lower().split():
#         if word in ACTIONS:
#             return word
#     return None

def core_loop():
    pending_action = None
    last_command = None  # short-term memory

    while True:
        user_input = input("You > ").strip()

        if user_input.lower() in ["exit", "quit"]:
            print("JUNE > Shutting down. Goodbye.")
            break

        if not user_input:
            print("JUNE > No input detected. Please enter a command or message.")
            continue

        print(f"JUNE processing: {user_input}")

        #  Clarification turn
        if pending_action:
            intent, command = detect_intent(f"{pending_action} {user_input}")

            if command:
                pending_action = None
                confirmed = ask_confirmation(command)

                if confirmed:
                    print(f"JUNE > Command confirmed: {command.summary()}")
                    last_command = command
                    print("JUNE > (Execution layer will be added later)")
                else:
                    print("JUNE > Command cancelled.")
                continue

            print("JUNE > I still need more specific information.")
            continue

        #  Reference resolution with decay
        lowered = user_input.lower()
        if lowered in ["close it", "open it", "delete it", "restart it"]:
            if not last_command:
                print("JUNE > I’m not sure what 'it' refers to.")
                continue

            action = lowered.split()[0]
            command = Command(
                action=action,
                object=last_command.object,
                raw_text=user_input
            )

            confirmed = ask_confirmation(command)
            if confirmed:
                print(f"JUNE > Command confirmed: {command.summary()}")
                print("JUNE > (Execution layer will be added later)")
                last_command = None  #  MEMORY DECAY HERE
            else:
                print("JUNE > Command cancelled.")
            continue

        # Normal intent detection
        intent, command = detect_intent(user_input)

        if intent == Intent.COMMAND and command:
            confirmed = ask_confirmation(command)

            if confirmed:
                print(f"JUNE > Command confirmed: {command.summary()}")
                last_command = command
                print("JUNE > (Execution layer will be added later)")
            else:
                print("JUNE > Command cancelled.")

        elif intent == Intent.INCOMPLETE_COMMAND:
            action = extract_action(user_input)
            pending_action = action
            print("JUNE > I detected an action, but I need more details.")
            print(f"JUNE > What would you like me to {pending_action}?")

        elif intent == Intent.CHAT:
            print("JUNE > Detected CHAT intent")
            print(f"JUNE > You said: '{user_input}'")

        else:
            print("JUNE > I’m not sure how to understand that.")


if __name__ == "__main__":
    boot()
    core_loop()
