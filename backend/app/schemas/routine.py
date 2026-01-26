from __future__ import annotations
from sqlmodel import SQLModel
from typing import Optional, List
from datetime import time
from Indoora.backend.app.models.routine import DaysOfWeek


# ------------DayRoutine------------

class DayRoutineBase(SQLModel):
    day: DaysOfWeek
    home_id: int


class DayRoutineCreate(DayRoutineBase):
    pass


class DayRoutineUpdate(SQLModel):
    day: Optional[DaysOfWeek] = None
    home_id: Optional[int] = None


class DayRoutineRead(DayRoutineBase):
    id: int


# ------------Routine------------

class RoutineBase(SQLModel):
    name: str
    description: Optional[str] = None
    startTime: time
    endTime: time


class RoutineCreate(RoutineBase):
    activity_id: int


class RoutineUpdate(SQLModel):
    name: Optional[str] = None
    description: Optional[str] = None
    startTime: Optional[time] = None
    endTime: Optional[time] = None
    activity_id: Optional[int] = None


class RoutineRead(RoutineBase):
    id: int
    activity_id: int
    dayroutines: List[DayRoutineRead] = []
