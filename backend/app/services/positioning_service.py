from datetime import datetime
from fastapi import HTTPException
from app.repositories.home_repository import HomeRepository
from app.utils.email import send_alert_email
from sqlmodel import Session
from app.repositories.positioning_repository import PositioningRepository
from app.repositories.alert_repository import AlertRepository
from app.models.models import Alert, User, UserType

class PositioningService:
    def __init__(self, session: Session):
        self.positioning_repo = PositioningRepository(session)
        self.home_repo = HomeRepository(session)
        self.alert_repo = AlertRepository(session)

    def evaluate_routines(self, home_id: int, room: str, position: str, timestamp: datetime, force_alert: bool = False) -> list:
        
        '''Evalúa las rutinas activas para la casa y genera alertas si la posición actual no coincide con las esperadas.'''
        
        # force_alert = false -> No genera alerta porque lo indica el motor
        #                       - Caso: temporizados de desviación no ha llegado a 0
        if not force_alert:
            return []

        now = datetime.now()
        current_time = now.time()
        current_day = now.strftime("%A").upper()
        routines = self.positioning_repo.get_active_routines(home_id, current_time, current_day)
        alerts = []

        # Evaluar cada rutina activa del día de hoy
        for routine in routines:
            expected_list = self.positioning_repo.get_expected_positions(routine)
            if not expected_list:
                continue
            # Si la posición actual está en alguna de las esperadas, no hay alerta
            if (room, position) in expected_list:
                continue
            # Generar alerta (el motor ya ha contado el tiempo de desviación)
            alert = Alert(
                home_id=home_id,
                message=f"Rutina '{routine.name}' incumplida. El usuario del hogar está en '{room}' - '{position}' y debería estar haciendo la actividad '{routine.activity.name}'."
            )
            saved = self.alert_repo.save_alert(alert)
            # Agregar alerta a la lista de respuesta para el motor
            alerts.append({
                "id": saved.id,
                "message": saved.message,
                "timestamp": saved.timestamp.isoformat()
            })
        return alerts
    
    def _check_position_against_routines(self, home_id: int, room: str, position: str) -> tuple[bool, str | None, str | None]:
        
        '''Verifica si la posición actual coincide con las esperadas de las rutinas activas. Devuelve si es esperada y cuáles son las esperadas.'''
        
        now = datetime.now()
        current_time = now.time()
        current_day = now.strftime("%A").upper()
        routines = self.positioning_repo.get_active_routines(home_id, current_time, current_day)
        expected_room = None
        expected_pos = None
        for routine in routines:
            expected_list = self.positioning_repo.get_expected_positions(routine)
            if not expected_list:
                continue
            if expected_room is None:
                # expected room/position -> se pone una de las opciones esperadas de la actividad de la rutina
                expected_room, expected_pos = expected_list[0]
            if (room, position) in expected_list:
                return True, expected_room, expected_pos
        return False, expected_room, expected_pos

    def process_stable_position(self, home_id: int, room: str, position: str, timestamp: datetime, deviation_timer: bool) -> dict:
        
        '''Procesa la posición estable recibida del motor, evalúa contra rutinas y genera alertas si es necesario. Devuelve si la posición es esperada y detalles.'''
        
        # Validaciones (home, subject) usando repositorios
        home = self.home_repo.get_home_by_id(home_id)
        if not home:
            raise HTTPException(404, "Home not found")

        # Evaluar si la posición actual es esperada según rutinas activas
        is_expected, expected_room, expected_pos = self._check_position_against_routines(home_id, room, position)
        
        # Si es desviación y no esperada, generar alertas (y enviar correos)
        if deviation_timer and not is_expected:
            alerts = self.evaluate_routines(
                home_id=home_id,
                room=room,
                position=position,
                timestamp=timestamp,
                force_alert=True
            )
            # Enviar correos a supervisores (usando home_repo.get_supervisor_emails_by_home)
            supervisor_emails = self.home_repo.get_supervisor_emails_by_home(home_id)
            for alert in alerts:
                for email in supervisor_emails:
                    send_alert_email(email, alert["message"])
            return {"status": "ok", "is_expected": False, "alert_generated": True}
        
        # Respuesta al motor: si la posición es esperada o no, y cuáles son las esperadas 
        return {"status": "ok", "is_expected": is_expected, "expected_room": expected_room, "expected_position": expected_pos}
    
    def get_unread_alerts(self, home_id: int, current_user: User) -> list[dict]:
        if current_user.home_id != home_id:
            raise HTTPException(403, "Not authorized for this home")
        alerts = self.alert_repo.get_unread_by_home(home_id)
        return [
            {
                "id": a.id,
                "message": a.message,
                "timestamp": a.timestamp.isoformat()
            }
            for a in alerts
        ]

    def mark_alert_read(self, alert_id: int, current_user: User) -> dict:
        alert = self.alert_repo.get_by_id(alert_id)  # Necesitas este método en AlertRepository
        if not alert:
            raise HTTPException(404, "Alert not found")
        if alert.home_id != current_user.home_id:
            raise HTTPException(403, "Not authorized")
        self.alert_repo.mark_as_read(alert_id)
        return {"status": "ok"}

    def get_rooms_positions(self, home_id: int, current_user: User) -> dict:
        # Permitir acceso si es admin o pertenece a la casa
        if current_user.user_type != UserType.ADMIN and current_user.home_id != home_id:
            raise HTTPException(403, "Not authorized")
        return self.home_repo.get_rooms_and_positions(home_id)