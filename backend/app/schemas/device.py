from __future__ import annotations
from sqlmodel import SQLModel, Field
from typing import Optional
from pydantic import ConfigDict


# ------------EmisorDevice (pulsera)------------

class EmisorDeviceBase(SQLModel):
    name: str
    mac_address: str = Field(alias="macAddress")
    
    model_config = ConfigDict(populate_by_name=True)


class EmisorDeviceCreate(EmisorDeviceBase):
    user_id: int = Field(alias="userId")

    model_config = ConfigDict(populate_by_name=True)

class EmisorDeviceUpdate(SQLModel):
    name: Optional[str] = None
    mac_address: Optional[str] = Field(default=None, alias="macAddress")
    user_id: Optional[int] = None
    
    model_config = ConfigDict(populate_by_name=True)


class EmisorDeviceRead(EmisorDeviceBase):
    id: int
    user_id: int = Field(alias="userId")


# ------------ReceptorDevice (ESP32)------------

class ReceptorDeviceBase(SQLModel):
    name: str 
    mac_address: str = Field(alias="macAddress")
    
    model_config = ConfigDict(populate_by_name=True)


class ReceptorDeviceCreate(ReceptorDeviceBase):
    room_id: int


class ReceptorDeviceUpdate(SQLModel):
    name: Optional[str] = None 
    mac_address: Optional[str] = Field(default=None, alias="macAddress")
    room_id: Optional[int] = None
    
    model_config = ConfigDict(populate_by_name=True)


class ReceptorDeviceRead(ReceptorDeviceBase):
    id: int
    room_id: int