"""
Unit and Integration Tests for User Profile Endpoints.
"""

import pytest
from httpx import AsyncClient


@pytest.mark.asyncio
async def test_get_and_update_profile(client: AsyncClient):
    """Verifies profile retrieval and updating goal."""
    headers = {"Authorization": "Bearer test_token_athlete_1"}
    
    # 1. Fetch default profile
    res_get = await client.get("/api/profile", headers=headers)
    assert res_get.status_code == 200
    assert res_get.json()["user_id"] == "athlete_1"
    assert res_get.json()["daily_goal"] == 8000

    # 2. Update profile goal and name
    update_payload = {"name": "Marathon Runner", "daily_goal": 12000}
    res_put = await client.put("/api/profile", json=update_payload, headers=headers)
    assert res_put.status_code == 200
    data = res_put.json()
    assert data["name"] == "Marathon Runner"
    assert data["daily_goal"] == 12000

    # 3. Verify changes persist
    res_verify = await client.get("/api/profile", headers=headers)
    assert res_verify.status_code == 200
    assert res_verify.json()["daily_goal"] == 12000
    assert res_verify.json()["name"] == "Marathon Runner"
