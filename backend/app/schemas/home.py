from __future__ import annotations
from sqlmodel import SQLModel
from typing import Optional, List
from Indoora.backend.app.models.home import RoomType



# ------------Home------------

class HomeBase(SQLModel):
    name: str


class HomeCreate(HomeBase):
    subject_id: int


class HomeUpdate(SQLModel):
    name: Optional[str] = None
    subject_id: Optional[int] = None


class HomeRead(HomeBase):
    id: int
    subject_id: int


# ------------Room------------

class RoomBase(SQLModel):
    name: str
    roomType: RoomType
    home_id: int


class RoomCreate(RoomBase):
    pass


class RoomUpdate(SQLModel):
    name: Optional[str] = None
    roomType: Optional[RoomType] = None
    home_id: Optional[int] = None


class RoomRead(RoomBase):
    id: int
    device_id: Optional[int] = None


# ------------Position------------

class PositionBase(SQLModel):
    name: str
    room_id: int


class PositionCreate(PositionBase):
    pass


class PositionUpdate(SQLModel):
    name: Optional[str] = None
    room_id: Optional[int] = None


class PositionRead(PositionBase):
    id: int


# ------------Activity------------
class ActivityBase(SQLModel): 
    name: str 
    
class ActivityCreate(ActivityBase): 
    pass 

class ActivityUpdate(SQLModel): 
    name: Optional[str] = None 
    
class ActivityRead(ActivityBase): 
    id: int