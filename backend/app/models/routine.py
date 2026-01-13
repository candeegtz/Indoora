from datetime import datetime
import enum
from sqlmodel import SQLModel, Field, Relationship

from Indoora.backend.app.models.home import Activity

class Routine(SQLModel, table=True):
    id: int | None = Field(default=None, primary_key=True)
    name: str
    description: str | None = None
    starTime: datetime.time
    endTime: datetime.time
    activity: Activity = Relationship(back_populates="routines")