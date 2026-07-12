import pytest
from httpx import AsyncClient

pytestmark = pytest.mark.asyncio

REGISTER_PAYLOAD = {
    "full_name": "Test User",
    "email": "test@example.com",
    "password": "password123",
}


async def test_register_returns_tokens(client: AsyncClient) -> None:
    response = await client.post("/api/v1/auth/register", json=REGISTER_PAYLOAD)

    assert response.status_code == 201
    body = response.json()
    assert body["access_token"]
    assert body["refresh_token"]
    assert body["token_type"] == "bearer"


async def test_register_duplicate_email_is_rejected(client: AsyncClient) -> None:
    await client.post("/api/v1/auth/register", json=REGISTER_PAYLOAD)
    response = await client.post("/api/v1/auth/register", json=REGISTER_PAYLOAD)

    assert response.status_code == 409


async def test_login_with_correct_credentials(client: AsyncClient) -> None:
    await client.post("/api/v1/auth/register", json=REGISTER_PAYLOAD)

    response = await client.post(
        "/api/v1/auth/login",
        json={"email": REGISTER_PAYLOAD["email"], "password": REGISTER_PAYLOAD["password"]},
    )

    assert response.status_code == 200
    assert response.json()["access_token"]


async def test_login_with_wrong_password_is_rejected(client: AsyncClient) -> None:
    await client.post("/api/v1/auth/register", json=REGISTER_PAYLOAD)

    response = await client.post(
        "/api/v1/auth/login",
        json={"email": REGISTER_PAYLOAD["email"], "password": "wrong-password"},
    )

    assert response.status_code == 401


async def test_get_me_requires_valid_token(client: AsyncClient) -> None:
    register_response = await client.post("/api/v1/auth/register", json=REGISTER_PAYLOAD)
    access_token = register_response.json()["access_token"]

    response = await client.get(
        "/api/v1/users/me",
        headers={"Authorization": f"Bearer {access_token}"},
    )

    assert response.status_code == 200
    assert response.json()["email"] == REGISTER_PAYLOAD["email"]


async def test_get_me_without_token_is_rejected(client: AsyncClient) -> None:
    response = await client.get("/api/v1/users/me")

    assert response.status_code in (401, 403)


async def test_refresh_returns_new_token_pair(client: AsyncClient) -> None:
    register_response = await client.post("/api/v1/auth/register", json=REGISTER_PAYLOAD)
    refresh_token = register_response.json()["refresh_token"]

    response = await client.post(
        "/api/v1/auth/refresh",
        json={"refresh_token": refresh_token},
    )

    assert response.status_code == 200
    body = response.json()
    assert body["access_token"]
    assert body["refresh_token"] != refresh_token


async def test_refresh_with_reused_token_is_rejected(client: AsyncClient) -> None:
    register_response = await client.post("/api/v1/auth/register", json=REGISTER_PAYLOAD)
    refresh_token = register_response.json()["refresh_token"]

    await client.post("/api/v1/auth/refresh", json={"refresh_token": refresh_token})
    second_attempt = await client.post(
        "/api/v1/auth/refresh", json={"refresh_token": refresh_token}
    )

    assert second_attempt.status_code == 401


async def test_logout_revokes_refresh_token(client: AsyncClient) -> None:
    register_response = await client.post("/api/v1/auth/register", json=REGISTER_PAYLOAD)
    refresh_token = register_response.json()["refresh_token"]

    logout_response = await client.post(
        "/api/v1/auth/logout", json={"refresh_token": refresh_token}
    )
    assert logout_response.status_code == 204

    reuse_response = await client.post(
        "/api/v1/auth/refresh", json={"refresh_token": refresh_token}
    )
    assert reuse_response.status_code == 401
