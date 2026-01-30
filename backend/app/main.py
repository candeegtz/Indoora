from fastapi import FastAPI
from app.database import create_db_and_tables

from app.routers import (
    user_router,
    device_router,
    auth_router,
    routine_router,
    activity_router,
    dayroutine_router,
    home_router
)

app = FastAPI(
    title="Indoora Backend",
    version="0.1.0"
)

@app.on_event("startup")
def on_startup():
    create_db_and_tables()

# Routers
app.include_router(auth_router.router)
app.include_router(user_router.router)
app.include_router(device_router.router)
app.include_router(routine_router.router)
app.include_router(activity_router.router)
app.include_router(dayroutine_router.router)
app.include_router(home_router.router)

@app.get("/")
def root():
    return {"message": "Backend activo"}
