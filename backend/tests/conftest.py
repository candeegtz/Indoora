import sys
import os
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), "..")))

import pytest
from dotenv import load_dotenv
from fastapi.testclient import TestClient
from sqlmodel import SQLModel, create_engine, Session

from app.main import app
from app.database import get_session

load_dotenv(".env")

TEST_DATABASE_URL = os.getenv("DATABASE_URL_TEST", "DATABASE_URL_TEST=postgresql://postgres:1234@localhost:5432/indoora_test")

engine = create_engine(
    TEST_DATABASE_URL,
    echo=False,
    connect_args={"check_same_thread": False} if "sqlite" in TEST_DATABASE_URL else {}
)


@pytest.fixture(scope="function")
def db_session():
    """Resetear BD antes de cada test"""
    SQLModel.metadata.create_all(engine)
    
    with Session(engine) as session:
        yield session
    
    SQLModel.metadata.drop_all(engine)


@pytest.fixture(name="client")
def client_fixture(db_session):
    """Cliente de testing con BD limpia"""
    def override_get_session():
        yield db_session
    
    app.dependency_overrides[get_session] = override_get_session
    
    client = TestClient(app)
    yield client
    
    app.dependency_overrides.clear()