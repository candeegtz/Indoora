# Indoora: Sistema de Localización Indoor y Gestión de Rutinas

Este proyecto implementa **Indoora**, un sistema integral de localización en interiores basado en dispositivos ESP32 y tecnología Bluetooth Low Energy (BLE), complementado con algoritmos de Machine Learning (XGBoost) para la predicción de la posición. A diferencia de un motor aislado, Indoora aporta una arquitectura cloud centralizada y una aplicación móvil para gestionar el entorno del hogar, entrenar el modelo de forma guiada, definir rutinas y generar alertas en tiempo real ante desviaciones en el comportamiento del usuario.

---

## 🎯 Objetivos

- Diseñar e implementar una aplicación Android que sirva como interfaz principal para la configuración del hogar, gestión de usuarios, rutinas y visualización de alertas.
- Implementar un proceso guiado desde la aplicación móvil para la recogida de muestras de señal BLE y el entrenamiento autónomo del modelo predictivo.
- Desarrollar un backend centralizado y robusto que gestione la lógica de negocio y el almacenamiento de datos.
- Adaptar e integrar un motor de posicionamiento para lograr una comunicación fluida bidireccional mediante MQTT y HTTP.
- Crear una solución de monitorización accesible, de bajo coste y que respete totalmente la privacidad del usuario (sin uso de cámaras o micrófonos).

---

## ⚙️ Tecnologías utilizadas

- **Hardware:** Microcontroladores ESP32, smartwatch BLE (emisor).
- **Red de comunicación:** Eclipse Mosquitto (MQTT) y API REST.
- **Machine Learning:** XGBoost, Scikit-learn, Pandas (Python).
- **Backend:** Python, FastAPI, SQLAlchemy.
- **Base de datos:** PostgreSQL (Supabase).
- **Frontend (App Móvil):** Android, Kotlin, Jetpack Compose, Arquitectura MVVM, Retrofit.
- **Despliegue e Infraestructura:** Docker, DigitalOcean App Platform, GitHub Actions.

---

## ⚠️ Alertas soportadas

- El usuario permanece en una ubicación incorrecta (desviación de la actividad programada en su rutina) durante un tiempo superior al margen de tolerancia establecido (ej. 30 minutos).
- No se detecta señal del smartwatch por parte de los receptores ESP32 (el usuario sale de la vivienda o hay pérdida total de señal).

*Las alertas se notifican de forma inmediata y simultánea a través de correos electrónicos y notificaciones integradas en la aplicación móvil de los supervisores.*

## 🧪 Casos de uso

- Supervisión, monitorización y asistencia a personas mayores o en situación de dependencia en su propio hogar, garantizando su seguridad y la tranquilidad de sus familiares o tutores.

---

## 👨‍💻 Autores

- **Autora y desarrolladora:** Candela Jazmín Gutiérrez González
- **Directora:** María Teresa Gómez López
- **Mención:** El motor de posicionamiento base es una adaptación integrada a partir del proyecto original de Francisco Antonio Campos Campos.
