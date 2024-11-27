from typing import Optional

from database import Database
from repository import GenreRepository
from services import RecommendationService


class DependencyInjector:
    @staticmethod
    def get_recommend_service() -> Optional[RecommendationService]:
        return RecommendationService(
            genre_repository=DependencyInjector.get_user_repository()
        )

    @staticmethod
    def get_db() -> Optional[Database]:
        return Database()

    @staticmethod
    def get_user_repository():
        return GenreRepository(db=DependencyInjector.get_db())