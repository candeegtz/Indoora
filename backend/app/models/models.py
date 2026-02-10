import enum
from datetime import time
from typing import Optional, List
from sqlmodel import SQLModel, Field, Relationship, Column
from sqlalchemy import JSON
from sqlalchemy.orm import Mapped


class UserType(str, enum.Enum):
    ADMIN = "ADMIN"
    SUPERVISOR_CREATOR = "SUPERVISOR_CREATOR"
    SUPERVISOR = "SUPERVISOR"  
    SUBJECT = "SUBJECT"


class DaysOfWeek(str, enum.Enum):
    MONDAY = "MONDAY"
    TUESDAY = "TUESDAY"
    WEDNESDAY = "WEDNESDAY"
    THURSDAY = "THURSDAY"
    FRIDAY = "FRIDAY"
    SATURDAY = "SATURDAY"
    SUNDAY = "SUNDAY"


class RoomType(str, enum.Enum):
    KITCHEN = "KITCHEN"
    LIVING_ROOM = "LIVING_ROOM"
    BEDROOM = "BEDROOM"
    BATHROOM = "BATHROOM"
    OTHER = "OTHER"


class ActivityPosition(SQLModel, table=True):
    activity_id: int = Field(foreign_key="activity.id", primary_key=True)
    position_id: int = Field(foreign_key="position.id", primary_key=True)


class Home(SQLModel, table=True):
    id: Optional[int] = Field(default=None, primary_key=True)
    name: str

    users: Mapped[List["User"]] = Relationship(back_populates="home")

    rooms: Mapped[List["Room"]] = Relationship(
        back_populates="home",
        sa_relationship_kwargs={"cascade": "all, delete-orphan"}
    )
    
    activities: Mapped[List["Activity"]] = Relationship(
        back_populates="home",
        sa_relationship_kwargs={"cascade": "all, delete-orphan"}
    )


class User(SQLModel, table=True):
    id: Optional[int] = Field(default=None, primary_key=True)
    username: str = Field(index=True, unique=True)
    name: str
    surnames: str
    email: str = Field(index=True)
    password_hash: str
    user_type: UserType

    home_id: Optional[int] = Field(default=None, foreign_key="home.id")
    home: Mapped[Optional["Home"]] = Relationship(back_populates="users")

    emisor_device: Mapped[Optional["EmisorDevice"]] = Relationship(
        back_populates="user",
        sa_relationship_kwargs={"uselist": False, "cascade": "all, delete-orphan"}
    )


class EmisorDevice(SQLModel, table=True):
    id: Optional[int] = Field(default=None, primary_key=True)
    name: str
    mac_address: str = Field(index=True, unique=True)

    user_id: int = Field(foreign_key="user.id", unique=True)
    user: Mapped["User"] = Relationship(back_populates="emisor_device")


class Room(SQLModel, table=True):
    id: Optional[int] = Field(default=None, primary_key=True)
    name: str
    room_type: RoomType

    home_id: int = Field(foreign_key="home.id")
    home: Mapped["Home"] = Relationship(back_populates="rooms")

    receptor_devices: Mapped[List["ReceptorDevice"]] = Relationship(
        back_populates="room",
        sa_relationship_kwargs={"cascade": "all, delete-orphan"}
    )
    
    positions: Mapped[List["Position"]] = Relationship(
        back_populates="room",
        sa_relationship_kwargs={"cascade": "all, delete-orphan"}
    )


class ReceptorDevice(SQLModel, table=True):
    id: Optional[int] = Field(default=None, primary_key=True)
    name: str
    mac_address: str = Field(index=True, unique=True)

    room_id: int = Field(foreign_key="room.id")
    room: Mapped["Room"] = Relationship(back_populates="receptor_devices")


class Position(SQLModel, table=True):
    id: Optional[int] = Field(default=None, primary_key=True)
    name: str
    
    room_id: int = Field(foreign_key="room.id")
    room: Mapped["Room"] = Relationship(back_populates="positions")

    activities: Mapped[List["Activity"]] = Relationship(
        back_populates="positions",
        link_model=ActivityPosition
    )


class Activity(SQLModel, table=True):
    id: Optional[int] = Field(default=None, primary_key=True)
    name: str

    home_id: int = Field(foreign_key="home.id")
    home: Mapped["Home"] = Relationship(back_populates="activities")

    positions: Mapped[List["Position"]] = Relationship(
        back_populates="activities",
        link_model=ActivityPosition
    )
    
    routines: Mapped[List["Routine"]] = Relationship(
        back_populates="activity",
        sa_relationship_kwargs={"cascade": "all, delete-orphan"}
    )


class Routine(SQLModel, table=True):
    id: Optional[int] = Field(default=None, primary_key=True)
    name: str
    description: Optional[str] = None
    start_time: time
    end_time: time
    days: List[DaysOfWeek]

    activity_id: int = Field(foreign_key="activity.id")
    activity: Mapped["Activity"] = Relationship(back_populates="routines")