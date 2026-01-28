from __future__ import annotations
from sqlmodel import SQLModel
from typing import Optional


# ------------EmisorDevice (pulsera)------------

class EmisorDeviceBase(SQLModel):
    name: str
    macAddress: str


class EmisorDeviceCreate(EmisorDeviceBase):
    user_id: int


class EmisorDeviceUpdate(SQLModel):
    name: Optional[str] = None
    macAddress: Optional[str] = None
    user_id: Optional[int] = None


class EmisorDeviceRead(EmisorDeviceBase):
    id: int
    user_id: int


# ------------ReceptorDevice (ESP32)------------

class ReceptorDeviceBase(SQLModel):
    macAddress: str


class ReceptorDeviceCreate(ReceptorDeviceBase):
    room_id: int


class ReceptorDeviceUpdate(SQLModel):
    macAddress: Optional[str] = None
    room_id: Optional[int] = None


class ReceptorDeviceRead(ReceptorDeviceBase):
    id: int
    room_id: int
