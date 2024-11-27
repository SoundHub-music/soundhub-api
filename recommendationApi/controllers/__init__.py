from .HealthcheckController import router as healthcheck_router
from .RecommendController import router as recommend_router

routes = [healthcheck_router, recommend_router]

__all__ = ['routes']