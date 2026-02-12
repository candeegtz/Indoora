def auth_header(client):
    register_response = client.post(
        "/auth/register-supervisor",
        json={
            "username": "admin",
            "name": "Admin",
            "surnames": "Root",
            "email": "admin@gmail.com",
            "password": "123456",
            "userType": "SUPERVISOR_CREATOR",
            "homeName": "CasaAdmin"
        }
    )
    
    print("Register status:", register_response.status_code)
    print("Register response:", register_response.json())

    login_response = client.post(
        "/auth/login",
        json={"email": "admin@gmail.com", "password": "123456"}
    )

    print("Login status:", login_response.status_code)
    print("Login response:", login_response.json())

    token = login_response.json()["access_token"]

    return {"Authorization": f"Bearer {token}"}

def test_create_supervisor_creator(client):
    headers = auth_header(client)

    response = client.post(
        "/users/",
        json={
            "username": "creator2",
            "name": "Creator",
            "surnames": "Two",
            "email": "creator2@gmail.com",
            "password": "123456",
            "userType": "SUPERVISOR_CREATOR",
            "homeName": "CasaNueva"
        },
        headers=headers
    )

    assert response.status_code == 200
    assert response.json()["homeId"] is not None

def test_create_supervisor_without_subject_username(client):
    headers = auth_header(client)

    response = client.post(
        "/users/",
        json={
            "username": "sup1",
            "name": "Sup",
            "surnames": "One",
            "email": "sup1@gmail.com",
            "password": "123456",
            "userType": "SUPERVISOR"
        },
        headers=headers
    )

    assert response.status_code == 400

def test_create_subject_from_creator(client):
    headers = auth_header(client)

    response = client.post(
        "/users/",
        json={
            "username": "subject1",
            "name": "Sub",
            "surnames": "One",
            "email": "subject1@gmail.com",
            "password": "123456",
            "userType": "SUBJECT"
        },
        headers=headers
    )

    print("Status:", response.status_code)
    print("Response:", response.json())  

    assert response.status_code == 200
    assert response.json()["homeId"] is not None

def test_create_second_subject_same_home(client):
    headers = auth_header(client)

    client.post(
        "/users/",
        json={
            "username": "subject1",
            "name": "Sub",
            "surnames": "One",
            "email": "subject1@gmail.com",
            "password": "123456",
            "userType": "SUBJECT"
        },
        headers=headers
    )

    response = client.post(
        "/users/",
        json={
            "username": "subject2",
            "name": "Sub",
            "surnames": "Two",
            "email": "subject2@gmail.com",
            "password": "123456",
            "userType": "SUBJECT"
        },
        headers=headers
    )

    assert response.status_code == 400

def test_create_supervisor_for_subject(client):
    headers = auth_header(client)

    subject = client.post(
        "/users/",
        json={
            "username": "subject1",
            "name": "Sub",
            "surnames": "One",
            "email": "subject1@gmail.com",
            "password": "123456",
            "userType": "SUBJECT",
        },
        headers=headers
    )

    print("Status:", subject.status_code)
    print("Response:", subject.json()) 

    subject = subject.json()

    response = client.post(
        "/users/",
        json={
            "username": "sup1",
            "name": "Sup",
            "surnames": "One",
            "email": "sup1@gmail.com",
            "password": "123456",
            "userType": "SUPERVISOR",
            "subjectUsername": "subject1"
        },
        headers=headers
    )

    print("Status:", response.status_code)
    print("Response:", response.json()) 

    assert response.status_code == 200
    assert response.json()["homeId"] == subject["homeId"]

def test_update_user_forbidden_outside_home(client):
    headers = auth_header(client)

    subject = client.post(
        "/users/",
        json={
            "username": "subject1",
            "name": "Sub",
            "surnames": "One",
            "email": "subject1@gmail.com",
            "password": "123456",
            "userType": "SUBJECT"
        },
        headers=headers
    ).json()

    creator2 = client.post(
        "/users/",
        json={
            "username": "creator2",
            "name": "Creator",
            "surnames": "Two",
            "email": "creator2@gmail.com",
            "password": "123456",
            "userType": "SUPERVISOR_CREATOR",
            "homeName": "CasaDos"
        },
        headers=headers
    ).json()

    login2 = client.post(
        "/auth/login",
        json={"email": "creator2@gmail.com", "password": "123456"}
    ).json()

    headers2 = {"Authorization": f"Bearer {login2['access_token']}"}

    response = client.put(
        f"/users/{subject['id']}",
        json={"name": "Hacked"},
        headers=headers2
    )

    assert response.status_code == 403

def test_delete_user_only_creator_can_delete(client):
    headers = auth_header(client)

    subject = client.post(
        "/users/",
        json={
            "username": "subject1",
            "name": "Sub",
            "surnames": "One",
            "email": "subject1@gmail.com",
            "password": "123456",
            "userType": "SUBJECT"
        },
        headers=headers
    ).json()

    response = client.delete(f"/users/{subject['id']}", headers=headers)

    assert response.status_code == 400

def test_delete_supervisor_valid(client):
    headers = auth_header(client)

    subject = client.post(
        "/users/",
        json={
            "username": "subject1",
            "name": "Sub",
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

    supervisor = client.post(
        "/users/",
        json={
            "username": "sup1",
            "name": "Sup",
            "surnames": "One",
            "email": "sup1@gmail.com",
            "password": "123456",
            "userType": "SUPERVISOR",
            "subjectUsername": "subject1"
        },
        headers=headers
    )
    
    print("SUPERVISOR STATUS:", supervisor.status_code)
    print("SUPERVISOR RESPONSE:", supervisor.json())
    
    supervisor = supervisor.json()

    response = client.delete(f"/users/{supervisor['id']}", headers=headers)
    
    print("DELETE STATUS:", response.status_code)
    print("DELETE RESPONSE:", response.json())

    assert response.status_code == 200