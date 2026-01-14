from fastapi import FastAPI
from app.database import create_db_and_tables
from app.routers import auth, users, routines, devices

app = FastAPI(
    title="Indoora  Backend",
    version="0.1.0"
)

@app.on_event("startup")
def on_startup():
    create_db_and_tables()

# app.include_router(auth.router)
app.include_router(users.router)
app.include_router(routines.router)
app.include_router(devices.router)

@app.get("/")
def root():
    return {"message": "Backend activo"}
