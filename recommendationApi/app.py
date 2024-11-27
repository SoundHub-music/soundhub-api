from fastapi import FastAPI

from controllers import routes
from handlers import error_handlers

app = FastAPI(version='1.1')

def include_routers():
    for route in routes:
        app.include_router(route)

def add_exception_handlers():
    for exception, handler in error_handlers:
        app.add_exception_handler(exception, handler)


include_routers()
add_exception_handlers()