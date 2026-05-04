import os
import sys
import json
import threading
import time
from datetime import datetime
import pandas as pd
import paho.mqtt.client as mqtt

# ------------ CONFIGURACIÓN ------------
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
CONFIG_FILE = os.path.join(BASE_DIR, 'config.json')
LOGS_DIR = os.path.join(BASE_DIR, 'logs')
CSV_FILE = os.path.join(LOGS_DIR, 'datosparaEntrenar.csv')
os.makedirs(LOGS_DIR, exist_ok=True)

# Cargar configuración
with open(CONFIG_FILE, 'r') as f:
    cfg = json.load(f)

MQTT_BROKER = cfg.get("mqtt_broker", "localhost")
MQTT_PORT = cfg.get("mqtt_port", 1883)
MQTT_USER = cfg.get("mqtt_user", "")
MQTT_PASSWORD = cfg.get("mqtt_password", "")
TOPIC_PREFIX = cfg.get("training_topic_prefix", "training")
TOTAL_READINGS = cfg.get("total_readings_per_position", 10)

# Variables globales
sequence = []
seq_index = -1
current_room = None
current_position = None
rows_written = 0
training_active = False
waiting_confirmation = False

# IDs de ESP32 (hasta 10)       *Modificar si se necesitan más o menos*
esp32_ids = {f"receivers/{i}": f"ESP32_{i}" for i in range(1, 11)}
all_esp32_ids = list(esp32_ids.values())

# Variables para RSSI
current_row = None
row_lock = threading.RLock()
timeout_thread = None
TIMEOUT_SECONDS = 3.5

# Cliente MQTT
client = mqtt.Client()
if MQTT_USER:
    client.username_pw_set(MQTT_USER, MQTT_PASSWORD)

# ------------ FUNCIONES MQTT ------------
def publish_instruction(instruction_type, **kwargs):
    msg = {"type": instruction_type, "timestamp": time.time()}
    msg.update(kwargs)
    client.publish(f"{TOPIC_PREFIX}/instruction", json.dumps(msg))

def publish_progress(current, total):
    msg = {"type": "progress", "current": current, "total": total}
    client.publish(f"{TOPIC_PREFIX}/progress", json.dumps(msg))

def publish_complete():
    client.publish(f"{TOPIC_PREFIX}/complete", json.dumps({"type": "complete"}))

def stop_and_exit():
    print("Deteniendo MQTT y saliendo...")
    client.loop_stop()
    client.disconnect()
    os._exit(0)

# ------------ LÓGICA DE ENTRENAMIENTO ------------
def start_training_from_message(payload):
    global sequence, seq_index, training_active, waiting_confirmation
    try:
        data = json.loads(payload)
        seq_list = data.get("sequence", [])
        # Extraer secuencia de habitaciones y posiciones
        sequence = [(item["room"], item["position"]) for item in seq_list]
        if not sequence:
            print("Secuencia vacía. Ignorando inicio.")
            return
        seq_index = -1
        training_active = True
        waiting_confirmation = False
        print(f"Entrenamiento iniciado con {len(sequence)} posiciones, {TOTAL_READINGS} lecturas/posición")
        next_position()
    except Exception as e:
        print(f"Error al procesar mensaje start: {e}")

def next_position():
    global seq_index, current_room, current_position, rows_written, waiting_confirmation, training_active
    seq_index += 1
    # Si se ha completado la secuencia, finalizar entrenamiento
    if seq_index >= len(sequence):
        print("Entrenamiento completado. Saliendo...")
        training_active = False
        publish_complete()
        time.sleep(0.5)
        stop_and_exit()
    # Configurar la siguiente posición
    current_room, current_position = sequence[seq_index]
    rows_written = 0
    waiting_confirmation = True
    publish_instruction("move_to", room=current_room, position=current_position, readings_needed=TOTAL_READINGS)
    print(f"Esperando confirmación para: {current_room} → {current_position}")

def on_confirm():
    global waiting_confirmation
    # Solo aceptar confirmación si se esta esperando y el entrenamiento está activo
    if waiting_confirmation and training_active:
        waiting_confirmation = False
        print(f"Confirmado. Grabando {current_room} - {current_position} durante {TOTAL_READINGS} lecturas")

