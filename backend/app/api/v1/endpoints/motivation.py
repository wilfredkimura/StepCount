"""
Motivation API Endpoint.
Provides a daily motivational quote to inspire users on their fitness journey.
"""

from fastapi import APIRouter
from app.schemas.motivation import MotivationDto
from app.services.quotable_service import QuotableService

router = APIRouter()


@router.get("", response_model=MotivationDto, summary="Get daily motivational quote")
async def get_motivational_quote() -> MotivationDto:
    """
    Returns an inspiring fitness quote fetched from the Quotable API or local quote bank.
    """
    return await QuotableService.get_quote()
