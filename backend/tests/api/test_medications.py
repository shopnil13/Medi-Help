import pytest
import pytest_asyncio
from httpx import AsyncClient

pytestmark = pytest.mark.asyncio


@pytest_asyncio.fixture
async def auth_headers(client: AsyncClient) -> dict[str, str]:
    response = await client.post(
        "/api/v1/auth/register",
        json={
            "full_name": "Med User",
            "email": "meduser@example.com",
            "password": "password123",
        },
    )
    access_token = response.json()["access_token"]
    return {"Authorization": f"Bearer {access_token}"}


MEDICATION_PAYLOAD = {
    "name": "Metformin",
    "strength": "500mg",
    "dosage_instruction": "Take 1 tablet after breakfast",
    "schedules": [
        {
            "time_of_day": "08:00:00",
            "frequency_type": "daily",
            "meal_relation": "after_food",
            "dose_amount": "1 tablet",
        }
    ],
}


async def test_create_medication_requires_auth(client: AsyncClient) -> None:
    response = await client.post("/api/v1/medications", json=MEDICATION_PAYLOAD)
    assert response.status_code in (401, 403)


async def test_create_and_list_medication(
    client: AsyncClient, auth_headers: dict[str, str]
) -> None:
    create_response = await client.post(
        "/api/v1/medications", json=MEDICATION_PAYLOAD, headers=auth_headers
    )
    assert create_response.status_code == 201
    created = create_response.json()
    assert created["name"] == "Metformin"
    assert created["status"] == "active"
    assert len(created["schedules"]) == 1
    assert created["schedules"][0]["meal_relation"] == "after_food"

    list_response = await client.get("/api/v1/medications", headers=auth_headers)
    assert list_response.status_code == 200
    assert len(list_response.json()) == 1


async def test_get_medication_detail(client: AsyncClient, auth_headers: dict[str, str]) -> None:
    create_response = await client.post(
        "/api/v1/medications", json=MEDICATION_PAYLOAD, headers=auth_headers
    )
    medication_id = create_response.json()["id"]

    detail_response = await client.get(f"/api/v1/medications/{medication_id}", headers=auth_headers)
    assert detail_response.status_code == 200
    assert detail_response.json()["id"] == medication_id


async def test_get_medication_not_owned_returns_404(
    client: AsyncClient, auth_headers: dict[str, str]
) -> None:
    other_register = await client.post(
        "/api/v1/auth/register",
        json={
            "full_name": "Other User",
            "email": "other@example.com",
            "password": "password123",
        },
    )
    other_headers = {"Authorization": f"Bearer {other_register.json()['access_token']}"}

    create_response = await client.post(
        "/api/v1/medications", json=MEDICATION_PAYLOAD, headers=auth_headers
    )
    medication_id = create_response.json()["id"]

    response = await client.get(f"/api/v1/medications/{medication_id}", headers=other_headers)
    assert response.status_code == 404


async def test_update_medication_status(client: AsyncClient, auth_headers: dict[str, str]) -> None:
    create_response = await client.post(
        "/api/v1/medications", json=MEDICATION_PAYLOAD, headers=auth_headers
    )
    medication_id = create_response.json()["id"]

    patch_response = await client.patch(
        f"/api/v1/medications/{medication_id}",
        json={"status": "paused"},
        headers=auth_headers,
    )
    assert patch_response.status_code == 200
    assert patch_response.json()["status"] == "paused"


async def test_delete_medication(client: AsyncClient, auth_headers: dict[str, str]) -> None:
    create_response = await client.post(
        "/api/v1/medications", json=MEDICATION_PAYLOAD, headers=auth_headers
    )
    medication_id = create_response.json()["id"]

    delete_response = await client.delete(
        f"/api/v1/medications/{medication_id}", headers=auth_headers
    )
    assert delete_response.status_code == 204

    get_response = await client.get(f"/api/v1/medications/{medication_id}", headers=auth_headers)
    assert get_response.status_code == 404


async def test_reminder_log_and_adherence_summary(
    client: AsyncClient, auth_headers: dict[str, str]
) -> None:
    create_response = await client.post(
        "/api/v1/medications", json=MEDICATION_PAYLOAD, headers=auth_headers
    )
    medication_id = create_response.json()["id"]

    log_response = await client.post(
        "/api/v1/reminders/log",
        json={
            "medication_id": medication_id,
            "scheduled_at": "2026-01-01T08:00:00Z",
            "action": "taken",
        },
        headers=auth_headers,
    )
    assert log_response.status_code == 201
    assert log_response.json()["action"] == "taken"

    summary_response = await client.get(
        "/api/v1/reminders/adherence-summary?days=365", headers=auth_headers
    )
    assert summary_response.status_code == 200
    body = summary_response.json()
    assert body["taken_count"] == 1
    assert body["total_count"] == 1
