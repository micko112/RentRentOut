import json
import os
from datetime import datetime, timezone

_LOG_FILE = os.path.join(os.path.dirname(os.path.abspath(__file__)), "data", "chatbot_logs.jsonl")
os.makedirs(os.path.dirname(_LOG_FILE), exist_ok=True)


def log_event(thread_id, event_type, data):
    """Append one JSON line to chatbot_logs.jsonl. Never throws."""
    entry = {
        "timestamp": datetime.now(timezone.utc).isoformat(),
        "thread_id": str(thread_id),
        "event": event_type,
        "data": data,
    }
    try:
        with open(_LOG_FILE, "a", encoding="utf-8") as f:
            f.write(json.dumps(entry, ensure_ascii=False) + "\n")
    except Exception as e:
        print(f"log_event failed ({e}); event={event_type}")
