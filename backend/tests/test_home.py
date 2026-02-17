import pytest
from app.models.models import RoomType


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
def create_subject(client, auth_header):
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
    
    return subject


def test_create_home(client, auth_header):
    response = client.post(
        "/homes/",
        json={"name": "New Home"},
        headers=auth_header
    )

    assert response.status_code == 200
    assert response.json()["name"] == "New Home"


def test_create_home_empty_name(client, auth_header):
    response = client.post(
        "/homes/",
        json={"name": "   "},
        headers=auth_header
    )

    assert response.status_code == 400
    assert "Home name cannot be empty" in response.json()["detail"]


def test_get_home(client, auth_header, create_subject):
    home_id = create_subject["homeId"]
    
    response = client.get(f"/homes/{home_id}", headers=auth_header)

    assert response.status_code == 200
    assert response.json()["id"] == home_id


def test_get_home_not_found(client, auth_header):
    response = client.get("/homes/99999", headers=auth_header)
    assert response.status_code == 404


def test_get_all_homes_forbidden_for_supervisor(client, auth_header, create_subject):
    response = client.get("/homes/", headers=auth_header)

    assert response.status_code == 403
    assert "Only admins" in response.json()["detail"]


def test_update_home(client, auth_header, create_subject):
    home_id = create_subject["homeId"]
    
    response = client.put(
        f"/homes/{home_id}",
        json={"name": "Updated Home"},
        headers=auth_header
    )

    assert response.status_code == 200
    assert response.json()["name"] == "Updated Home"


def test_delete_home(client, auth_header):
    created = client.post(
        "/homes/",
        json={"name": "Home to Delete"},
        headers=auth_header
    ).json()

    response = client.delete(f"/homes/{created['id']}", headers=auth_header)

    assert response.status_code == 200
    assert "deleted successfully" in response.json()["message"]


def test_create_room(client, auth_header, create_subject):
    home_id = create_subject["homeId"]
    
    response = client.post(
        "/homes/rooms",
        json={
            "name": "Living Room",
            "roomType": "LIVING_ROOM",
            "homeId": home_id
        },
        headers=auth_header
    )

    assert response.status_code == 200
    assert response.json()["name"] == "Living Room"
    assert response.json()["roomType"] == "LIVING_ROOM"


def test_create_room_empty_name(client, auth_header, create_subject):
    home_id = create_subject["homeId"]
    
    response = client.post(
        "/homes/rooms",
        json={
            "name": "   ",
            "roomType": "BEDROOM",
            "homeId": home_id
        },
        headers=auth_header
    )

    assert response.status_code == 400
    assert "Room name cannot be empty" in response.json()["detail"]


def test_create_room_home_not_found(client, auth_header):
    response = client.post(
        "/homes/rooms",
        json={
            "name": "Kitchen",
            "roomType": "KITCHEN",
            "homeId": 99999
        },
        headers=auth_header
    )

    assert response.status_code == 404
    assert "Home not found" in response.json()["detail"]


def test_create_room_forbidden_different_home(client, auth_header, create_subject):
    creator2 = client.post(
        "/users/",
        json={
            "username": "creator2",
            "name": "Creator",
            "surnames": "Two",
            "email": "creator2@gmail.com",
            "password": "123456",
            "userType": "SUPERVISOR_CREATOR",
            "homeName": "Another Home"
        },
        headers=auth_header
    ).json()

    login2 = client.post(
        "/auth/login",
        json={"email": "creator2@gmail.com", "password": "123456"}
    ).json()
    
    headers2 = {"Authorization": f"Bearer {login2['access_token']}"}
    home_id = create_subject["homeId"]

    response = client.post(
        "/homes/rooms",
        json={
            "name": "Kitchen",
            "roomType": "KITCHEN",
            "homeId": home_id
        },
        headers=headers2
    )

    assert response.status_code == 403


def test_get_room(client, auth_header, create_subject):
    home_id = create_subject["homeId"]
    
    created = client.post(
        "/homes/rooms",
        json={
            "name": "Bathroom",
            "roomType": "BATHROOM",
            "homeId": home_id
        },
        headers=auth_header
    ).json()

    response = client.get(f"/homes/rooms/{created['id']}", headers=auth_header)

    assert response.status_code == 200
    assert response.json()["name"] == "Bathroom"


def test_get_room_not_found(client, auth_header):
    response = client.get("/homes/rooms/99999", headers=auth_header)
    assert response.status_code == 404


