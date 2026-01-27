from fastapi import APIRouter, Depends, HTTPException
from sqlmodel import Session
from Indoora.backend.app.database import get_session
from Indoora.backend.app.repositories.device_repository import DeviceRepository
from Indoora.backend.app.schemas.device import (
    EmisorDeviceCreate, EmisorDeviceUpdate, EmisorDeviceRead,
    ReceptorDeviceCreate, ReceptorDeviceUpdate, ReceptorDeviceRead
)

router = APIRouter(prefix="/devices", tags=["Devices"])


# ------------EmisorDevice (pulsera)------------

@router.post("/emisor", response_model=EmisorDeviceRead)
def create_emisor(data: EmisorDeviceCreate, session: Session = Depends(get_session)):
    repo = DeviceRepository(session)
    return repo.create_emisor(data)


@router.get("/emisor/{device_id}", response_model=EmisorDeviceRead)
def get_emisor(device_id: int, session: Session = Depends(get_session)):
    repo = DeviceRepository(session)
    dev = repo.get_emisor_by_id(device_id)
    if not dev:
        raise HTTPException(404, "EmisorDevice not found")
    return dev


@router.get("/emisor", response_model=list[EmisorDeviceRead])
def get_all_emisors(session: Session = Depends(get_session)):
    repo = DeviceRepository(session)
    return repo.get_all_emisors()


@router.put("/emisor/{device_id}", response_model=EmisorDeviceRead)
def update_emisor(device_id: int, data: EmisorDeviceUpdate, session: Session = Depends(get_session)):
    repo = DeviceRepository(session)
    return repo.update_emisor(device_id, data)


@router.delete("/emisor/{device_id}")
def delete_emisor(device_id: int, session: Session = Depends(get_session)):
    repo = DeviceRepository(session)
    repo.delete_emisor(device_id)
    return {"message": "EmisorDevice deleted successfully"}


# ------------ReceptorDevice (ESP32)------------

@router.post("/receptor", response_model=ReceptorDeviceRead)
def create_receptor(data: ReceptorDeviceCreate, session: Session = Depends(get_session)):
    repo = DeviceRepository(session)
    return repo.create_receptor(data)


@router.get("/receptor/{device_id}", response_model=ReceptorDeviceRead)
def get_receptor(device_id: int, session: Session = Depends(get_session)):
    repo = DeviceRepository(session)
    dev = repo.get_receptor_by_id(device_id)
    if not dev:
        raise HTTPException(404, "ReceptorDevice not found")
    return dev


@router.get("/receptor", response_model=list[ReceptorDeviceRead])
def get_all_receptors(session: Session = Depends(get_session)):
    repo = DeviceRepository(session)
    return repo.get_all_receptors()


@router.put("/receptor/{device_id}", response_model=ReceptorDeviceRead)
def update_receptor(device_id: int, data: ReceptorDeviceUpdate, session: Session = Depends(get_session)):
    repo = DeviceRepository(session)
    return repo.update_receptor(device_id, data)


@router.delete("/receptor/{device_id}")
def delete_receptor(device_id: int, session: Session = Depends(get_session)):
    repo = DeviceRepository(session)
    repo.delete_receptor(device_id)
    return {"message": "ReceptorDevice deleted successfully"}
