from __future__ import annotations
import enum
from sqlmodel import SQLModel, Field, Relationship

from Indoora.backend.app.models.home import Room
from Indoora.backend.app.models.user import User

# Dispositivos ESP32
class EmisorDevice(SQLModel, table=True):
    id: int | None = Field(default=None, primary_key=True)
    macAddress: str
    room: Room = Relationship(back_populates="devices")

# Pulsera bluetooth receptora
class ReceptorDevice(SQLModel, table=True):
    id: int | None = Field(default=None, primary_key=True)
    name: str
    macAddress: str
    user: User = Relationship(back_populates="devices")