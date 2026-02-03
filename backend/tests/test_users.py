def auth_header(client):
    register_response = client.post(
        "/auth/register-supervisor",
        json={
            "username": "admin",
            "name": "Admin",
            "surnames": "Root",
            "email": "admin@gmail.com",
            "password": "123456",
            "user_type": "SUPERVISOR"
        }
    )
    print(f"Register status: {register_response.status_code}")
    print(f"Register response: {register_response.json()}")

    login_response = client.post(
        "/auth/login",
        json={"email": "admin@gmail.com", "password": "123456"}
    )
    print(f"Login status: {login_response.status_code}")
    print(f"Login response: {login_response.json()}")
    
    login = login_response.json()
    return {"Authorization": f"Bearer {login['access_token']}"}

def test_create_user(client):
    headers = auth_header(client)

    response = client.post(
        "/users/",
        json={
            "username": "user1",
            "name": "Test",
            "surnames": "User",
            "email": "user1@gmail.com",
            "password": "123456",
            "user_type": "SUBJECT"
        },
        headers=headers
    )

    assert response.status_code == 200
    assert response.json()["email"] == "user1@gmail.com"


def test_duplicate_email(client):
    headers = auth_header(client)

    client.post(
        "/users/",
        json={
            "username": "user1",
            "name": "Test",
            "surnames": "User",
            "email": "dup@gmail.com",
            "password": "123456",
            "user_type": "SUBJECT"
        },
        headers=headers
    )

    response = client.post(
        "/users/",
        json={
            "username": "user2",
            "name": "Test",
            "surnames": "User",
            "email": "dup@gmail.com",
            "password": "123456",
            "user_type": "SUBJECT"
        },
        headers=headers
    )

    assert response.status_code == 400


def test_get_user(client):
    headers = auth_header(client)

    created = client.post(
        "/users/",
        json={
            "username": "user1",
            "name": "Test",
            "surnames": "User",
            "email": "get@gmail.com",
            "password": "123456",
            "user_type": "SUBJECT"
        },
        headers=headers
    ).json()

    response = client.get(f"/users/{created['id']}", headers=headers)
    assert response.status_code == 200
    assert response.json()["email"] == "get@gmail.com"


def test_update_user(client):
    headers = auth_header(client)

    created = client.post(
        "/users/",
        json={
            "username": "user1",
            "name": "Test",
            "surnames": "User",
            "email": "update@gmail.com",
            "password": "123456",
            "user_type": "SUBJECT"
        },
        headers=headers
    ).json()

    response = client.put(
        f"/users/{created['id']}",
        json={"name": "Updated"},
        headers=headers
    )

    assert response.status_code == 200
    assert response.json()["name"] == "Updated"


def test_delete_user(client):
    headers = auth_header(client)

    created = client.post(
        "/users/",
        json={
            "username": "user1",
            "name": "Test",
            "surnames": "User",
            "email": "delete@gmail.com",
            "password": "123456",
            "user_type": "SUBJECT"
        },
        headers=headers
    ).json()

    response = client.delete(f"/users/{created['id']}", headers=headers)
    assert response.status_code == 200

    response = client.get(f"/users/{created['id']}", headers=headers)
    assert response.status_code == 404


def test_get_all_users(client):
    headers = auth_header(client)

    client.post(
        "/users/",
        json={
            "username": "user1",
            "name": "Test",
            "surnames": "User",
            "email": "all1@gmail.com",
            "password": "123456",
            "user_type": "SUBJECT"
        },
        headers=headers
    )

    client.post(
        "/users/",
        json={
            "username": "user2",
            "name": "Test",
            "surnames": "User",
            "email": "all2@gmail.com",
            "password": "123456",
            "user_type": "SUBJECT"
        },
        headers=headers
    )

    response = client.get("/users/", headers=headers)
    assert response.status_code == 200
    assert len(response.json()) >= 2