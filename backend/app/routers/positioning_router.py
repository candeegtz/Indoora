from datetime import datetime

from fastapi import APIRouter, Depends, HTTPException, Header
from sqlmodel import Session
from app.database import get_session
from app.models.models import User
from app.schemas.positioning import StablePosition
from app.services.positioning_service import PositioningService
import os
from app.dependencies.auth import get_current_user

router = APIRouter(prefix="/positioning", tags=["positioning"])

# Leer la clave API del entorno
MOTOR_API_KEY = os.getenv("MOTOR_API_KEY")

def verify_motor_api_key(motor_api_key: str = Header(...)):
    """Valida que la cabecera MOTOR-API-Key coincide con la clave configurada."""
    if motor_api_key != MOTOR_API_KEY:
        raise HTTPException(status_code=401, detail="Invalid API Key")
    return True

@router.post("/stable")
async def receive_stable_position(
    stable: StablePosition,
    _: bool = Depends(verify_motor_api_key),
    session: Session = Depends(get_session)
):
    service = PositioningService(session)
    return service.process_stable_position(
        home_id=stable.home_id,
        room=stable.room,
        position=stable.position,
        timestamp=stable.timestamp,
        deviation_timer=stable.deviation_timer
    )

@router.get("/alerts/unread/{home_id}")
async def get_unread_alerts(
    home_id: int,
    current_user: User = Depends(get_current_user),
    session: Session = Depends(get_session)
):
    service = PositioningService(session)
    return service.get_unread_alerts(home_id, current_user)

@router.patch("/alerts/{alert_id}/read")
async def mark_alert_read(
    alert_id: int,
    current_user: User = Depends(get_current_user),
    session: Session = Depends(get_session)
):
    service = PositioningService(session)
    return service.mark_alert_read(alert_id, current_user)

@router.get("/rooms_positions/{home_id}")
async def get_rooms_positions(
    home_id: int,
    current_user: User = Depends(get_current_user),
    session: Session = Depends(get_session)
):
    service = PositioningService(session)
    return service.get_rooms_positions(home_id, current_user)