import os
import pytest
from datetime import datetime

# Obtenemos la clave real que tu conftest.py ha cargado desde el .env
VALID_API_KEY = os.getenv("MOTOR_API_KEY", "fallback_key")


@pytest.fixture
def setup_positioning_data(client):
    register_response = client.post(
        "/auth/register-supervisor",
        json={
            "username": "positioning_admin",
            "name": "Positioning",
            "surnames": "Admin",
            "email": "positioning_admin@gmail.com",
            "password": "123456",
            "userType": "SUPERVISOR_CREATOR",
            "homeName": "Positioning Test Home"
        }
    )
    assert register_response.status_code == 200
    supervisor = register_response.json()
    home_id = supervisor["homeId"]
    
    login_response = client.post(
        "/auth/login",
        json={"username": "positioning_admin", "password": "123456"}
    )
    assert login_response.status_code == 200
    token = login_response.json()["access_token"]
    auth_header = {"Authorization": f"Bearer {token}"}
    
    living_room = client.post(
        "/homes/rooms",
        json={
            "name": "Salón",
            "roomType": "LIVING_ROOM",
            "homeId": home_id
        },
        headers=auth_header
    ).json()
    
    bedroom = client.post(
        "/homes/rooms",
        json={
            "name": "Dormitorio",
            "roomType": "BEDROOM",
            "homeId": home_id
        },
        headers=auth_header
    ).json()
    
    kitchen = client.post(
        "/homes/rooms",
        json={
            "name": "Cocina",
            "roomType": "KITCHEN",
            "homeId": home_id
        },
        headers=auth_header
    ).json()
    
    sofa = client.post(
        "/homes/positions",
        json={
            "name": "Sofá",
            "roomId": living_room["id"]
        },
        headers=auth_header
    ).json()
    
    mesa = client.post(
        "/homes/positions",
        json={
            "name": "Mesa",
            "roomId": living_room["id"]
        },
        headers=auth_header
    ).json()
    
    cama = client.post(
        "/homes/positions",
        json={
            "name": "Cama",
            "roomId": bedroom["id"]
        },
        headers=auth_header
    ).json()
    
    encimera = client.post(
        "/homes/positions",
        json={
            "name": "Encimera",
            "roomId": kitchen["id"]
        },
        headers=auth_header
    ).json()
    
    ver_tele = client.post(
        "/activities/",
        json={
            "name": "Ver televisión",
            "homeId": home_id,
            "positionIds": [sofa["id"], mesa["id"]]
        },
        headers=auth_header
    ).json()
    
    dormir = client.post(
        "/activities/",
        json={
            "name": "Dormir",
            "homeId": home_id,
            "positionIds": [cama["id"]]
        },
        headers=auth_header
    ).json()
    
    cocinar = client.post(
        "/activities/",
        json={
            "name": "Cocinar",
            "homeId": home_id,
            "positionIds": [encimera["id"]]
        },
        headers=auth_header
    ).json()
    
    routine_ver_tele = client.post(
        "/routines/",
        json={
            "name": "Ver Tele por la noche",
            "startTime": "20:00:00",
            "endTime": "22:00:00",
            "days": ["MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY"],
            "activityId": ver_tele["id"]
        },
        headers=auth_header
    ).json()
    
    routine_dormir = client.post(
        "/routines/",
        json={
            "name": "Dormir",
            "startTime": "23:00:00",
            "endTime": "08:00:00",
            "days": ["MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"],
            "activityId": dormir["id"]
        },
        headers=auth_header
    ).json()
    
    routine_cocinar = client.post(
        "/routines/",
        json={
            "name": "Cocinar comida",
            "startTime": "12:00:00",
            "endTime": "14:00:00",
            "days": ["MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY"],
            "activityId": cocinar["id"]
        },
        headers=auth_header
    ).json()
    
    return {
        "token": token,
        "auth_header": auth_header,
        "home_id": home_id,
        "rooms": {
            "salón": living_room,
            "dormitorio": bedroom,
            "cocina": kitchen
        },
        "positions": {
            "sofá": sofa,
            "mesa": mesa,
            "cama": cama,
            "encimera": encimera
        },
        "activities": {
            "ver_tele": ver_tele,
            "dormir": dormir,
            "cocinar": cocinar
        },
        "routines": {
            "ver_tele": routine_ver_tele,
            "dormir": routine_dormir,
            "cocinar": routine_cocinar
        }
    }


def test_receive_stable_position_expected(client, setup_positioning_data):
    data = setup_positioning_data
    
    # 2026-06-19 es Viernes
    response = client.post(
        "/positioning/stable",
        json={
            "home_id": data["home_id"],
            "room": "Salón",
            "position": "Sofá",
            "timestamp": "2026-06-19T20:30:00",
            "deviation_timer": False
        },
        headers={"MOTOR-API-Key": VALID_API_KEY}
    )
    
    assert response.status_code == 200
    result = response.json()
    assert result["status"] == "ok"
    assert result["is_expected"] is True


