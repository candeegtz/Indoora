from fastapi import HTTPException
from sqlmodel import Session

from Indoora.backend.app.repositories.device_repository import DeviceRepository
from Indoora.backend.app.repositories.home_repository import HomeRepository
from Indoora.backend.app.repositories.user_repository import UserRepository

from Indoora.backend.app.schemas.device import (
    EmisorDeviceCreate, EmisorDeviceUpdate,
    ReceptorDeviceCreate, ReceptorDeviceUpdate
)


class DeviceService:
    def __init__(self, session: Session):
        self.repo = DeviceRepository(session)
        self.home_repo = HomeRepository(session)
        self.user_repo = UserRepository(session)

    
    #------------EmisorDevice (pulsera)------------

    def create_emisor(self, data: EmisorDeviceCreate):
        # MAC no vacía
        if not data.macAddress.strip():
            raise HTTPException(400, "MAC address cannot be empty")

        # Usuario debe existir
        user = self.user_repo.get_user_by_id(data.user_id)
        if not user:
            raise HTTPException(404, "User not found")

        # MAC única
        emisors = self.repo.get_all_emisors()
        if any(d.macAddress == data.macAddress for d in emisors):
            raise HTTPException(400, "MAC address already registered")

        return self.repo.create_emisor(data)

    def get_emisor(self, device_id: int):
        device = self.repo.get_emisor_by_id(device_id)
        if not device:
            raise HTTPException(404, "EmisorDevice not found")
        return device

    def get_all_emisors(self):
        return self.repo.get_all_emisors()

    def update_emisor(self, device_id: int, data: EmisorDeviceUpdate):
        device = self.repo.get_emisor_by_id(device_id)
        if not device:
            raise HTTPException(404, "EmisorDevice not found")

        # MAC única si se actualiza
        if data.macAddress:
            emisors = self.repo.get_all_emisors()
            if any(d.macAddress == data.macAddress and d.id != device_id for d in emisors):
                raise HTTPException(400, "MAC address already registered")

        return self.repo.update_emisor(device_id, data)

    def delete_emisor(self, device_id: int):
        device = self.repo.get_emisor_by_id(device_id)
        if not device:
            raise HTTPException(404, "EmisorDevice not found")

        self.repo.delete_emisor(device_id)


    #------------ReceptorDevice (ESP32)------------

    def create_receptor(self, data: ReceptorDeviceCreate):
        # MAC no vacía
        if not data.macAddress.strip():
            raise HTTPException(400, "MAC address cannot be empty")

        # Room debe existir
        room = self.home_repo.get_room_by_id(data.room_id)
        if not room:
            raise HTTPException(404, "Room not found")

        # MAC única
        receptors = self.repo.get_all_receptors()
        if any(d.macAddress == data.macAddress for d in receptors):
            raise HTTPException(400, "MAC address already registered")

        return self.repo.create_receptor(data)

    def get_receptor(self, device_id: int):
        device = self.repo.get_receptor_by_id(device_id)
        if not device:
            raise HTTPException(404, "ReceptorDevice not found")
        return device

    def get_all_receptors(self):
        return self.repo.get_all_receptors()

    def update_receptor(self, device_id: int, data: ReceptorDeviceUpdate):
        device = self.repo.get_receptor_by_id(device_id)
        if not device:
            raise HTTPException(404, "ReceptorDevice not found")

        # MAC única si se actualiza
        if data.macAddress:
            receptors = self.repo.get_all_receptors()
            if any(d.macAddress == data.macAddress and d.id != device_id for d in receptors):
                raise HTTPException(400, "MAC address already registered")

        return self.repo.update_receptor(device_id, data)

    def delete_receptor(self, device_id: int):
        device = self.repo.get_receptor_by_id(device_id)
        if not device:
            raise HTTPException(404, "ReceptorDevice not found")

        self.repo.delete_receptor(device_id)
