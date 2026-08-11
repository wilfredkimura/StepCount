"""
Unit and Integration Tests for Motivation Quote Endpoint.
"""

import pytest
from httpx import AsyncClient


@pytest.mark.asyncio
async def test_get_motivation_quote(client: AsyncClient):
    """Verifies that the motivation endpoint returns an inspirational quote."""
    response = await client.get("/api/motivation")
    assert response.status_code == 200
    data = response.json()
    assert "quote" in data
    assert len(data["quote"]) > 0
    assert "author" in data
