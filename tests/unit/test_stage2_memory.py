import pytest
from fastapi.testclient import TestClient

from backend.api.app import app
from backend.memory.store import MemoryStore
from shared.contracts.memory import MemoryItem, MemorySource, MemoryType

client = TestClient(app)


def test_memory_store_save_and_retrieve():
    store = MemoryStore()
    item = MemoryItem(
        key="user_favorite_drink",
        content="Cold coffee without sugar",
        memory_type=MemoryType.PREFERENCE,
        source=MemorySource.USER_STATED,
    )
    store.save(item)
    retrieved = store.get(item.id)
    assert retrieved is not None
    assert retrieved.content == "Cold coffee without sugar"


def test_memory_store_rejects_sensitive_without_consent():
    store = MemoryStore()
    item = MemoryItem(
        key="medical_condition",
        content="Asthma",
        sensitive=True,
    )
    with pytest.raises(ValueError, match="consent"):
        store.save(item, user_consented_sensitive=False)

    # Allowed with explicit user consent
    saved = store.save(item, user_consented_sensitive=True)
    assert saved.id == item.id


def test_memory_store_caps_inferred_confidence():
    store = MemoryStore()
    item = MemoryItem(
        key="probable_bedtime",
        content="11:30 PM",
        source=MemorySource.INFERRED,
        confidence=0.99,
    )
    saved = store.save(item)
    assert saved.confidence <= 0.85


def test_memory_store_delete():
    store = MemoryStore()
    item = MemoryItem(key="temp", content="to be removed")
    store.save(item)
    assert store.delete(item.id) is True
    assert store.get(item.id) is None


def test_api_memory_lifecycle():
    # 1. Create
    mem_payload = {
        "memory": {
            "key": "preferred_language",
            "content": "English and Hindi",
            "memory_type": "preference",
            "source": "user_stated",
        },
        "consent_sensitive": False,
    }
    res = client.post("/api/v1/memory", json=mem_payload)
    assert res.status_code == 200
    created = res.json()
    assert created["key"] == "preferred_language"
    mem_id = created["id"]

    # 2. List
    res_list = client.get("/api/v1/memory")
    assert res_list.status_code == 200
    ids = [m["id"] for m in res_list.json()]
    assert mem_id in ids

    # 3. Delete
    res_del = client.delete(f"/api/v1/memory/{mem_id}")
    assert res_del.status_code == 200
    assert res_del.json()["success"] is True


def test_api_context_mode():
    res_get = client.get("/api/v1/context-mode")
    assert res_get.status_code == 200

    res_set = client.post("/api/v1/context-mode", json={"mode": "focus"})
    assert res_set.status_code == 200
    assert res_set.json()["current_mode"] == "focus"
