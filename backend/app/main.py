from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.database import create_db_and_tables

from app.routers import (
    user_router,
    device_router,
    auth_router,
    routine_router,
    activity_router,
    home_router
)

app = FastAPI(
    title="Indoora Backend",
    version="0.1.0",
    description="API REST para sistema de posicionamiento interior"
)

# ⚠️ IMPORTANTE: CORS para que Android pueda conectarse
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # Permite todos los orígenes (desarrollo)
    allow_credentials=True,
    allow_methods=["*"],  # Permite GET, POST, PUT, DELETE, etc.
    allow_headers=["*"],  # Permite todos los headers (Authorization, etc.)
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
app.include_router(home_router.router)

@app.get("/")
def root():
    return {"message": "Indoora API - Backend activo"}

@app.get("/health")
def health_check():
    return {"status": "healthy", "database": "postgresql"}