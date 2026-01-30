from __future__ import annotations
from sqlmodel import SQLModel, Field, Relationship
from app.models.user import User

class EmisorDevice(SQLModel, table=True):
    id: int | None = Field(default=None, primary_key=True)
    name: str
    macAddress: str

    user_id: int = Field(foreign_key="user.id")
    user: "User" = Relationship(back_populates="devices")

class ReceptorDevice(SQLModel, table=True):
    id: int | None = Field(default=None, primary_key=True)
    macAddress: str

    room_id: int = Field(foreign_key="room.id", unique=True)
    room: "Room" = Relationship(back_populates="device")
