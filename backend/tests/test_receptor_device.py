import pytest
from app.models.models import UserType


@pytest.fixture
def auth_header(client):
    client.post(
        "/auth/register-supervisor",
        json={
            "username": "admin",
            "name": "Admin",
            "surnames": "Root",
            "email": "admin@gmail.com",
            "password": "123456",
            "userType": "SUPERVISOR_CREATOR",
            "homeName": "Test Home"
        }
    )

    login = client.post(
        "/auth/login",
        json={"email": "admin@gmail.com", "password": "123456"}
    ).json()

    return {"Authorization": f"Bearer {login['access_token']}"}


@pytest.fixture
def create_subject_and_room(client, auth_header):

    subject = client.post(
        "/users/",
        json={
            "username": "subject1",
            "name": "Subject",
            "surnames": "One",
            "email": "subject1@gmail.com",
            "password": "123456",
            "userType": "SUBJECT"
        },
        headers=auth_header
    ).json()

    room = client.post(
        "/homes/rooms",
        json={
            "name": "Living Room",
            "roomType": "LIVING_ROOM",
            "homeId": subject["homeId"]
        },
        headers=auth_header
    ).json()

    return subject["id"], room["id"]



def test_create_receptor_device(client, auth_header, create_subject_and_room):
    _, room_id = create_subject_and_room

    response = client.post(
        "/devices/receptor",
        json={
            "name": "ESP32 Living Room",
            "macAddress": "11:22:33:44:55:01",
            "roomId": room_id
        },
        headers=auth_header
    )

    assert response.status_code == 200
    data = response.json()
    assert data["name"] == "ESP32 Living Room"
    assert data["macAddress"] == "11:22:33:44:55:01"
    assert data["roomId"] == room_id


def test_create_receptor_duplicate_mac(client, auth_header, create_subject_and_room):
    _, room_id = create_subject_and_room

    # Crear primer dispositivo
    client.post(
        "/devices/receptor",
        json={
            "name": "ESP32 1",
            "macAddress": "11:22:33:44:55:02",
            "roomId": room_id
        },
        headers=auth_header
    )

    # Intentar crear con misma MAC
    response = client.post(
        "/devices/receptor",
        json={
            "name": "ESP32 2",
            "macAddress": "11:22:33:44:55:02",
            "roomId": room_id
        },
        headers=auth_header
    )

    assert response.status_code == 400
    assert "MAC address already registered" in response.json()["detail"]


def test_create_receptor_room_not_found(client, auth_header):
    response = client.post(
        "/devices/receptor",
        json={
            "name": "ESP32",
            "macAddress": "11:22:33:44:55:03",
            "roomId": 99999  # ID que no existe
        },
        headers=auth_header
    )

    assert response.status_code == 404
    assert "Room not found" in response.json()["detail"]


def test_get_receptor_device(client, auth_header, create_subject_and_room):
    _, room_id = create_subject_and_room

    # Crear dispositivo
    created = client.post(
        "/devices/receptor",
        json={
            "name": "ESP32 Get Test",
            "macAddress": "11:22:33:44:55:04",
            "roomId": room_id
        },
        headers=auth_header
    ).json()

    # Obtener dispositivo
    response = client.get(f"/devices/receptor/{created['id']}", headers=auth_header)

    assert response.status_code == 200
    data = response.json()
    assert data["id"] == created["id"]
    assert data["name"] == "ESP32 Get Test"


def test_get_receptor_not_found(client, auth_header):
    response = client.get("/devices/receptor/99999", headers=auth_header)
    assert response.status_code == 404


def test_get_all_receptors(client, auth_header, create_subject_and_room):
    _, room_id = create_subject_and_room

    # Crear dispositivo
    client.post(
        "/devices/receptor",
        json={
            "name": "ESP32 All Test",
            "macAddress": "11:22:33:44:55:05",
            "roomId": room_id
        },
        headers=auth_header
    )

    response = client.get("/devices/receptor", headers=auth_header)

    assert response.status_code == 200
    assert len(response.json()) >= 1



def test_update_receptor_device(client, auth_header, create_subject_and_room):
    _, room_id = create_subject_and_room

    # Crear dispositivo
    created = client.post(
        "/devices/receptor",
        json={
            "name": "ESP32 Original",
            "macAddress": "11:22:33:44:55:06",
            "roomId": room_id
        },
        headers=auth_header
    ).json()

    # Actualizar
    response = client.put(
        f"/devices/receptor/{created['id']}",
        json={"name": "ESP32 Actualizado"},
        headers=auth_header
    )

    assert response.status_code == 200
    data = response.json()
    assert data["name"] == "ESP32 Actualizado"
    assert data["macAddress"] == "11:22:33:44:55:06"  # No cambió


def test_update_receptor_duplicate_mac(client, auth_header, create_subject_and_room):
    subject_id, room_id = create_subject_and_room

    # Obtener subject para sacar homeId
    subject = client.get(f"/users/{subject_id}", headers=auth_header).json()

    room2 = client.post(
        "/homes/rooms",
        json={
            "name": "Bedroom",
            "roomType": "BEDROOM",
            "homeId": subject["homeId"]
        },
        headers=auth_header
    ).json()

    device1 = client.post(
        "/devices/receptor",
        json={
            "name": "ESP32 1",
            "macAddress": "11:22:33:44:55:07",
            "roomId": room_id
        },
        headers=auth_header
    ).json()

    client.post(
        "/devices/receptor",
        json={
            "name": "ESP32 2",
            "macAddress": "11:22:33:44:55:08",
            "roomId": room2["id"]
        },
        headers=auth_header
    )

    response = client.put(
        f"/devices/receptor/{device1['id']}",
        json={"macAddress": "11:22:33:44:55:08"},
        headers=auth_header
    )

    assert response.status_code == 400
    assert "MAC address already registered" in response.json()["detail"]



def test_delete_receptor_device(client, auth_header, create_subject_and_room):
    _, room_id = create_subject_and_room

    created = client.post(
        "/devices/receptor",
        json={
            "name": "ESP32 Delete Test",
            "macAddress": "11:22:33:44:55:09",
            "roomId": room_id
        },
        headers=auth_header
    ).json()

    response = client.delete(f"/devices/receptor/{created['id']}", headers=auth_header)

    assert response.status_code == 200
    assert "deleted successfully" in response.json()["message"]

    get_response = client.get(f"/devices/receptor/{created['id']}", headers=auth_header)
    assert get_response.status_code == 404



def test_multiple_receptors_same_room(client, auth_header, create_subject_and_room):
    _, room_id = create_subject_and_room

    # Crear primer receptor
    receptor1 = client.post(
        "/devices/receptor",
        json={
            "name": "ESP32 Entrada",
            "macAddress": "11:22:33:44:55:10",
            "roomId": room_id
        },
        headers=auth_header
    )

    receptor2 = client.post(
        "/devices/receptor",
        json={
            "name": "ESP32 Ventana",
            "macAddress": "11:22:33:44:55:11",
            "roomId": room_id
        },
        headers=auth_header
    )

    assert receptor1.status_code == 200
    assert receptor2.status_code == 200
    assert receptor1.json()["roomId"] == receptor2.json()["roomId"]