from sqlmodel import SQLModel

class LoginRequest(SQLModel):
    email: str
    password: str

class RefreshRequest(SQLModel): 
    refresh_token: str