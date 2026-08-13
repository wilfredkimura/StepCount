"""
Quotable Motivation API Service.
Fetches daily motivational fitness quotes from the open-source Quotable API
with a resilient local quote fallback if the external service is unavailable.
"""

import logging
import random
from typing import Dict
import httpx

from app.core.config import settings
from app.schemas.motivation import MotivationDto

logger = logging.getLogger(__name__)

# Curated fallback quotes to ensure 100% reliable responses
FALLBACK_QUOTES = [
    {"quote": "Every step you take is a step toward a healthier, happier you.", "author": "StepCount Team"},
    {"quote": "It does not matter how slowly you go as long as you do not stop.", "author": "Confucius"},
    {"quote": "Success starts with self-discipline.", "author": "Dwayne Johnson"},
    {"quote": "Small daily improvements over time lead to stunning results.", "author": "Robin Sharma"},
    {"quote": "The only bad workout is the one that didn't happen.", "author": "Fitness Proverb"},
    {"quote": "Your body can stand almost anything; it's your mind that you have to convince.", "author": "Anonymous"},
    {"quote": "Action is the foundational key to all success.", "author": "Pablo Picasso"},
]


class QuotableService:
    """
    Client for fetching motivational fitness quotes.
    """

    @staticmethod
    async def get_quote() -> MotivationDto:
        """
        Calls the Quotable open-source API to get an inspiring quote.
        Falls back to a curated fitness quote if the external call fails.
        """
        try:
            async with httpx.AsyncClient(timeout=4.0) as client:
                response = await client.get(settings.QUOTABLE_API_URL)
                if response.status_code == 200:
                    data: Dict = response.json()
                    content = data.get("content") or data.get("quote")
                    author = data.get("author", "Unknown")
                    if content:
                        return MotivationDto(quote=content, author=author)
        except Exception as e:
            logger.info(f"External quote service unavailable, using local curated quote: {e}")

        # Choose a random curated fallback quote
        fallback = random.choice(FALLBACK_QUOTES)
        return MotivationDto(quote=fallback["quote"], author=fallback["author"])
