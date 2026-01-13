from __future__ import annotations
import enum
from sqlmodel import SQLModel, Field, Relationship

class Home(SQLModel, table=True):
    id: int | None = Field(default=None, primary_key=True)
    name: str
    owner_id: int = Field(foreign_key="user.id")
    rooms: list[Room] = Relationship(back_populates="home")

class RoomType(str, enum.Enum):
    KITCHEN = "KITCHEN"
    LIVING_ROOM = "LIVING_ROOM"
    BEDROOM = "BEDROOM"
    BATHROOM = "BATHROOM"
    OTHER = "OTHER"

class Room(SQLModel, table=True):
    id: int | None = Field(default=None, primary_key=True)
    roomType: RoomType
    home_id: int = Field(foreign_key="home.id")
    home: Home = Relationship(back_populates="rooms")
    positions: list[Position] = Relationship(back_populates="rooms")

class Activity(SQLModel, table=True):
    id: int = Field(default=None, primary_key=True)
    name: str
    positions: list[Position] = Relationship(back_populates="activities", link_model=ActivityPosition)

class Position(SQLModel, table=True):
    id: int = Field(default=None, primary_key=True)
    name: str
    activities: list[Activity] = Relationship(back_populates="positions", link_model=ActivityPosition)

class ActivityPosition(SQLModel, table=True):
    activity_id: int = Field(foreign_key="activity.id", primary_key=True)
    position_id: int = Field(foreign_key="position.id", primary_key=True)