def test_receive_stable_position_unexpected_no_timer(client, setup_positioning_data):
    data = setup_positioning_data
    
    response = client.post(
        "/positioning/stable",
        json={
            "home_id": data["home_id"],
            "room": "Dormitorio",
            "position": "Cama",
            "timestamp": "2026-06-19T20:30:00",
            "deviation_timer": False
        },
        headers={"MOTOR-API-Key": VALID_API_KEY}
    )
    
    assert response.status_code == 200
    result = response.json()
    assert result["status"] == "ok"
    assert result["is_expected"] is False
    assert "alert_generated" not in result or result.get("alert_generated") is False


def test_receive_stable_position_unexpected_with_timer(client, setup_positioning_data):
    data = setup_positioning_data
    
    response = client.post(
        "/positioning/stable",
        json={
            "home_id": data["home_id"],
            "room": "Cocina",
            "position": "Encimera",
            "timestamp": "2026-06-19T20:30:00",
            "deviation_timer": True
        },
        headers={"MOTOR-API-Key": VALID_API_KEY}
    )
    
    assert response.status_code == 200
    result = response.json()
    assert result["is_expected"] is False
    assert result.get("alert_generated") is True


def test_receive_stable_position_invalid_api_key(client, setup_positioning_data):
    data = setup_positioning_data
    
    response = client.post(
        "/positioning/stable",
        json={
            "home_id": data["home_id"],
            "room": "Salón",
            "position": "Sofá",
            "timestamp": "2026-06-19T20:30:00",
            "deviation_timer": False
        },
        headers={"MOTOR-API-Key": "invalid_key"}
    )
    
    assert response.status_code == 401
    assert "Invalid API Key" in response.json()["detail"]


def test_receive_stable_position_missing_api_key(client, setup_positioning_data):
    data = setup_positioning_data
    
    response = client.post(
        "/positioning/stable",
        json={
            "home_id": data["home_id"],
            "room": "Salón",
            "position": "Sofá",
            "timestamp": "2026-06-19T20:30:00",
            "deviation_timer": False
        }
    )
    
    assert response.status_code in [401, 403, 422]


def test_receive_stable_position_home_not_found(client):
    response = client.post(
        "/positioning/stable",
        json={
            "home_id": 99999,
            "room": "Salón",
            "position": "Sofá",
            "timestamp": "2026-06-19T20:30:00",
            "deviation_timer": False
        },
        headers={"MOTOR-API-Key": VALID_API_KEY}
    )
    
    assert response.status_code == 404
    assert "Home not found" in response.json()["detail"]


def test_receive_stable_position_multiple_valid_positions(client, setup_positioning_data):
    data = setup_positioning_data
    
    response1 = client.post(
        "/positioning/stable",
        json={
            "home_id": data["home_id"],
            "room": "Salón",
            "position": "Sofá",
            "timestamp": "2026-06-19T20:30:00",
            "deviation_timer": False
        },
        headers={"MOTOR-API-Key": VALID_API_KEY}
    )
    assert response1.json()["is_expected"] is True
    
    response2 = client.post(
        "/positioning/stable",
        json={
            "home_id": data["home_id"],
            "room": "Salón",
            "position": "Mesa",
            "timestamp": "2026-06-19T20:30:00",
            "deviation_timer": False
        },
        headers={"MOTOR-API-Key": VALID_API_KEY}
    )
    assert response2.json()["is_expected"] is True


def test_receive_stable_position_time_dependent(client, setup_positioning_data):
    data = setup_positioning_data
    
    response1 = client.post(
        "/positioning/stable",
        json={
            "home_id": data["home_id"],
            "room": "Salón",
            "position": "Sofá",
            "timestamp": "2026-06-19T20:30:00",
            "deviation_timer": False
        },
        headers={"MOTOR-API-Key": VALID_API_KEY}
    )
    assert response1.json()["is_expected"] is True
    
    response2 = client.post(
        "/positioning/stable",
        json={
            "home_id": data["home_id"],
            "room": "Salón",
            "position": "Sofá",
            "timestamp": "2026-06-19T19:30:00",
            "deviation_timer": False
        },
        headers={"MOTOR-API-Key": VALID_API_KEY}
    )
    assert response2.json()["is_expected"] is False


def test_get_unread_alerts_empty(client, setup_positioning_data):
    data = setup_positioning_data
    
    response = client.get(
        f"/positioning/alerts/unread/{data['home_id']}",
        headers=data["auth_header"]
    )
    
    assert response.status_code == 200
    alerts = response.json()
    assert isinstance(alerts, list)
    assert len(alerts) == 0


