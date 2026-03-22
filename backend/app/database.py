import os
from dotenv import load_dotenv
from sqlmodel import SQLModel, Session, create_engine

from app.core.init_db import create_admin_user, create_initial_data
from app.models.models import (
    Home, User, Room, Routine,
    Activity, Position, EmisorDevice,
    ReceptorDevice, ActivityPosition
)

# Cargar variables de entorno
load_dotenv()

# Obtener DATABASE_URL del .env
DATABASE_URL = os.getenv("DATABASE_URL")

# Validar que DATABASE_URL existe
if not DATABASE_URL:
    raise ValueError(
        "❌ ERROR: DATABASE_URL no está configurado.\n"
        "Por favor, crea un archivo .env en la carpeta backend con:\n"
        "DATABASE_URL=postgresql://postgres:TU_CONTRASEÑA@localhost:5432/indoora"
    )

# Validar que es PostgreSQL
if not DATABASE_URL.startswith("postgresql"):
    raise ValueError(
        f"❌ ERROR: Se esperaba una URL de PostgreSQL.\n"
        f"DATABASE_URL actual: {DATABASE_URL}\n"
        f"Formato esperado: postgresql://usuario:contraseña@host:puerto/database"
    )

print(f"🐘 Conectando a PostgreSQL...")
engine = create_engine(DATABASE_URL, echo=True)


def create_db_and_tables():
    """Crea todas las tablas y datos iniciales"""
    print("📊 Creando tablas...")
    SQLModel.metadata.create_all(engine)
    
    print("👤 Creando datos iniciales...")
    with Session(engine) as session:
        create_admin_user(session)
        create_initial_data(session)
    
    print("✅ Base de datos inicializada correctamente")


def get_session():
    with Session(engine) as session:
        yield session