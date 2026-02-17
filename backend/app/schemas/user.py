from __future__ import annotations
from sqlmodel import SQLModel, Field
from typing import Optional
from app.models.models import UserType
from pydantic import ConfigDict, EmailStr


class UserBase(SQLModel):
    username: str
    name: str
    surnames: str
    email: EmailStr
    user_type: UserType = Field(alias="userType")
    
    model_config = ConfigDict(populate_by_name=True)


class UserCreate(UserBase):
    password: str
    home_name: Optional[str] = Field(default=None, alias="homeName")
    subject_username: Optional[str] = Field(default=None, alias="subjectUsername") 
    
    model_config = ConfigDict(populate_by_name=True)


class UserUpdate(SQLModel):
    username: Optional[str] = None
    name: Optional[str] = None
    surnames: Optional[str] = None
    email: Optional[EmailStr] = None
    user_type: Optional[UserType] = Field(default=None, alias="userType")
    password: Optional[str] = None
    
    model_config = ConfigDict(populate_by_name=True)


class UserRead(UserBase):
    id: int
    home_id: Optional[int] = Field(default=None, alias="homeId")    
    
    model_config = ConfigDict(populate_by_name=True)