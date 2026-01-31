from core.intent import detect_intent, Intent


def boot():
    print("===================================")
    print(" JUNE booting up...")
    print(" Offline-first personal AI assistant")
    print(" Type 'exit' to shut down")
    print("===================================")


def ask_confirmation_prompt(command: str) -> bool:
    while True:
        reply=input(
            f"JUNE > Do you want me to execute the command: '{command}'? (yes/no): "
        ).strip().lower()
        
        if reply in ["yes", "y"]:
            return True
        elif reply in ["no", "n"]:
            return False
        else:
            print("JUNE > Please respond with 'yes' or 'no'.")
            

def core_loop():
    while True:
        user_input = input("You > ").strip()

        if user_input.lower() in ["exit", "quit"]:
            print("JUNE > Shutting down. Goodbye.")
            break
        

        if not user_input:
            print("JUNE > No input detected. Please enter a command or message.")
            continue
        

        print(f"JUNE processing: {user_input}")

        intent = detect_intent(user_input)
        
        if intent == Intent.COMMAND:
            confirmed=ask_confirmation_prompt(user_input)

            if confirmed:
                    print("JUNE > Command confirmed.")
                    print("JUNE > (Execution layer will be added later)")
            else:
                    print("JUNE > Command cancelled.")

        elif intent == Intent.INCOMPLETE_COMMAND:
                print("JUNE > I detected an action, but I need more details.")
                print("JUNE > Please clarify what you want me to act on.")

        elif intent == Intent.CHAT:
                print("JUNE > Detected CHAT intent")
                print(f"JUNE > You said: '{user_input}'")

        else:
                print("JUNE > I’m not sure how to understand that.")


if __name__ == "__main__":
    boot()
    core_loop()
