from app.schemas.device import EmisorDeviceCreate, EmisorDeviceUpdate, ReceptorDeviceCreate, ReceptorDeviceUpdate
from sqlmodel import Session, select
from app.models.models import EmisorDevice, ReceptorDevice


class DeviceRepository:
    def __init__(self, session: Session):
        self.session = session

    # ------------EmisorDevice (pulsera)------------

    def create_emisor(self, device: EmisorDeviceCreate) -> EmisorDevice:
        device = EmisorDevice(
            name = device.name,
            mac_address = device.mac_address, 
            user_id = device.user_id
        )

        self.session.add(device)
        self.session.commit()
        self.session.refresh(device)
        return device
    
    def get_emisor_by_id(self, device_id: int) -> EmisorDevice | None:
        return self.session.get(EmisorDevice, device_id)

    def get_all_emisors(self) -> list[EmisorDevice]:
        return self.session.exec(select(EmisorDevice)).all()
    
    def update_emisor(self, device_id: int, data: EmisorDeviceUpdate) -> EmisorDevice:
        device = self.get_emisor_by_id(device_id)
        if not device:
            raise ValueError("EmisorDevice not found")

        update_data = data.model_dump(exclude_unset=True)

        for key, value in update_data.items():
            setattr(device, key, value)
        
        self.session.commit()
        self.session.refresh(device)
        return device
    
    def delete_emisor(self, device_id: int):
        device = self.get_emisor_by_id(device_id)
        if not device:
            raise ValueError("EmisorDevice not found")
        
        self.session.delete(device)
        self.session.commit()

    def get_emisor_device_by_user_id(self, user_id: int) -> EmisorDevice | None:
        return self.session.exec(select(EmisorDevice).where(EmisorDevice.user_id == user_id)).first()

    # ------------ReceptorDevice (ESP32)------------

    def create_receptor(self, device: ReceptorDeviceCreate) -> ReceptorDevice:
        device = ReceptorDevice(
            name= device.name,
            mac_address = device.mac_address,
            room_id = device.room_id
        )

        self.session.add(device)
        self.session.commit()
        self.session.refresh(device)
        return device
    

    def get_receptor_by_id(self, device_id: int) -> ReceptorDevice | None:
        return self.session.get(ReceptorDevice, device_id)  
    
    def get_all_receptors(self) -> list[ReceptorDevice]:
        return self.session.exec(select(ReceptorDevice)).all()
    
    def update_receptor(self, device_id: int, data: ReceptorDeviceUpdate) -> ReceptorDevice:
        device = self.get_receptor_by_id(device_id)
        if not device:
            raise ValueError("ReceptorDevice not found")    
        
        update_data = data.model_dump(exclude_unset=True)

        for key, value in update_data.items():
            setattr(device, key, value)

        self.session.commit()
        self.session.refresh(device)
        return device
    
    def delete_receptor(self, device_id: int):
        device = self.get_receptor_by_id(device_id)
        if not device:
            raise ValueError("ReceptorDevice not found")
        
        self.session.delete(device)
        self.session.commit()