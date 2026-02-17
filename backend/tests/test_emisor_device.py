import pytest


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


def test_create_emisor_device(client, auth_header, create_subject_and_room):
    subject_id, _ = create_subject_and_room

    response = client.post(
        "/devices/emisor",
        json={
            "name": "Pulsera Subject 1",
            "macAddress": "AA:BB:CC:DD:EE:01",
            "userId": subject_id
        },
        headers=auth_header
    )

    assert response.status_code == 200
    data = response.json()
    assert data["name"] == "Pulsera Subject 1"
    assert data["macAddress"] == "AA:BB:CC:DD:EE:01"
    assert data["userId"] == subject_id


def test_create_emisor_duplicate_mac(client, auth_header, create_subject_and_room):
    subject_id, _ = create_subject_and_room

    client.post(
        "/devices/emisor",
        json={
            "name": "Pulsera 1",
            "macAddress": "AA:BB:CC:DD:EE:02",
            "userId": subject_id
        },
        headers=auth_header
    )

    response = client.post(
        "/devices/emisor",
        json={
            "name": "Pulsera 2",
            "macAddress": "AA:BB:CC:DD:EE:02",
            "userId": subject_id
        },
        headers=auth_header
    )

    assert response.status_code == 400
    assert "MAC address already registered" in response.json()["detail"]


def test_create_emisor_user_already_has_device(client, auth_header, create_subject_and_room):
    subject_id, _ = create_subject_and_room

    client.post(
        "/devices/emisor",
        json={
            "name": "Pulsera 1",
            "macAddress": "AA:BB:CC:DD:EE:03",
            "userId": subject_id
        },
        headers=auth_header
    )

    response = client.post(
        "/devices/emisor",
        json={
            "name": "Pulsera 2",
            "macAddress": "AA:BB:CC:DD:EE:04",
            "userId": subject_id
        },
        headers=auth_header
    )

    assert response.status_code == 400
    assert "already has an emisor device" in response.json()["detail"]


def test_create_emisor_user_not_found(client, auth_header):
    response = client.post(
        "/devices/emisor",
        json={
            "name": "Pulsera",
            "macAddress": "AA:BB:CC:DD:EE:05",
            "userId": 99999 
        },
        headers=auth_header
    )

    assert response.status_code == 404
    assert "User not found" in response.json()["detail"]


def test_get_emisor_device(client, auth_header, create_subject_and_room):
    subject_id, _ = create_subject_and_room

    created = client.post(
        "/devices/emisor",
        json={
            "name": "Pulsera Get Test",
            "macAddress": "AA:BB:CC:DD:EE:06",
            "userId": subject_id
        },
        headers=auth_header
    ).json()

    response = client.get(f"/devices/emisor/{created['id']}", headers=auth_header)

    assert response.status_code == 200
    data = response.json()
    assert data["id"] == created["id"]
    assert data["name"] == "Pulsera Get Test"


def test_get_emisor_not_found(client, auth_header):
    response = client.get("/devices/emisor/99999", headers=auth_header)
    assert response.status_code == 404


def test_get_all_emisors(client, auth_header, create_subject_and_room):
    subject_id, _ = create_subject_and_room

    client.post(
        "/devices/emisor",
        json={
            "name": "Pulsera All Test",
            "macAddress": "AA:BB:CC:DD:EE:07",
            "userId": subject_id
        },
        headers=auth_header
    )

    response = client.get("/devices/emisor", headers=auth_header)

    assert response.status_code == 200
    assert len(response.json()) >= 1


def test_update_emisor_device(client, auth_header, create_subject_and_room):
    subject_id, _ = create_subject_and_room

    created = client.post(
        "/devices/emisor",
        json={
            "name": "Pulsera Original",
            "macAddress": "AA:BB:CC:DD:EE:08",
            "userId": subject_id
        },
        headers=auth_header
    ).json()

    response = client.put(
        f"/devices/emisor/{created['id']}",
        json={"name": "Pulsera Actualizada"},
        headers=auth_header
    )

    assert response.status_code == 200
    data = response.json()
    assert data["name"] == "Pulsera Actualizada"
    assert data["macAddress"] == "AA:BB:CC:DD:EE:08" 


def test_update_emisor_duplicate_mac(client, auth_header, create_subject_and_room):
    subject_id, _ = create_subject_and_room

    creator2_response = client.post(
        "/users/",
        json={
            "username": "creator2",
            "name": "Creator",
            "surnames": "Two",
            "email": "creator2@gmail.com",
            "password": "123456",
            "userType": "SUPERVISOR_CREATOR",
            "homeName": "Home2"
        },
        headers=auth_header
    )
    
    login2 = client.post(
        "/auth/login",
        json={"email": "creator2@gmail.com", "password": "123456"}
    ).json()
    
    headers2 = {"Authorization": f"Bearer {login2['access_token']}"}

    subject2 = client.post(
        "/users/",
        json={
            "username": "subject2",
            "name": "Subject",
            "surnames": "Two",
            "email": "subject2@gmail.com",
            "password": "123456",
            "userType": "SUBJECT"
        },
        headers=headers2
    ).json()

    device1 = client.post(
        "/devices/emisor",
        json={
            "name": "Pulsera 1",
            "macAddress": "AA:BB:CC:DD:EE:09",
            "userId": subject_id
        },
        headers=auth_header
    ).json()

    client.post(
        "/devices/emisor",
        json={
            "name": "Pulsera 2",
            "macAddress": "AA:BB:CC:DD:EE:10",
            "userId": subject2["id"]
        },
        headers=headers2
    )

    response = client.put(
        f"/devices/emisor/{device1['id']}",
        json={"macAddress": "AA:BB:CC:DD:EE:10"},
        headers=auth_header
    )

    assert response.status_code == 400
    assert "MAC address already registered" in response.json()["detail"]


def test_delete_emisor_device(client, auth_header, create_subject_and_room):
    subject_id, _ = create_subject_and_room

    created = client.post(
        "/devices/emisor",
        json={
            "name": "Pulsera Delete Test",
            "macAddress": "AA:BB:CC:DD:EE:11",
            "userId": subject_id
        },
        headers=auth_header
    ).json()

    response = client.delete(f"/devices/emisor/{created['id']}", headers=auth_header)

    assert response.status_code == 200
    assert "deleted successfully" in response.json()["message"]

    get_response = client.get(f"/devices/emisor/{created['id']}", headers=auth_header)
    assert get_response.status_code == 404