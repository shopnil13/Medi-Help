import pytest
import pytest_asyncio
from httpx import AsyncClient

pytestmark = pytest.mark.asyncio


@pytest_asyncio.fixture
async def auth_headers(client: AsyncClient) -> dict[str, str]:
    response = await client.post(
        "/api/v1/auth/register",
        json={
            "full_name": "Vitals User",
            "email": "vitals@example.com",
            "password": "password123",
        },
    )
    return {"Authorization": f"Bearer {response.json()['access_token']}"}


def vital_payload(
    value: float = 72,
    recorded_at: str = "2026-07-01T08:00:00Z",
) -> dict[str, object]:
    return {
        "metric_type": "heart_rate",
        "value_numeric": value,
        "unit": "bpm",
        "recorded_at": recorded_at,
        "source": "manual",
    }


async def test_create_vital_requires_authentication(client: AsyncClient) -> None:
    response = await client.post("/api/v1/vitals", json=vital_payload())
    assert response.status_code in (401, 403)


async def test_create_list_and_filter_vitals(
    client: AsyncClient,
    auth_headers: dict[str, str],
) -> None:
    heart_rate = await client.post(
        "/api/v1/vitals",
        headers=auth_headers,
        json=vital_payload(),
    )
    assert heart_rate.status_code == 201
    assert heart_rate.json()["metric_name"] == "Heart rate"
    assert heart_rate.json()["source"] == "manual"

    glucose = await client.post(
        "/api/v1/vitals",
        headers=auth_headers,
        json={
            "metric_type": "blood_glucose",
            "value_numeric": 5.5,
            "unit": "mmol/L",
            "recorded_at": "2026-07-02T08:00:00Z",
        },
    )
    assert glucose.status_code == 201

    filtered = await client.get(
        "/api/v1/vitals",
        headers=auth_headers,
        params={
            "metric_type": "blood_glucose",
            "source": "manual",
            "start_date": "2026-07-02T00:00:00Z",
            "end_date": "2026-07-03T00:00:00Z",
        },
    )
    assert filtered.status_code == 200
    assert [item["id"] for item in filtered.json()] == [glucose.json()["id"]]


async def test_bulk_sync_creates_blood_pressure_and_custom_records(
    client: AsyncClient,
    auth_headers: dict[str, str],
) -> None:
    response = await client.post(
        "/api/v1/vitals/bulk-sync",
        headers=auth_headers,
        json={
            "records": [
                {
                    "metric_type": "blood_pressure_systolic",
                    "value_numeric": 120,
                    "unit": "mmHg",
                    "recorded_at": "2026-07-01T09:00:00Z",
                },
                {
                    "metric_type": "blood_pressure_diastolic",
                    "value_numeric": 80,
                    "unit": "mmHg",
                    "recorded_at": "2026-07-01T09:00:00Z",
                },
                {
                    "metric_type": "custom",
                    "metric_name": "Temperature",
                    "value_numeric": 37.1,
                    "unit": "C",
                    "recorded_at": "2026-07-01T09:00:00Z",
                },
            ]
        },
    )
    assert response.status_code == 201
    assert [item["metric_name"] for item in response.json()] == [
        "Blood pressure (systolic)",
        "Blood pressure (diastolic)",
        "Temperature",
    ]


async def test_health_connect_bulk_sync_deduplicates_by_metric_and_timestamp(
    client: AsyncClient,
    auth_headers: dict[str, str],
) -> None:
    payload = {
        "records": [
            {
                "metric_type": "heart_rate",
                "value_numeric": 72,
                "unit": "bpm",
                "recorded_at": "2026-07-01T09:00:00Z",
                "source": "health_connect",
            },
            {
                "metric_type": "heart_rate",
                "value_numeric": 99,
                "unit": "bpm",
                "recorded_at": "2026-07-01T09:00:00Z",
                "source": "health_connect",
            },
            {
                "metric_type": "blood_glucose",
                "value_numeric": 108,
                "unit": "mg/dL",
                "recorded_at": "2026-07-01T09:00:00Z",
                "source": "health_connect",
            },
        ]
    }
    first = await client.post("/api/v1/vitals/bulk-sync", headers=auth_headers, json=payload)
    assert first.status_code == 201
    assert len(first.json()) == 2
    assert [item["value_numeric"] for item in first.json()] == [72, 108]

    payload["records"][0]["value_numeric"] = 80
    repeated = await client.post("/api/v1/vitals/bulk-sync", headers=auth_headers, json=payload)
    assert repeated.status_code == 201
    assert [item["id"] for item in repeated.json()] == [item["id"] for item in first.json()]
    assert [item["value_numeric"] for item in repeated.json()] == [72, 108]

    history = await client.get(
        "/api/v1/vitals",
        headers=auth_headers,
        params={"source": "health_connect"},
    )
    assert len(history.json()) == 2


async def test_trends_group_points_and_calculate_direction(
    client: AsyncClient,
    auth_headers: dict[str, str],
) -> None:
    for value, recorded_at in (
        (70, "2026-07-01T08:00:00Z"),
        (80, "2026-07-02T08:00:00Z"),
        (90, "2026-07-03T08:00:00Z"),
    ):
        response = await client.post(
            "/api/v1/vitals",
            headers=auth_headers,
            json=vital_payload(value, recorded_at),
        )
        assert response.status_code == 201

    response = await client.get(
        "/api/v1/vitals/trends",
        headers=auth_headers,
        params={"metric_type": "heart_rate"},
    )
    assert response.status_code == 200
    trend = response.json()[0]
    assert trend["count"] == 3
    assert trend["minimum"] == 70
    assert trend["maximum"] == 90
    assert trend["average"] == 80
    assert trend["latest"] == 90
    assert trend["direction"] == "up"
    assert len(trend["points"]) == 3


async def test_vitals_are_scoped_to_the_current_user(
    client: AsyncClient,
    auth_headers: dict[str, str],
) -> None:
    await client.post("/api/v1/vitals", headers=auth_headers, json=vital_payload())
    registration = await client.post(
        "/api/v1/auth/register",
        json={
            "full_name": "Other Vitals User",
            "email": "other-vitals@example.com",
            "password": "password123",
        },
    )
    other_headers = {"Authorization": f"Bearer {registration.json()['access_token']}"}

    response = await client.get("/api/v1/vitals", headers=other_headers)
    assert response.status_code == 200
    assert response.json() == []


async def test_vital_validation_rejects_missing_provenance_and_invalid_ranges(
    client: AsyncClient,
    auth_headers: dict[str, str],
) -> None:
    custom = vital_payload()
    custom["metric_type"] = "custom"
    assert (
        await client.post("/api/v1/vitals", headers=auth_headers, json=custom)
    ).status_code == 422

    lab_record = vital_payload()
    lab_record["source"] = "lab_report"
    assert (
        await client.post("/api/v1/vitals", headers=auth_headers, json=lab_record)
    ).status_code == 422

    invalid_range = await client.get(
        "/api/v1/vitals",
        headers=auth_headers,
        params={
            "start_date": "2026-07-02T00:00:00Z",
            "end_date": "2026-07-01T00:00:00Z",
        },
    )
    assert invalid_range.status_code == 422
