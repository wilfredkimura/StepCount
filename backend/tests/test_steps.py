"""
Unit and Integration Tests for Daily Steps Endpoints.
"""

from datetime import date
import pytest
from httpx import AsyncClient


@pytest.mark.asyncio
async def test_upload_and_get_today_steps(client: AsyncClient):
    """Verifies uploading steps and retrieving today's step count."""
    headers = {"Authorization": "Bearer test_token_runner_1"}
    today_str = date.today().isoformat()
    
    # 1. Initially get today steps when none logged (virtual 0)
    res_initial = await client.get("/api/steps/today", headers=headers)
    assert res_initial.status_code == 200
    assert res_initial.json()["steps"] == 0

    # 2. Upload steps for today
    payload = {"date": today_str, "steps": 5420, "goal": 8000}
    res_upload = await client.post("/api/steps", json=payload, headers=headers)
    assert res_upload.status_code == 200
    data = res_upload.json()
    assert data["steps"] == 5420
    assert data["goal"] == 8000
    assert data["date"] == today_str

    # 3. Retrieve today steps after upload
    res_today = await client.get("/api/steps/today", headers=headers)
    assert res_today.status_code == 200
    assert res_today.json()["steps"] == 5420


@pytest.mark.asyncio
async def test_upsert_idempotent_step_update(client: AsyncClient):
    """Verifies that calling upload twice for the same date updates the existing record."""
    headers = {"Authorization": "Bearer test_token_runner_2"}
    
    # First upload
    res1 = await client.post("/api/steps", json={"date": "2026-08-01", "steps": 3000, "goal": 8000}, headers=headers)
    assert res1.status_code == 200
    assert res1.json()["steps"] == 3000

    # Second upload with updated steps on same date
    res2 = await client.post("/api/steps", json={"date": "2026-08-01", "steps": 7500, "goal": 10000}, headers=headers)
    assert res2.status_code == 200
    assert res2.json()["steps"] == 7500
    assert res2.json()["goal"] == 10000

    # History should contain exactly 1 entry for this date
    res_hist = await client.get("/api/steps/history", headers=headers)
    assert len(res_hist.json()) == 1


@pytest.mark.asyncio
async def test_update_and_delete_steps_for_date(client: AsyncClient):
    """Verifies PUT and DELETE on step dates."""
    headers = {"Authorization": "Bearer test_token_runner_3"}
    date_str = "2026-08-05"

    # Create step record
    await client.post("/api/steps", json={"date": date_str, "steps": 4000, "goal": 8000}, headers=headers)

    # PUT update
    res_put = await client.put(f"/api/steps/{date_str}", json={"date": date_str, "steps": 6200, "goal": 8000}, headers=headers)
    assert res_put.status_code == 200
    assert res_put.json()["steps"] == 6200

    # DELETE
    res_del = await client.delete(f"/api/steps/{date_str}", headers=headers)
    assert res_del.status_code == 204

    # Deleting again should return 404
    res_del_again = await client.delete(f"/api/steps/{date_str}", headers=headers)
    assert res_del_again.status_code == 404


@pytest.mark.asyncio
async def test_get_user_streak(client: AsyncClient):
    """Verifies calculating streak stats via backend."""
    headers = {"Authorization": "Bearer test_token_runner_streak"}
    today = date.today()
    today_str = today.isoformat()

    # Upload met goals
    await client.post("/api/steps", json={"date": today_str, "steps": 10000, "goal": 8000}, headers=headers)
    
    res = await client.get("/api/steps/streak", headers=headers)
    assert res.status_code == 200
    data = res.json()
    assert data["current_streak"] == 1
    assert data["best_streak"] == 1
    assert data["total_goal_days"] == 1

