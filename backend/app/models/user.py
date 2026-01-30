import enum
from typing import List
from sqlmodel import SQLModel, Field, Relationship

class UserType(str, enum.Enum):
    ADMIN = "ADMIN"
    SUPERVISOR = "SUPERVISOR"
    SUBJECT = "SUBJECT"

class User(SQLModel, table=True):
    id : int | None = Field(default=None, primary_key=True)
    username: str = Field(index=True, unique=True)
    name: str
    surnames: str
    email: str = Field(index=True, unique=True)
    password_hash: str
    userType: UserType

    # Relaciones SIN imports directos
    device: "EmisorDevice" = Relationship(
        back_populates="user",
        sa_relationship_kwargs={"uselist": False}
    )

    home_as_subject: "Home" = Relationship(back_populates="subject")

    home_as_supervisor_id: int | None = Field(
        foreign_key="home.id",
        unique=True
    )
    home_as_supervisor: "Home" = Relationship(back_populates="supervisors")