def test_get_rooms_by_home(client, auth_header, create_subject):
    home_id = create_subject["homeId"]
    
    client.post(
        "/homes/rooms",
        json={
            "name": "Room 1",
            "roomType": "BEDROOM",
            "homeId": home_id
        },
        headers=auth_header
    )

    response = client.get(f"/homes/{home_id}/rooms", headers=auth_header)

    assert response.status_code == 200
    assert len(response.json()) >= 1


def test_update_room(client, auth_header, create_subject):
    home_id = create_subject["homeId"]
    
    created = client.post(
        "/homes/rooms",
        json={
            "name": "Old Name",
            "roomType": "OTHER",
            "homeId": home_id
        },
        headers=auth_header
    ).json()

    response = client.put(
        f"/homes/rooms/{created['id']}",
        json={"name": "New Name"},
        headers=auth_header
    )

    assert response.status_code == 200
    assert response.json()["name"] == "New Name"


def test_delete_room(client, auth_header, create_subject):
    home_id = create_subject["homeId"]
    
    created = client.post(
        "/homes/rooms",
        json={
            "name": "Room to Delete",
            "roomType": "OTHER",
            "homeId": home_id
        },
        headers=auth_header
    ).json()

    response = client.delete(f"/homes/rooms/{created['id']}", headers=auth_header)

    assert response.status_code == 200
    assert "deleted successfully" in response.json()["message"]


def test_create_position(client, auth_header, create_subject):
    home_id = create_subject["homeId"]
    
    room = client.post(
        "/homes/rooms",
        json={
            "name": "Kitchen",
            "roomType": "KITCHEN",
            "homeId": home_id
        },
        headers=auth_header
    ).json()

    response = client.post(
        "/homes/positions",
        json={
            "name": "Near Fridge",
            "roomId": room["id"]
        },
        headers=auth_header
    )

    assert response.status_code == 200
    assert response.json()["name"] == "Near Fridge"


def test_create_position_room_not_found(client, auth_header):
    response = client.post(
        "/homes/positions",
        json={
            "name": "Position",
            "roomId": 99999
        },
        headers=auth_header
    )

    assert response.status_code == 404
    assert "Room not found" in response.json()["detail"]


def test_get_position(client, auth_header, create_subject):
    home_id = create_subject["homeId"]
    
    room = client.post(
        "/homes/rooms",
        json={
            "name": "Bedroom",
            "roomType": "BEDROOM",
            "homeId": home_id
        },
        headers=auth_header
    ).json()

    created = client.post(
        "/homes/positions",
        json={
            "name": "Near Window",
            "roomId": room["id"]
        },
        headers=auth_header
    ).json()

    response = client.get(f"/homes/positions/{created['id']}", headers=auth_header)

    assert response.status_code == 200
    assert response.json()["name"] == "Near Window"


def test_get_position_not_found(client, auth_header):
    response = client.get("/homes/positions/99999", headers=auth_header)
    assert response.status_code == 404


def test_get_positions_by_room(client, auth_header, create_subject):
    home_id = create_subject["homeId"]
    
    room = client.post(
        "/homes/rooms",
        json={
            "name": "Living Room",
            "roomType": "LIVING_ROOM",
            "homeId": home_id
        },
        headers=auth_header
    ).json()

    client.post(
        "/homes/positions",
        json={
            "name": "Position 1",
            "roomId": room["id"]
        },
        headers=auth_header
    )

    response = client.get(f"/homes/rooms/{room['id']}/positions", headers=auth_header)

    assert response.status_code == 200
    assert len(response.json()) >= 1


def test_update_position(client, auth_header, create_subject):
    home_id = create_subject["homeId"]
    
    room = client.post(
        "/homes/rooms",
        json={
            "name": "Office",
            "roomType": "OTHER",
            "homeId": home_id
        },
        headers=auth_header
    ).json()

    created = client.post(
        "/homes/positions",
        json={
            "name": "Old Position",
            "roomId": room["id"]
        },
        headers=auth_header
    ).json()

    response = client.put(
        f"/homes/positions/{created['id']}",
        json={"name": "Updated Position"},
        headers=auth_header
    )

    assert response.status_code == 200
    assert response.json()["name"] == "Updated Position"


def test_delete_position(client, auth_header, create_subject):
    home_id = create_subject["homeId"]
    
    room = client.post(
        "/homes/rooms",
        json={
            "name": "Garage",
            "roomType": "OTHER",
            "homeId": home_id
        },
        headers=auth_header
    ).json()

    created = client.post(
        "/homes/positions",
        json={
            "name": "Position to Delete",
            "roomId": room["id"]
        },
        headers=auth_header
    ).json()

    response = client.delete(f"/homes/positions/{created['id']}", headers=auth_header)

    assert response.status_code == 200
    assert "deleted successfully" in response.json()["message"]