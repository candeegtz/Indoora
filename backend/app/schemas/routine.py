from __future__ import annotations
from sqlmodel import SQLModel, Field
from typing import Optional, List
from datetime import time
from pydantic import ConfigDict


class RoutineBase(SQLModel):
    name: str
    description: Optional[str] = None
    start_time: time = Field(alias="startTime")
    end_time: time = Field(alias="endTime")
    days: List[str]  
    
    model_config = ConfigDict(populate_by_name=True)


class RoutineCreate(RoutineBase):
    activity_id: int = Field(alias="activityId")
    
    model_config = ConfigDict(populate_by_name=True)


class RoutineUpdate(SQLModel):
    name: Optional[str] = None
    description: Optional[str] = None
    start_time: Optional[time] = Field(default=None, alias="startTime")
    end_time: Optional[time] = Field(default=None, alias="endTime")
    days: Optional[List[str]] = None
    
    model_config = ConfigDict(populate_by_name=True)


class RoutineRead(RoutineBase):
    id: int
    activity_id: int = Field(alias="activityId")
    
    model_config = ConfigDict(populate_by_name=True)