def test_get_unread_alerts_after_deviation(client, setup_positioning_data):
    data = setup_positioning_data
    
    client.post(
        "/positioning/stable",
        json={
            "home_id": data["home_id"],
            "room": "Cocina",
            "position": "Encimera",
            "timestamp": "2026-06-19T20:30:00",
            "deviation_timer": True
        },
        headers={"MOTOR-API-Key": VALID_API_KEY}
    )
    
    response = client.get(
        f"/positioning/alerts/unread/{data['home_id']}",
        headers=data["auth_header"]
    )
    
    assert response.status_code == 200
    alerts = response.json()
    assert len(alerts) > 0
    assert "message" in alerts[0]
    assert "timestamp" in alerts[0]


def test_get_unread_alerts_unauthorized(client, setup_positioning_data):
    data = setup_positioning_data
    
    other_register = client.post(
        "/auth/register-supervisor",
        json={
            "username": "other_positioning",
            "name": "Other",
            "surnames": "Positioning",
            "email": "other_positioning@gmail.com",
            "password": "123456",
            "userType": "SUPERVISOR_CREATOR",
            "homeName": "Other Positioning Home"
        }
    )
    assert other_register.status_code == 200
    
    other_login = client.post(
        "/auth/login",
        json={"username": "other_positioning", "password": "123456"}
    ).json()
    
    other_headers = {"Authorization": f"Bearer {other_login['access_token']}"}
    
    response = client.get(
        f"/positioning/alerts/unread/{data['home_id']}",
        headers=other_headers
    )
    
    assert response.status_code == 403
    assert "Not authorized" in response.json()["detail"]


def test_mark_alert_read_success(client, setup_positioning_data):
    data = setup_positioning_data
    
    client.post(
        "/positioning/stable",
        json={
            "home_id": data["home_id"],
            "room": "Cocina",
            "position": "Encimera",
            "timestamp": "2026-06-19T20:30:00",
            "deviation_timer": True
        },
        headers={"MOTOR-API-Key": VALID_API_KEY}
    )
    
    alerts_response = client.get(
        f"/positioning/alerts/unread/{data['home_id']}",
        headers=data["auth_header"]
    )
    
    alerts = alerts_response.json()
    alert_id = alerts[0]["id"]
    
    response = client.patch(
        f"/positioning/alerts/{alert_id}/read",
        headers=data["auth_header"]
    )
    
    assert response.status_code == 200
    assert response.json()["status"] == "ok"
    
    unread_response = client.get(
        f"/positioning/alerts/unread/{data['home_id']}",
        headers=data["auth_header"]
    )
    
    unread_alerts = unread_response.json()
    assert not any(a["id"] == alert_id for a in unread_alerts)


def test_mark_alert_read_not_found(client, setup_positioning_data):
    data = setup_positioning_data
    
    response = client.patch(
        "/positioning/alerts/99999/read",
        headers=data["auth_header"]
    )
    
    assert response.status_code == 404
    assert "Alert not found" in response.json()["detail"]


def test_mark_alert_read_unauthorized(client, setup_positioning_data):
    data = setup_positioning_data
    
    client.post(
        "/positioning/stable",
        json={
            "home_id": data["home_id"],
            "room": "Cocina",
            "position": "Encimera",
            "timestamp": "2026-06-19T20:30:00",
            "deviation_timer": True
        },
        headers={"MOTOR-API-Key": VALID_API_KEY}
    )
    
    alerts_response = client.get(
        f"/positioning/alerts/unread/{data['home_id']}",
        headers=data["auth_header"]
    )
    
    alert_id = alerts_response.json()[0]["id"]
    
    other_register = client.post(
        "/auth/register-supervisor",
        json={
            "username": "other_alert_user",
            "name": "Other",
            "surnames": "Alert",
            "email": "other_alert@gmail.com",
            "password": "123456",
            "userType": "SUPERVISOR_CREATOR",
            "homeName": "Other Alert Home"
        }
    )
    
    other_login = client.post(
        "/auth/login",
        json={"username": "other_alert_user", "password": "123456"}
    ).json()
    
    other_headers = {"Authorization": f"Bearer {other_login['access_token']}"}
    
    response = client.patch(
        f"/positioning/alerts/{alert_id}/read",
        headers=other_headers
    )
    
    assert response.status_code == 403
    assert "Not authorized" in response.json()["detail"]


def test_get_rooms_positions_success(client, setup_positioning_data):
    data = setup_positioning_data
    
    response = client.get(
        f"/positioning/rooms_positions/{data['home_id']}",
        headers=data["auth_header"]
    )
    
    assert response.status_code == 200
    result = response.json()
    
    assert "Salón" in result
    assert "Dormitorio" in result
    assert "Cocina" in result
    
    assert isinstance(result["Salón"], list)
    assert isinstance(result["Dormitorio"], list)
    assert isinstance(result["Cocina"], list)


