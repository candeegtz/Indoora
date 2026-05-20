import paho.mqtt.client as mqtt
import json
import pandas as pd
import threading
import joblib
from datetime import datetime
import os
from dotenv import load_dotenv
from collections import deque
import requests

# ==================== CARGAR CONFIGURACIÓN ====================
load_dotenv()
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
CONFIG_FILE = os.path.join(BASE_DIR, 'config.json')
if not os.path.exists(CONFIG_FILE):
    print(f"ERROR: No se encuentra {CONFIG_FILE}. Debe existir en el mismo directorio.")
    exit(1)

with open(CONFIG_FILE, 'r') as f:
    cfg = json.load(f)

MQTT_BROKER = cfg.get("mqtt_broker", "192.168.0.18")
MQTT_PORT = cfg.get("mqtt_port", 1883)
MQTT_USER = cfg.get("mqtt_user", "")
MQTT_PASSWORD = cfg.get("mqtt_password", "")
MQTT_TOPIC = cfg.get("mqtt_topic_receivers", "receivers/#")
TOPIC_CONFIG = cfg.get("topic_config", "motor/config")

BACKEND_URL = cfg.get("backend_url", "http://localhost:8000")
STABLE_ENDPOINT = f"{BACKEND_URL}/positioning/stable"

API_KEY = os.getenv("MOTOR_API_KEY")  # No uses cfg.get("api_key", "")
if not API_KEY:
    print("ERROR: MOTOR_API_KEY no definida en el entorno. Saliendo.")
    exit(1)

MODELS_DIR = cfg.get("models_dir", "src/logs")
OUTPUT_CSV = cfg.get("output_csv", "src/logs/predicciones_xgboost.csv")
STABLE_WINDOW_SIZE = cfg.get("stable_window_size", 5)
TIMEOUT_SECONDS = cfg.get("timeout_seconds", 3)      # Timeout para recolección de datos
DEVIATION_TIMEOUT_SECONDS = cfg.get("deviation_timeout_seconds", 30 * 60)  # 30 minutos por defecto
UMBRAL_CONFIANZA_HABITACION = cfg.get("umbral_confianza_habitacion", 0.40)
UMBRAL_CONFIANZA_POSICION = cfg.get("umbral_confianza_posicion", 0.00)

# Construir rutas de modelos
MODEL_HABITACION_PATH = os.path.join(MODELS_DIR, 'xgboost_habitacion_model.pkl')
ENCODER_HABITACION_PATH = os.path.join(MODELS_DIR, 'xgboost_label_encoder_habitacion.pkl')
MODEL_POSICION_PATH = os.path.join(MODELS_DIR, 'xgboost_posicion_model.pkl')
ENCODER_POSICION_PATH = os.path.join(MODELS_DIR, 'xgboost_label_encoder_posicion.pkl')

# ==================== MODELOS ====================
print("Cargando modelos...")
model_habitacion = joblib.load(MODEL_HABITACION_PATH)
label_encoder_habitacion = joblib.load(ENCODER_HABITACION_PATH)
model_posicion = joblib.load(MODEL_POSICION_PATH)
label_encoder_posicion = joblib.load(ENCODER_POSICION_PATH)
print("Modelos cargados correctamente.")

# ==================== CONFIGURACIÓN DINÁMICA ====================
posiciones_por_habitacion = {}   # Se llenará vía MQTT
HOME_ID = None                  # Se recibirá vía MQTT

# ==================== ESTRUCTURAS RSSI ====================
esp32_ids = {f'receivers/{i}': f'ESP32_{i}' for i in range(1, 11)}
all_esp32_ids = list(esp32_ids.values())
current_row = None
row_lock = threading.RLock()
timeout_thread = None

# ==================== ESTABILIZACIÓN ====================
position_window = deque(maxlen=STABLE_WINDOW_SIZE)
last_stable = None

# ==================== TEMPORIZADOR DE DESVIACIÓN (NUEVO) ====================
deviation_start_time = None
last_incorrect_position = None
deviation_timer = None
deviation_timer_lock = threading.Lock()

def send_deviation_alert(room, position):
    """Envía la alerta de desviación al backend tras el timeout."""
    global deviation_timer, deviation_start_time, last_incorrect_position
    if HOME_ID is None:
        return
    with deviation_timer_lock:
        # Solo enviar si sigue activa la misma desviación
        if deviation_start_time is None:
            return
        elapsed = (datetime.now() - deviation_start_time).total_seconds()
        if elapsed < DEVIATION_TIMEOUT_SECONDS:
            return   # No ha pasado suficiente tiempo (por si el timer se activó antes)
        payload = {
            "home_id": HOME_ID,
            "room": room,
            "position": position,
            "timestamp": datetime.now().isoformat(),
            "deviation_timer": True
        }
        headers = {"Content-Type": "application/json"}
        if API_KEY:
            headers["MOTOR-API-Key"] = API_KEY
        try:
            response = requests.post(STABLE_ENDPOINT, json=payload, headers=headers, timeout=3)
            if response.status_code == 200:
                print(f"ALERTA ENVIADA: desviación superó {DEVIATION_TIMEOUT_SECONDS/60} minutos en {room} - {position}")
                # Limpiar para no repetir
                deviation_start_time = None
                last_incorrect_position = None
                deviation_timer = None
            else:
                print(f"Error al enviar alerta: {response.status_code} - {response.text}")
        except Exception as e:
            print(f"Fallo al enviar alerta: {e}")

