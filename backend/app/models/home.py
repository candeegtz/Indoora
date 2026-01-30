import enum
from typing import List
from sqlmodel import SQLModel, Field, Relationship
from app.models.user import User

class RoomType(str, enum.Enum):
    KITCHEN = "KITCHEN"
    LIVING_ROOM = "LIVING_ROOM"
    BEDROOM = "BEDROOM"
    BATHROOM = "BATHROOM"
    OTHER = "OTHER"

class Home(SQLModel, table=True):
    id: int | None = Field(default=None, primary_key=True)
    name: str
    subject_id: int | None = Field(foreign_key="user.id")

    subject: "User" = Relationship(back_populates="home_as_subject")
    supervisors: List["User"] = Relationship(back_populates="home_as_supervisor")

    rooms: list["Room"] = Relationship(back_populates="home")

class Room(SQLModel, table=True):
    id: int | None = Field(default=None, primary_key=True)
    name: str
    roomType: RoomType
    home_id: int = Field(foreign_key="home.id")

    device: "ReceptorDevice" = Relationship(
        back_populates="room",
        sa_relationship_kwargs={"uselist": False}
    )
    home: "Home" = Relationship(back_populates="rooms")
    positions: list["Position"] = Relationship(back_populates="room")

class ActivityPosition(SQLModel, table=True):
    activity_id: int = Field(foreign_key="activity.id", primary_key=True)
    position_id: int = Field(foreign_key="position.id", primary_key=True)

class Activity(SQLModel, table=True):
    id: int = Field(default=None, primary_key=True)
    name: str

    positions: list["Position"] = Relationship(
        back_populates="activities",
        link_model=ActivityPosition
    )

class Position(SQLModel, table=True):
    id: int = Field(default=None, primary_key=True)
    name: str
    room_id: int = Field(foreign_key="room.id")

    room: "Room" = Relationship(back_populates="positions")
    activities: list["Activity"] = Relationship(
        back_populates="positions",
        link_model=ActivityPosition
    )
