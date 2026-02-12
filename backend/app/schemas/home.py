from __future__ import annotations
from sqlmodel import SQLModel, Field
from typing import Optional, List
from pydantic import ConfigDict


# ------------Home------------

class HomeBase(SQLModel):
    name: str


class HomeCreate(HomeBase):
    """
    Creado automáticamente cuando se registra un Supervisor.
    El subject_id se asigna en el backend según el supervisor.
    """
    pass


class HomeUpdate(SQLModel):
    name: Optional[str] = None


class HomeRead(HomeBase):
    id: int
    

# ------------Room------------

class RoomBase(SQLModel):
    name: str
    room_type: str = Field(alias="roomType")  # RoomType enum
    
    model_config = ConfigDict(populate_by_name=True)


class RoomCreate(RoomBase):
    home_id: int = Field(alias="homeId")
    
    model_config = ConfigDict(populate_by_name=True)


class RoomUpdate(SQLModel):
    name: Optional[str] = None
    room_type: Optional[str] = Field(default=None, alias="roomType")
    
    model_config = ConfigDict(populate_by_name=True)


class RoomRead(RoomBase):
    id: int
    home_id: int = Field(alias="homeId")
    
    model_config = ConfigDict(populate_by_name=True)


# ------------Position------------

class PositionBase(SQLModel):
    name: str


class PositionCreate(PositionBase):
    room_id: int = Field(alias="roomId")
    
    model_config = ConfigDict(populate_by_name=True)


class PositionUpdate(SQLModel):
    name: Optional[str] = None
    room_id: Optional[int] = Field(default=None, alias="roomId")
    
    model_config = ConfigDict(populate_by_name=True)


class PositionRead(PositionBase):
    id: int
    room_id: int = Field(alias="roomId")
    
    model_config = ConfigDict(populate_by_name=True)


# ------------Activity------------

class ActivityBase(SQLModel):
    name: str


class ActivityCreate(ActivityBase):
    home_id: int = Field(alias="homeId")
    position_ids: List[int] = Field(default=[], alias="positionIds")  # Opcional
    
    model_config = ConfigDict(populate_by_name=True)


class ActivityUpdate(SQLModel):
    name: Optional[str] = None
    position_ids: Optional[List[int]] = Field(default=None, alias="positionIds")
    
    model_config = ConfigDict(populate_by_name=True)


class ActivityRead(ActivityBase):
    id: int
    home_id: int = Field(alias="homeId")
    
    model_config = ConfigDict(populate_by_name=True)