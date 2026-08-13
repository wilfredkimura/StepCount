"""
Unit and Integration Tests for Leaderboard Endpoints.
"""

from datetime import date
import pytest
from httpx import AsyncClient


@pytest.mark.asyncio
async def test_leaderboard_rankings_and_user_rank(client: AsyncClient):
    """Verifies competitive leaderboard ranking logic and personal rank calculation."""
    today_str = date.today().isoformat()

    # User 1 logs 10,000 steps
    headers_user1 = {"Authorization": "Bearer test_token_leader_1"}
    await client.post("/api/auth/firebase-login", json={"email": "u1@test.com", "name": "User One"}, headers=headers_user1)
    await client.post("/api/steps", json={"date": today_str, "steps": 10000, "goal": 8000}, headers=headers_user1)

    # User 2 logs 15,000 steps (top rank)
    headers_user2 = {"Authorization": "Bearer test_token_leader_2"}
    await client.post("/api/auth/firebase-login", json={"email": "u2@test.com", "name": "User Two"}, headers=headers_user2)
    await client.post("/api/steps", json={"date": today_str, "steps": 15000, "goal": 8000}, headers=headers_user2)

    # 1. Check leaderboard sorted by steps
    res_lb = await client.get("/api/leaderboard?period=today&type=steps", headers=headers_user1)
    assert res_lb.status_code == 200
    lb_data = res_lb.json()
    assert len(lb_data) >= 2
    assert lb_data[0]["user_id"] == "leader_2"
    assert lb_data[0]["rank"] == 1
    assert lb_data[0]["steps"] == 15000
    assert lb_data[1]["user_id"] == "leader_1"
    assert lb_data[1]["rank"] == 2
    assert lb_data[1]["steps"] == 10000

    # 2. Check User 1 personal rank position
    res_my_rank = await client.get("/api/leaderboard/me?period=today", headers=headers_user1)
    assert res_my_rank.status_code == 200
    my_data = res_my_rank.json()
    assert my_data["user_id"] == "leader_1"
    assert my_data["rank"] == 2
    assert my_data["steps"] == 10000
