from __future__ import annotations
from sqlmodel import SQLModel
from typing import Optional
from Indoora.backend.app.models.user import UserType


class UserBase(SQLModel):
    username: str
    name: str
    surnames: str
    email: str
    userType: UserType


class UserCreate(UserBase):
    password: str


class UserUpdate(SQLModel):
    username: Optional[str] = None
    name: Optional[str] = None
    surnames: Optional[str] = None
    email: Optional[str] = None
    userType: Optional[UserType] = None
    password: Optional[str] = None


class UserRead(UserBase):
    id: int
    home_as_subject_id: Optional[int] = None
    home_as_supervisor_id: Optional[int] = None
