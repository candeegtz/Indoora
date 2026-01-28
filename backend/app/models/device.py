from __future__ import annotations
import enum
from sqlmodel import SQLModel, Field, Relationship

from Indoora.backend.app.models.home import Room
from Indoora.backend.app.models.user import User


# Pulsera bluetooth receptora
class EmisorDevice(SQLModel, table=True):
    id: int | None = Field(default=None, primary_key=True)
    name: str
    macAddress: str

    user_id: int = Field(foreign_key="user.id")
    user: "User" = Relationship(back_populates="devices")
    #nombre para una identificacion  más sencilla a la hora de la configuración

# Dispositivos ESP32
class ReceptorDevice(SQLModel, table=True):
    id: int | None = Field(default=None, primary_key=True)
    macAddress: str

    room_id: int = Field(foreign_key="room.id", unique=True)
    room: "Room" = Relationship(back_populates="devices")