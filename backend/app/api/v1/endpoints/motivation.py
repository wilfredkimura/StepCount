import logging
from fastapi import APIRouter
from app.schemas.motivation import MotivationDto
from app.services.quotable_service import QuotableService

logger = logging.getLogger("stepcount.motivation")

router = APIRouter()


@router.get("", response_model=MotivationDto, summary="Get daily motivational quote")
async def get_motivational_quote() -> MotivationDto:
    """
    Returns an inspiring fitness quote fetched from the Quotable API or local quote bank.
    """
    quote_dto = await QuotableService.get_quote()
    logger.info(f"[QUOTE REQUEST] Served quote: \"{quote_dto.quote}\" - {quote_dto.author}")
    return quote_dto

