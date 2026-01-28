from Indoora.backend.app.services.home_service import HomeService
from fastapi import APIRouter, Depends, HTTPException
from sqlmodel import Session
from Indoora.backend.app.database import get_session
from Indoora.backend.app.schemas.home import (
    HomeCreate, HomeUpdate, HomeRead,
    RoomCreate, RoomUpdate, RoomRead,
    PositionCreate, PositionUpdate, PositionRead
)

router = APIRouter(prefix="/homes", tags=["Homes"])


# ------------Home------------

@router.post("/", response_model=HomeRead)
def create_home(data: HomeCreate, session: Session = Depends(get_session)):
    repo = HomeService(session)
    return repo.create_home(data)


@router.get("/{home_id}", response_model=HomeRead)
def get_home(home_id: int, session: Session = Depends(get_session)):
    repo = HomeService(session)
    home = repo.get_home_by_id(home_id)
    if not home:
        raise HTTPException(404, "Home not found")
    return home


@router.get("/", response_model=list[HomeRead])
def get_all_homes(session: Session = Depends(get_session)):
    repo = HomeService(session)
    return repo.get_all_homes()


@router.put("/{home_id}", response_model=HomeRead)
def update_home(home_id: int, data: HomeUpdate, session: Session = Depends(get_session)):
    repo = HomeService(session)
    return repo.update_home(home_id, data)


@router.delete("/{home_id}")
def delete_home(home_id: int, session: Session = Depends(get_session)):
    repo = HomeService(session)
    repo.delete_home(home_id)
    return {"message": "Home deleted successfully"}


# ------------Room------------

@router.post("/room", response_model=RoomRead)
def create_room(data: RoomCreate, session: Session = Depends(get_session)):
    repo = HomeService(session)
    return repo.create_room(data)


@router.get("/room/{room_id}", response_model=RoomRead)
def get_room(room_id: int, session: Session = Depends(get_session)):
    repo = HomeService(session)
    room = repo.get_room_by_id(room_id)
    if not room:
        raise HTTPException(404, "Room not found")
    return room


@router.get("/{home_id}/rooms", response_model=list[RoomRead])
def get_rooms_by_home(home_id: int, session: Session = Depends(get_session)):
    repo = HomeService(session)
    return repo.get_rooms_by_home(home_id)


@router.put("/room/{room_id}", response_model=RoomRead)
def update_room(room_id: int, data: RoomUpdate, session: Session = Depends(get_session)):
    repo = HomeService(session)
    return repo.update_room(room_id, data)


@router.delete("/room/{room_id}")
def delete_room(room_id: int, session: Session = Depends(get_session)):
    repo = HomeService(session)
    repo.delete_room(room_id)
    return {"message": "Room deleted successfully"}


# ------------Position------------

@router.post("/position", response_model=PositionRead)
def create_position(data: PositionCreate, session: Session = Depends(get_session)):
    repo = HomeService(session)
    return repo.create_position(data)


@router.get("/position/{position_id}", response_model=PositionRead)
def get_position(position_id: int, session: Session = Depends(get_session)):
    repo = HomeService(session)
    pos = repo.get_position_by_id(position_id)
    if not pos:
        raise HTTPException(404, "Position not found")
    return pos


@router.get("/room/{room_id}/positions", response_model=list[PositionRead])
def get_positions_by_room(room_id: int, session: Session = Depends(get_session)):
    repo = HomeService(session)
    return repo.get_positions_by_room(room_id)


@router.put("/position/{position_id}", response_model=PositionRead)
def update_position(position_id: int, data: PositionUpdate, session: Session = Depends(get_session)):
    repo = HomeService(session)
    return repo.update_position(position_id, data)


@router.delete("/position/{position_id}")
def delete_position(position_id: int, session: Session = Depends(get_session)):
    repo = HomeService(session)
    repo.delete_position(position_id)
    return {"message": "Position deleted successfully"}
