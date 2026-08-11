"""
SQLAlchemy Declarative Base.
All database models inherit from this Base class.
"""

from sqlalchemy.orm import DeclarativeBase


class Base(DeclarativeBase):
    """
    Base class for all SQLAlchemy ORM models.
    """
    pass
