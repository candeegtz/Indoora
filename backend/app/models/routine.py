import enum
from datetime import time
from sqlmodel import SQLModel, Field, Relationship

from Indoora.backend.app.models.home import Activity

class DayRoutineLink(SQLModel, table=True):
    routine_id: int = Field(foreign_key="routine.id", primary_key=True)
    dayroutine_id: int = Field(foreign_key="dayroutine.id", primary_key=True)

class Routine(SQLModel, table=True):
    id: int | None = Field(default=None, primary_key=True)
    name: str
    description: str | None = None
    startTime: time
    endTime: time

    activity: "Activity" = Relationship(back_populates="routines")
    DayRoutines: list["DayRoutine"] = Relationship(
        back_populates="routines", 
        link_model=DayRoutineLink)

class DaysOfWeek(str, enum.Enum):
    MONDAY = "MONDAY"
    TUESDAY = "TUESDAY"
    WEDNESDAY = "WEDNESDAY"
    THURSDAY = "THURSDAY"
    FRIDAY = "FRIDAY"
    SATURDAY = "SATURDAY"
    SUNDAY = "SUNDAY"

class DayRoutine(SQLModel, table=True):
    id: int | None = Field(default=None, primary_key=True)
    day: DaysOfWeek
    home_id: int = Field(foreign_key="home.id")

    routines: list["Routine"] = Relationship(
        back_populates="DayRoutines", 
        link_model=DayRoutineLink)