def test_get_rooms_positions_detail(client, setup_positioning_data):
    data = setup_positioning_data
    
    response = client.get(
        f"/positioning/rooms_positions/{data['home_id']}",
        headers=data["auth_header"]
    )
    
    assert response.status_code == 200
    result = response.json()
    
    assert len(result.get("Salón", [])) == 2
    assert len(result.get("Dormitorio", [])) == 1
    assert len(result.get("Cocina", [])) == 1


def test_get_rooms_positions_unauthorized(client, setup_positioning_data):
    data = setup_positioning_data
    
    other_register = client.post(
        "/auth/register-supervisor",
        json={
            "username": "other_rooms",
            "name": "Other",
            "surnames": "Rooms",
            "email": "other_rooms@gmail.com",
            "password": "123456",
            "userType": "SUPERVISOR_CREATOR",
            "homeName": "Other Rooms Home"
        }
    )
    
    other_login = client.post(
        "/auth/login",
        json={"username": "other_rooms", "password": "123456"}
    ).json()
    
    other_headers = {"Authorization": f"Bearer {other_login['access_token']}"}
    
    response = client.get(
        f"/positioning/rooms_positions/{data['home_id']}",
        headers=other_headers
    )
    
    assert response.status_code == 403
    assert "Not authorized" in response.json()["detail"]


def test_get_rooms_positions_unauthorized_no_token(client, setup_positioning_data):
    data = setup_positioning_data
    
    response = client.get(
        f"/positioning/rooms_positions/{data['home_id']}"
    )
    
    assert response.status_code == 401


def test_positioning_complete_workflow(client, setup_positioning_data):
    data = setup_positioning_data
    
    response1 = client.post(
        "/positioning/stable",
        json={
            "home_id": data["home_id"],
            "room": "Salón",
            "position": "Sofá",
            "timestamp": "2026-06-19T20:30:00",
            "deviation_timer": False
        },
        headers={"MOTOR-API-Key": VALID_API_KEY}
    )
    assert response1.status_code == 200
    assert response1.json()["is_expected"] is True
    
    response2 = client.post(
        "/positioning/stable",
        json={
            "home_id": data["home_id"],
            "room": "Cocina",
            "position": "Encimera",
            "timestamp": "2026-06-19T20:30:00",
            "deviation_timer": True
        },
        headers={"MOTOR-API-Key": VALID_API_KEY}
    )
    assert response2.status_code == 200
    assert response2.json()["alert_generated"] is True
    
    response3 = client.get(
        f"/positioning/alerts/unread/{data['home_id']}",
        headers=data["auth_header"]
    )
    assert response3.status_code == 200
    alerts = response3.json()
    assert len(alerts) > 0
    
    for alert in alerts:
        response4 = client.patch(
            f"/positioning/alerts/{alert['id']}/read",
            headers=data["auth_header"]
        )
        assert response4.status_code == 200
    
    response5 = client.get(
        f"/positioning/alerts/unread/{data['home_id']}",
        headers=data["auth_header"]
    )
    assert len(response5.json()) == 0


def test_positioning_day_dependent(client, setup_positioning_data):
    data = setup_positioning_data
    
    response_weekday = client.post(
        "/positioning/stable",
        json={
            "home_id": data["home_id"],
            "room": "Salón",
            "position": "Sofá",
            "timestamp": "2026-06-19T20:30:00", # Viernes
            "deviation_timer": False
        },
        headers={"MOTOR-API-Key": VALID_API_KEY}
    )
    assert response_weekday.json()["is_expected"] is True
    
    response_weekend = client.post(
        "/positioning/stable",
        json={
            "home_id": data["home_id"],
            "room": "Cocina",
            "position": "Encimera",
            "timestamp": "2026-06-20T13:00:00", # Sábado
            "deviation_timer": False
        },
        headers={"MOTOR-API-Key": VALID_API_KEY}
    )
    assert response_weekend.json()["is_expected"] is False


def test_alert_message_contains_activity_name(client, setup_positioning_data):
    data = setup_positioning_data
    
    client.post(
        "/positioning/stable",
        json={
            "home_id": data["home_id"],
            "room": "Cocina",
            "position": "Encimera",
            "timestamp": "2026-06-19T20:30:00",
            "deviation_timer": True
        },
        headers={"MOTOR-API-Key": VALID_API_KEY}
    )
    
    alerts_response = client.get(
        f"/positioning/alerts/unread/{data['home_id']}",
        headers=data["auth_header"]
    )
    
    alerts = alerts_response.json()
    assert len(alerts) > 0
    
    alert_message = alerts[0]["message"]
    assert "Ver televisión" in alert_message or "Rutina" in alert_message