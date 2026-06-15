import os
from dotenv import load_dotenv
from sqlmodel import SQLModel, Session, create_engine


# Cargar variables de entorno
load_dotenv()

# Obtener DATABASE_URL del .env
DATABASE_URL = os.getenv("DATABASE_URL")

# Validar que DATABASE_URL existe
if not DATABASE_URL:
    raise ValueError(
        "ERROR: DATABASE_URL no está configurado.\n"
    )


connect_args = {}
if "supabase" in DATABASE_URL:
    connect_args = {"sslmode": "require"}

engine = create_engine(DATABASE_URL, echo=True, connect_args=connect_args)


def create_db_and_tables():
    """Crea todas las tablas """
    print("Creando tablas...")
    SQLModel.metadata.create_all(engine)
 
    print("Base de datos inicializada correctamente")


def get_session():
    with Session(engine) as session:
        yield session