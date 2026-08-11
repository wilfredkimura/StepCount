"""
Unit and Integration Tests for Authentication Endpoints.
"""

import pytest
from httpx import AsyncClient


@pytest.mark.asyncio
async def test_health_check(client: AsyncClient):
    """Verifies service health check returns status healthy."""
    response = await client.get("/health")
    assert response.status_code == 200
    assert response.json()["status"] == "healthy"


@pytest.mark.asyncio
async def test_root_endpoint(client: AsyncClient):
    """Verifies root endpoint returns service info."""
    response = await client.get("/")
    assert response.status_code == 200
    assert response.json()["status"] == "online"


@pytest.mark.asyncio
async def test_firebase_login_success(client: AsyncClient):
    """Verifies that a valid token provisions and returns user profile."""
    headers = {"Authorization": "Bearer test_token_new_user_999"}
    payload = {"email": "newuser@example.com", "name": "New Runner"}
    
    response = await client.post("/api/auth/firebase-login", json=payload, headers=headers)
    assert response.status_code == 200
    data = response.json()
    assert data["user_id"] == "new_user_999"
    assert data["email"] == "newuser@example.com"
    assert data["name"] == "New Runner"
    assert data["daily_goal"] == 8000


@pytest.mark.asyncio
async def test_firebase_login_missing_token(client: AsyncClient):
    """Verifies that missing token results in 401 Unauthorized."""
    payload = {"email": "test@example.com", "name": "Test"}
    response = await client.post("/api/auth/firebase-login", json=payload)
    assert response.status_code == 401
    data = response.json()
    assert "error" in data
    assert data["error"]["code"] == "UNAUTHORIZED"


@pytest.mark.asyncio
async def test_logout_endpoint(client: AsyncClient):
    """Verifies user logout endpoint."""
    headers = {"Authorization": "Bearer test_token_user_123"}
    response = await client.post("/api/auth/logout", headers=headers)
    assert response.status_code == 200
    assert "logged out successfully" in response.json()["message"]