# ------------ MANEJO DE DATOS RSSI ------------
def write_row_to_csv():
    global current_row, timeout_thread, rows_written
    with row_lock:
        if current_row is None:
            return
        # Completar valores nulos con -150
        for e in all_esp32_ids:
            if current_row.get(e) is None:
                current_row[e] = -150

        # Crear DataFrame y guardar en CSV
        df = pd.DataFrame([current_row]).set_index('time')
        cols = all_esp32_ids + ['Habitacion', 'Posicion']
        df = df[cols]

        # Escribir con encabezado solo si el archivo no existe o está vacío
        header = not os.path.exists(CSV_FILE) or os.path.getsize(CSV_FILE) == 0
        df.to_csv(CSV_FILE, mode='a', header=header,
                  date_format='%d/%m/%Y %H:%M:%S', index_label='time')

        # Incrementar contador de filas escritas y mostrar progreso
        rows_written += 1
        print(f"[{rows_written}/{TOTAL_READINGS}] {current_room}/{current_position}")

        # Publicar progreso después de cada escritura
        publish_progress(rows_written, TOTAL_READINGS)

        # Limpiar fila actual
        current_row = None
        if timeout_thread:
            timeout_thread.cancel()
            timeout_thread = None

        # Si se han escrito todas las lecturas necesarias para esta posición, pasar a la siguiente
        if rows_written >= TOTAL_READINGS:
            next_position()

# ------------ CALLBACKS MQTT ------------
def on_connect(client, userdata, flags, rc):
    # Suscribirse a los temas necesarios al conectar
    if rc == 0:
        print("Conectado al broker MQTT")
        client.subscribe(f"{TOPIC_PREFIX}/start")
        client.subscribe(f"{TOPIC_PREFIX}/confirm")
        client.subscribe("receivers/#")
    else:
        print(f"Error de conexión MQTT: {rc}")

def on_message(client, userdata, msg):
    # Procesar mensajes entrantes
    global current_row, timeout_thread, training_active, waiting_confirmation
    topic = msg.topic
    payload = msg.payload.decode()

    # Comandos de control de entrenamiento
    if topic == f"{TOPIC_PREFIX}/start":
        start_training_from_message(payload)
        return
    elif topic == f"{TOPIC_PREFIX}/confirm":
        on_confirm()
        return

    # Si no estamos en entrenamiento o esperando confirmación, ignorar RSSI
    if not training_active or waiting_confirmation:
        return

    # Procesar mensajes de los ESP32
    try:
        data = json.loads(payload)
        esp = esp32_ids.get(topic)
        if not esp:
            return

        tstr = data.get('time')
        if not tstr:
            tstr = datetime.now().strftime('%d/%m/%Y %H:%M:%S')
        rssi = int(data.get('rssi', -150))
        t_idx = pd.to_datetime(tstr, format='%d/%m/%Y %H:%M:%S')

        with row_lock:
            if current_row is None:
                current_row = {
                    'time': t_idx,
                    'Habitacion': current_room,
                    'Posicion': current_position
                }
                for e in all_esp32_ids:
                    current_row[e] = None
                timeout_thread = threading.Timer(TIMEOUT_SECONDS, write_row_to_csv)
                timeout_thread.start()

            current_row[esp] = rssi
            # Si ya tenemos datos de todos los ESP32, escribir inmediatamente
            if all(current_row[e] is not None for e in all_esp32_ids):
                write_row_to_csv()
    except Exception as e:
        print(f"Error procesando mensaje RSSI: {e}")

# ------------ ARRANQUE ------------
client.on_connect = on_connect
client.on_message = on_message

try:
    client.connect(MQTT_BROKER, MQTT_PORT, 60)
    print(f"Conectando a MQTT en {MQTT_BROKER}:{MQTT_PORT}")
    client.loop_forever()
except KeyboardInterrupt:
    print("\nInterrupción manual. Cerrando...")
    client.disconnect()
    sys.exit(0)