def get_stable_position():
    if len(position_window) == STABLE_WINDOW_SIZE:
        from collections import Counter
        return Counter(position_window).most_common(1)[0][0]
    return None

# ==================== MANEJO DE CONFIGURACIÓN MQTT ====================
def handle_config(payload):
    global posiciones_por_habitacion, HOME_ID
    try:
        data = json.loads(payload)
        if isinstance(data, str):
            data = json.loads(data)
        if "home_id" in data:
            HOME_ID = data["home_id"]
            print(f"Home ID recibido: {HOME_ID}")
        if "rooms_positions" in data:
            posiciones_por_habitacion = data["rooms_positions"]
        else:
            posiciones_por_habitacion = data
        print("Configuración actualizada:")
        for room, pos_list in posiciones_por_habitacion.items():
            print(f"   {room}: {pos_list}")
    except Exception as e:
        print(f"Error al procesar configuración: {e}")

# ==================== ENVÍO AL BACKEND (MODIFICADO CON TEMPORIZADOR) ====================
def send_to_backend(room, position):
    global deviation_start_time, last_incorrect_position, deviation_timer, HOME_ID
    if HOME_ID is None:
        print("Aún no se ha recibido home_id. No se envía posición.")
        return

    payload = {
        "home_id": HOME_ID,
        "room": room,
        "position": position,
        "timestamp": datetime.now().isoformat(),
        "deviation_timer": False
    }

    headers = {"Content-Type": "application/json"}
    if API_KEY:
        headers["X-API-Key"] = API_KEY

    try:
        response = requests.post(STABLE_ENDPOINT, json=payload, headers=headers, timeout=3)
        if response.status_code == 200:
            data = response.json()
            is_expected = data.get("is_expected", False)

            with deviation_timer_lock:
                if is_expected:
                    # Posición correcta → cancelar cualquier temporizador
                    if deviation_start_time is not None:
                        print("Posición correcta. Cancelando temporizador de desviación.")
                        if deviation_timer:
                            deviation_timer.cancel()
                            deviation_timer = None
                        deviation_start_time = None
                        last_incorrect_position = None
                else:
                    # Posición incorrecta
                    expected_room = data.get("expected_room")
                    expected_pos = data.get("expected_position")
                    current_incorrect = (room, position)

                    if deviation_start_time is None:
                        # Inicio de nueva desviación
                        deviation_start_time = datetime.now()
                        last_incorrect_position = current_incorrect
                        print(f"Inicio de desviación. Se esperaba {expected_room} - {expected_pos}")
                        if deviation_timer:
                            deviation_timer.cancel()
                        deviation_timer = threading.Timer(DEVIATION_TIMEOUT_SECONDS, send_deviation_alert, args=(room, position))
                        deviation_timer.start()
                    elif last_incorrect_position != current_incorrect:
                        # Cambio a otra posición incorrecta → reiniciar temporizador
                        print(f"Posición incorrecta cambió de {last_incorrect_position} a {current_incorrect}. Reiniciando temporizador.")
                        deviation_start_time = datetime.now()
                        last_incorrect_position = current_incorrect
                        if deviation_timer:
                            deviation_timer.cancel()
                        deviation_timer = threading.Timer(DEVIATION_TIMEOUT_SECONDS, send_deviation_alert, args=(room, position))
                        deviation_timer.start()
                    # Si es la misma posición incorrecta, no hacemos nada (el temporizador sigue corriendo)

            print(f"Enviado a backend: {room} - {position}")
        else:
            print(f"Error en backend: {response.status_code} - {response.text}")
    except Exception as e:
        print(f"Fallo al conectar con backend: {e}")

# ==================== CALLBACKS MQTT ====================
def on_connect(client, userdata, flags, rc):
    if rc == 0:
        print("Conectado al broker MQTT")
        client.subscribe(MQTT_TOPIC)
        client.subscribe(TOPIC_CONFIG)
    else:
        print(f"Error al conectar, código: {rc}")

