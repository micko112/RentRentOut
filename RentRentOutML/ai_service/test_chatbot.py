"""
Interaktivni CLI za testiranje produkcionog chatbot-a.

Pokretanje:
    python test_chatbot.py

Komande u toku rada:
    exit        - izlaz iz programa
    new         - pokreni novi razgovor (novi thread_id, gubi se istorija)
    history     - prikazi do sada poslate poruke
    debug on    - prikazuj svaki node detaljno (route, vreme, itd.)
    debug off   - sakri debug info, samo odgovor
"""

import os
import time
import uuid

# Sprecava ChromaDB i drugi printing kad startuje
os.environ.setdefault("TOKENIZERS_PARALLELISM", "false")

from chatbot import agent


def make_thread_id():
    """Napravi kratak random ID za novi razgovor."""
    return "test-" + str(uuid.uuid4())[:8]


def main():
    print("=" * 60)
    print("Bot Igor — interaktivni test mode")
    print("=" * 60)
    print("Komande: exit | new | history | debug on/off")
    print()

    thread_id = make_thread_id()
    debug_mode = True
    turn = 0

    print(f"Thread ID: {thread_id}\n")

    while True:
        try:
            question = input("Ti: ").strip()
        except (EOFError, KeyboardInterrupt):
            print("\nIzlaz.")
            break

        if not question:
            continue

        if question.lower() == "exit":
            print("Izlaz.")
            break

        if question.lower() == "new":
            thread_id = make_thread_id()
            turn = 0
            print(f"\nNovi razgovor. Thread ID: {thread_id}\n")
            continue

        if question.lower() == "history":
            config = {"configurable": {"thread_id": thread_id}}
            snapshot = agent.get_state(config)
            history = snapshot.values.get("chat_history", []) if snapshot.values else []
            if not history:
                print("(prazna istorija)\n")
            else:
                print()
                for line in history:
                    print("  " + line)
                print()
            continue

        if question.lower() == "debug on":
            debug_mode = True
            print("Debug mod uključen.\n")
            continue

        if question.lower() == "debug off":
            debug_mode = False
            print("Debug mod isključen.\n")
            continue

        turn += 1
        config = {"configurable": {"thread_id": thread_id}}
        start = time.time()

        try:
            result = agent.invoke(
                {"question": question, "user_context": ""},
                config=config,
            )
        except Exception as e:
            print(f"GRESKA: {e}\n")
            continue

        elapsed = int((time.time() - start) * 1000)

        if debug_mode:
            print()
            print(f"  [turn {turn} | {elapsed}ms]")
            if "is_relevant" in result:
                print(f"  [is_relevant: {result['is_relevant']}]")
            if "documents" in result and result["documents"]:
                print(f"  [dokumenata: {len(result['documents'])}]")
            print()

        print(f"Bot: {result['answer']}\n")


if __name__ == "__main__":
    main()
