from fastapi import Depends, HTTPException
from fastapi.security import OAuth2PasswordBearer
from sqlmodel import Session

from app.core.security import decode_token
from app.database import get_session
from app.repositories.user_repository import UserRepository

oauth2_scheme = OAuth2PasswordBearer(tokenUrl="/auth/login")


def get_current_user(
    token: str = Depends(oauth2_scheme),
    session: Session = Depends(get_session)
):
    payload = decode_token(token)
    if not payload:
        raise HTTPException(401, "Invalid or expired token")

    user_id = payload.get("sub")
    repo = UserRepository(session)
    user = repo.get_user_by_id(int(user_id))

    if not user:
        raise HTTPException(404, "User not found")

    return user
