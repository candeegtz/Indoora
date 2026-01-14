import enum
from sqlmodel import SQLModel, Field

class UserType(str, enum.Enum):
    ADMIN = "ADMIN"
    SUPERVISOR = "SUPERVISOR"
    SUBJECT = "SUBJECT"

class User(SQLModel, table=True):
    id : int = Field(default=None, primary_key=True)
    username: str = Field(index=True, unique=True)
    name: str
    surnames: str
    email: str = Field(index=True, unique=True)
    password_hash: str
    userType: UserType