def on_message(client, userdata, msg):
    global current_row, timeout_thread
    topic = msg.topic
    payload = msg.payload.decode()

    if topic == TOPIC_CONFIG:
        handle_config(payload)
        return

    try:
        data = json.loads(payload)
        esp32_id = esp32_ids.get(topic)
        if not esp32_id:
            return

        rssi = int(data.get('rssi', -150))

        with row_lock:
            if current_row is None:
                current_row = {esp_id: -150 for esp_id in all_esp32_ids}
                current_row['time'] = datetime.now().strftime('%d/%m/%Y %H:%M:%S')
                print("Nueva fila creada")
                timeout_thread = threading.Timer(TIMEOUT_SECONDS, predict_position)
                timeout_thread.start()

            current_row[esp32_id] = rssi

            if all(v != -150 for k, v in current_row.items() if k != 'time'):
                print("Todos los datos recibidos antes del timeout.")
                predict_position()

    except Exception as e:
        print(f"Error al procesar mensaje RSSI: {e}")

# ==================== PREDICCIÓN Y ESTABILIZACIÓN ====================
def predict_position():
    global current_row, timeout_thread, last_stable, position_window

    with row_lock:
        if current_row is None:
            return

        df = pd.DataFrame([current_row])
        try:
            feature_columns = [col for col in df.columns if col.startswith('ESP32_')]
            X = df[feature_columns]

            # Verifica que no está fuera de la vivienda
            if all(X.iloc[0][col] == -150 for col in feature_columns):
                predicted_habitacion_label = "Fuera de la vivienda"
                predicted_posicion_label = "Fuera de la vivienda"
            else:
                # Predicción habitación con probabilidades
                prediction_habitacion_proba = model_habitacion.predict_proba(X)
                max_proba_hab = prediction_habitacion_proba.max(axis=1)[0]

                if max_proba_hab >= UMBRAL_CONFIANZA_HABITACION:
                    prediction_habitacion = model_habitacion.predict(X)
                    predicted_habitacion_label = label_encoder_habitacion.inverse_transform(prediction_habitacion)[0]
                else:
                    predicted_habitacion_label = "Duda"

                # Predicción posición con probabilidades
                prediction_posicion_proba = model_posicion.predict_proba(X)
                max_proba_pos = prediction_posicion_proba.max(axis=1)[0]

                if max_proba_pos >= UMBRAL_CONFIANZA_POSICION:
                    prediction_posicion = model_posicion.predict(X)
                    predicted_posicion_label = label_encoder_posicion.inverse_transform(prediction_posicion)[0]
                else:
                    predicted_posicion_label = "Duda"

                # Fuerza la posición a "Duda" si la habitación es "Duda"
                if predicted_habitacion_label == "Duda":
                    predicted_posicion_label = "Duda"
                else:
                    # Si la habitación no es duda, verificamos coherencia
                    posiciones_validas = posiciones_por_habitacion.get(predicted_habitacion_label, [])
                    if predicted_posicion_label not in posiciones_validas and predicted_posicion_label != "Duda":
                        predicted_posicion_label = "Duda"

            # Añadir las predicciones al DataFrame
            df['habitacion_predicha'] = predicted_habitacion_label
            df['posicion_predicha'] = predicted_posicion_label

            # Mostrar las predicciones
            timestamp = df['time'].iloc[0]
            print(f"{timestamp} - Habitación predicha: {predicted_habitacion_label}, Posición predicha: {predicted_posicion_label}")

            # Guardar la fila en el archivo CSV
            if not os.path.isfile(OUTPUT_CSV):
                df.to_csv(OUTPUT_CSV, index=False, mode='w')
            else:
                df.to_csv(OUTPUT_CSV, index=False, mode='a', header=False)

            # Estabilización y envío al backend
            if predicted_habitacion_label not in ["Duda", "Fuera de la vivienda"] and predicted_posicion_label != "Duda":
                position_window.append((predicted_habitacion_label, predicted_posicion_label))
                stable = get_stable_position()
                if stable and stable != last_stable:
                    last_stable = stable
                    room, pos = stable
                    send_to_backend(room, pos)

        except Exception as e:
                print("Error al realizar la predicción:", e)

        current_row = None
        if timeout_thread:
            timeout_thread.cancel()
            timeout_thread = None

# ==================== INICIO ====================
client = mqtt.Client()
if MQTT_USER:
    client.username_pw_set(MQTT_USER, MQTT_PASSWORD)
client.on_connect = on_connect
client.on_message = on_message

try:
    client.connect(MQTT_BROKER, MQTT_PORT, 60)
except Exception as e:
    print(f"Error conectando al broker MQTT: {e}")
    exit(1)

print("Motor de predicción iniciado. Esperando datos ESP32...")
try:
    client.loop_forever()
except KeyboardInterrupt:
    print("\n Programa detenido por el usuario. Cerrando...")
    client.disconnect()
    # Cancelar temporizador si está activo
    if deviation_timer:
        deviation_timer.cancel()