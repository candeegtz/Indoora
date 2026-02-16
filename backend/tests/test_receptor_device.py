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


def create_subject_and_room(client, headers):
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
        headers=headers
    )

    print("SUBJECT STATUS:", subject.status_code)
    print("SUBJECT RESPONSE:", subject.json())

    subject = subject.json()

    room = client.post(
        "/homes/rooms",

        json={
            "name": "Living Room",
            "roomType": "LIVING_ROOM",
            "homeId": subject["homeId"]
        },
        headers=headers
    ).json()

    return subject["id"], room["id"]


def test_create_receptor_device(client):
    headers = auth_header(client)
    _, room_id = create_subject_and_room(client, headers)

    response = client.post(
        "/devices/receptor",
        json={
            "name": "ESP32 Living Room",
            "macAddress": "11:22:33:44:55:01",
            "roomId": room_id
        },
        headers=headers
    )

    print("STATUS:", response.status_code)
    print("RESPONSE:", response.json())

    assert response.status_code == 200
    data = response.json()
    assert data["name"] == "ESP32 Living Room"
    assert data["macAddress"] == "11:22:33:44:55:01"
    assert data["roomId"] == room_id


def test_create_receptor_duplicate_mac(client):
    headers = auth_header(client)
    _, room_id = create_subject_and_room(client, headers)

    # Crear primer dispositivo
    client.post(
        "/devices/receptor",
        json={
            "name": "ESP32 1",
            "macAddress": "11:22:33:44:55:02",
            "roomId": room_id
        },
        headers=headers
    )

    # Intentar crear con misma MAC
    response = client.post(
        "/devices/receptor",
        json={
            "name": "ESP32 2",
            "macAddress": "11:22:33:44:55:02",
            "roomId": room_id
        },
        headers=headers
    )

    assert response.status_code == 400
    assert "MAC address already registered" in response.json()["detail"]


def test_create_receptor_room_not_found(client):
    headers = auth_header(client)

    response = client.post(
        "/devices/receptor",
        json={
            "name": "ESP32",
            "macAddress": "11:22:33:44:55:03",
            "roomId": 99999  # ID que no existe
        },
        headers=headers
    )

    assert response.status_code == 404
    assert "Room not found" in response.json()["detail"]


def test_get_receptor_device(client):
    headers = auth_header(client)
    _, room_id = create_subject_and_room(client, headers)

    # Crear dispositivo
    created = client.post(
        "/devices/receptor",
        json={
            "name": "ESP32 Get Test",
            "macAddress": "11:22:33:44:55:04",
            "roomId": room_id
        },
        headers=headers
    ).json()

    # Obtener dispositivo
    response = client.get(f"/devices/receptor/{created['id']}", headers=headers)

    assert response.status_code == 200
    data = response.json()
    assert data["id"] == created["id"]
    assert data["name"] == "ESP32 Get Test"


def test_get_receptor_not_found(client):
    headers = auth_header(client)

    response = client.get("/devices/receptor/99999", headers=headers)

    assert response.status_code == 404


def test_get_all_receptors(client):
    headers = auth_header(client)
    _, room_id = create_subject_and_room(client, headers)

    # Crear dispositivo
    client.post(
        "/devices/receptor",
        json={
            "name": "ESP32 All Test",
            "macAddress": "11:22:33:44:55:05",
            "roomId": room_id
        },
        headers=headers
    )

    response = client.get("/devices/receptor", headers=headers)

    assert response.status_code == 200
    assert len(response.json()) >= 1


def test_update_receptor_device(client):
    headers = auth_header(client)
    _, room_id = create_subject_and_room(client, headers)

    # Crear dispositivo
    created = client.post(
        "/devices/receptor",
        json={
            "name": "ESP32 Original",
            "macAddress": "11:22:33:44:55:06",
            "roomId": room_id
        },
        headers=headers
    ).json()

    # Actualizar
    response = client.put(
        f"/devices/receptor/{created['id']}",
        json={"name": "ESP32 Actualizado"},
        headers=headers
    )

    assert response.status_code == 200
    data = response.json()
    assert data["name"] == "ESP32 Actualizado"
    assert data["macAddress"] == "11:22:33:44:55:06"  # No cambió


def test_update_receptor_duplicate_mac(client):
    headers = auth_header(client)
    subject_id, room_id = create_subject_and_room(client, headers)

    subject = client.get(f"/users/{subject_id}", headers=headers).json()

    # Crear segunda room
    room2 = client.post(
        "/homes/rooms",
        json={
            "name": "Bedroom",
            "roomType": "BEDROOM",
            "homeId": subject["homeId"]
        },
        headers=headers
    ).json()

    # Crear dos dispositivos
    device1 = client.post(
        "/devices/receptor",
        json={
            "name": "ESP32 1",
            "macAddress": "11:22:33:44:55:07",
            "roomId": room_id
        },
        headers=headers
    ).json()

    client.post(
        "/devices/receptor",
        json={
            "name": "ESP32 2",
            "macAddress": "11:22:33:44:55:08",
            "roomId": room2["id"]
        },
        headers=headers
    )

    # Intentar actualizar device1 con la MAC de device2
    response = client.put(
        f"/devices/receptor/{device1['id']}",
        json={"macAddress": "11:22:33:44:55:08"},
        headers=headers
    )

    assert response.status_code == 400
    assert "MAC address already registered" in response.json()["detail"]


def test_delete_receptor_device(client):
    headers = auth_header(client)
    _, room_id = create_subject_and_room(client, headers)

    # Crear dispositivo
    created = client.post(
        "/devices/receptor",
        json={
            "name": "ESP32 Delete Test",
            "macAddress": "11:22:33:44:55:09",
            "roomId": room_id
        },
        headers=headers
    ).json()

    # Eliminar
    response = client.delete(f"/devices/receptor/{created['id']}", headers=headers)

    assert response.status_code == 200
    assert "deleted successfully" in response.json()["message"]

    # Verificar que ya no existe
    get_response = client.get(f"/devices/receptor/{created['id']}", headers=headers)
    assert get_response.status_code == 404


# ============ TESTS DE INTEGRACIÓN ============

def test_multiple_receptors_same_room(client):
    """Verificar que se pueden crear múltiples receptores en la misma habitación"""
    headers = auth_header(client)
    _, room_id = create_subject_and_room(client, headers)

    # Crear primer receptor
    receptor1 = client.post(
        "/devices/receptor",
        json={
            "name": "ESP32 Entrada",
            "macAddress": "11:22:33:44:55:10",
            "roomId": room_id
        },
        headers=headers
    )

    # Crear segundo receptor en la misma room
    receptor2 = client.post(
        "/devices/receptor",
        json={
            "name": "ESP32 Ventana",
            "macAddress": "11:22:33:44:55:11",
            "roomId": room_id
        },
        headers=headers
    )

    assert receptor1.status_code == 200
    assert receptor2.status_code == 200
    assert receptor1.json()["roomId"] == receptor2.json()["roomId"]
