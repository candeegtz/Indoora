from sqlmodel import SQLModel

class LoginRequest(SQLModel):
    username: str
    password: str

class RefreshRequest(SQLModel): 
    refresh_token: str