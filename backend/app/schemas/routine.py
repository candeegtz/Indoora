from __future__ import annotations
from sqlmodel import SQLModel
from typing import Optional, List
from datetime import time
from app.models.models import DaysOfWeek


# ------------Routine------------

class RoutineBase(SQLModel):
    name: str
    description: Optional[str] = None
    startTime: time
    endTime: time
    days: List[DaysOfWeek]


class RoutineCreate(RoutineBase):
    activity_id: int


class RoutineUpdate(SQLModel):
    name: Optional[str] = None
    description: Optional[str] = None
    startTime: Optional[time] = None
    endTime: Optional[time] = None
    days: Optional[List[DaysOfWeek]] = None
    activity_id: Optional[int] = None


class RoutineRead(RoutineBase):
    id: int
    activity_id: int
    
