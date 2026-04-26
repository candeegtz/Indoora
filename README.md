# Indoora - Aplicación para sistema indoor

Indoora es una aplicación Android que permite gestionar entornos indoor (hogares, habitaciones, actividades y rutinas) con almacenamiento centralizado en PostgreSQL, un backend en FastAPI y comunicación mediante API REST.

---

## Tabla de contenidos

- [Requisitos previos](#requisitos-previos)
- [Instalación del backend](#instalación-del-backend)
- [Configuración de Android](#configuración-de-android)
- [Ejecución y pruebas](#ejecución-y-pruebas)
- [Solución de problemas](#solución-de-problemas)

---

## Requisitos previos

**Sistema operativo:** Windows (10, 11)

### Backend

| Requisito | Versión mínima | Comando para verificar | Descarga |
|-----------|----------------|------------------------|----------|
| Python | 3.8+ | `python --version` | [python.org](https://www.python.org/downloads/) |
| PostgreSQL | 14+ (Recomendada: 17) | `psql --version` | [postgresql.org](https://www.postgresql.org/download/) |

### Android

| Requisito | Versión | Descarga/Instalación |
|-----------|---------|---------------------|
| Android Studio | Hedgehog 2023.1.1+ | [developer.android.com](https://developer.android.com/studio) |
| JDK | 17 | Incluido en Android Studio
| Android SDK | API 36 (Android 15) | Se instala desde Android Studio SDK Manager |
| Gradle | 8.0+ | Incluido en el proyecto |

---

## Instalación del backend

### 1. Clonar el repositorio

```bash
git clone https://github.com/candeegtz/Indoora-CBD.git
cd Indoora-CBD
```

### 2. Configurar el backend

**cmd:**
```cmd
setup.bat
```
**Directorio de carpetas:**
```cmd
Ejecutar indoora-CBD/setup.bat
```

El script `setup` realiza automáticamente:

| Paso | Acción |
|------|--------|
| 1 | Verifica que Python esté instalado |
| 2 | Verifica que PostgreSQL esté instalado |
| 3 | Crea el entorno virtual `venv/` |
| 4 | Instala dependencias desde `requirements.txt` |
| 5 | Copia `.env.example` a `.env` |

### 3. Configurar la base de datos

#### 3.0. Instalación de PostgreSQL

Para que el backend funcione correctamente, es necesario tener un servidor PostgreSQL instalado y en ejecución. A continuación se detalla el proceso de instalación en Windows.

**Paso 1: Descargar el instalador**

1.  Accede a la página oficial de descargas de PostgreSQL: [https://www.postgresql.org/download/windows/](https://www.postgresql.org/download/windows/)
2.  Haz clic en el botón **"Download the installer"**. Esto te redirigirá a la web de EnterpriseDB (EDB), que proporciona el instalador oficial para Windows.
3.  Se recomienda descargar la versión **17.x** (o una versión estable reciente), que es la que se ha utilizado durante el desarrollo.

**Paso 2: Ejecutar el instalador**

1.  Localiza el archivo `.exe` descargado (por ejemplo, `postgresql-17.4-1-windows-x64.exe`) y ejecútalo con doble clic.
2.  Si el sistema te pide permisos de administrador, confírmalo para continuar.
3.  Aparecerá el asistente de instalación. Haz clic en **"Next"** para avanzar.

**Paso 3: Seleccionar componentes**

En la pantalla "Select Components", asegúrate de que los siguientes componentes estén marcados:

-   **PostgreSQL Server**: El motor de la base de datos (obligatorio).
-   **pgAdmin 4**: La herramienta gráfica de administración (opcional, pero muy recomendable).
-   **Command Line Tools**: Incluye la utilidad `psql` para usar desde la terminal (necesario).
-   **Stack Builder**: Este componente es opcional; puedes desmarcarlo para una instalación más rápida.

Haz clic en **"Next"** para continuar.

**Paso 4: Elegir el directorio de datos**

Selecciona la carpeta donde se almacenarán los datos de las bases de datos. Se puede dejar la ruta por defecto:
`C:\Program Files\PostgreSQL\17\data` o personalizarla si es necesario. Haz clic en **"Next"**.

**Paso 5: Configurar la contraseña del superusuario**

1.  En la pantalla "Password", introduce y confirma una **contraseña segura** para el usuario administrador `postgres`.
    > **Muy importante:**
    > Esta contraseña será necesaria más adelante para configurar el archivo `.env` del backend (`DATABASE_URL`). Anótala, ya que la necesitarás para conectar la aplicación y para operar con la base de datos.

2.  Haz clic en **"Next"**.

**Paso 6: Configurar el puerto**

Generalmente se puede dejar el puerto por defecto, **5432**, a menos que otro servicio lo esté utilizando. Acepta el valor predeterminado y haz clic en **"Next"**.

**Paso 7: Configurar la localización ("Locale")**

Selecciona la configuración regional (locale) para la base de datos. Puedes dejarla como `Default locale` o elegir la que corresponda a tu región (por ejemplo, `Spanish` o `English (United States)`). Haz clic en **"Next"**.

**Paso 8: Revisar la configuración**

El instalador mostrará un resumen de las opciones seleccionadas. Revísalas y haz clic en **"Next"** para comenzar la instalación.

**Paso 9: Finalizar la instalación**

Una vez completada la instalación, es posible que se ofrezca lanzar el "Stack Builder". Desmarca esa opción y haz clic en **"Finish"**. El servicio de PostgreSQL debería iniciarse automáticamente.

---

#### 3.1. Editar el archivo `.env`

Abre el archivo `.env.example` en la carpeta **backend** y configura tu conexión a PostgreSQL sustituyendo aquellos elementos en mayúscula:

```ini
DATABASE_URL=postgresql://USUARIO:TU_CONTRASEÑA@localhost:5432/indoora
SECRET_KEY=clave_secreta_indicada_en_la_memoria
```
> **Nota:** Si durante la instalación cambiaste el puerto por defecto (5432), asegúrate de reflejar el mismo puerto en la URL de `DATABASE_URL` (por ejemplo, `5433`).

A continuación, renombra el archivo a `.env`

#### 3.2. Crear la base de datos

**Windows SQL Shell :**

Abrir SQL Shell y pulsar ENTER hasta que pregunte por las credenciales (usuario y contraseña). Cuando se abra la sesión y te indique:
```cmd
postgres=#  
```
Introduce la siguiente instrucción
```cmd
CREATE DATABASE indoora;
\q
```

> **Nota:** Las tablas se crean automáticamente al ejecutar el backend por primera vez gracias a `create_db_and_tables()`.

### 4. Ejecutar el backend

**Windows:**
```cmd
run.bat
```
**Directorio de carpetas:**
```cmd
Ejecutar indoora-CBD/run.bat
```

Una vez iniciado, el servidor estará disponible en:

- **API:** http://localhost:8000
- **Documentación Swagger:** http://localhost:8000/docs
- **Documentación ReDoc:** http://localhost:8000/redoc

---

## Configuración de Android

### 1. Abrir el proyecto en Android Studio

1. Abre **Android Studio**
2. Selecciona **File → Open**
3. Navega a la carpeta `android/` dentro del repositorio clonado
4. Haz clic en **OK**

### 2. Configuración automática de dependencias

Al abrir el proyecto, Android Studio:

1. **Descargará automáticamente:**
   - Gradle Wrapper
   - Kotlin 2.0+
   - Jetpack Compose
   - Retrofit + Moshi
   - Coroutines
   - Navigation Compose

2. **Sincronizará el proyecto** (Gradle Sync)
   - Espera a que termine la sincronización (barra de progreso inferior)
   - Si aparecen errores, revisa la [sección de solución de problemas](#solución-de-problemas)

### 3. Configuración técnica del proyecto

El proyecto está configurado con:

```gradle
android {
    compileSdk = 36          // Android 15
    
    defaultConfig {
        minSdk = 24          // Android 7.0+
        targetSdk = 36       // Android 15
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    jvmToolchain(17)         // JDK 17
}
```

### 4. Verificar la configuración del JDK

1. Ve a **File → Project Structure** (o `Ctrl+Alt+Shift+S`)
2. En **SDK Location:**
   - **JDK location:** Debe apuntar a JDK 17
   - Si no está configurado, descárgalo desde el menú desplegable

### 5. Verificar el Android SDK

1. Ve a **Tools → SDK Manager**
2. En la pestaña **SDK Platforms:**
   - Marca **Android 15.0 (API 36)**
   - Marca **Android 13.0 (API 33)** (para compatibilidad)
3. En la pestaña **SDK Tools:**
   - Android SDK Build-Tools 36.0.0+
   - Android Emulator
   - Android SDK Platform-Tools
4. Haz clic en **Apply** para instalar

---

## Ejecución y pruebas

### 1. Configurar un emulador (si no tienes dispositivo físico)

1. Abre el **Device Manager** (icono de teléfono en la barra de herramientas)
2. Haz clic en **Create device**
3. Selecciona un modelo:
   - Recomendado: **Pixel 6** o **Pixel 5**
4. Selecciona una imagen del sistema:
   - **API 33 (Android 13.0)** o superior
   - Descarga la imagen si es necesario
5. Configura el emulador:
   - RAM: 2048 MB mínimo (4096 MB recomendado)
   - Gráficos: **Automatic** o **Hardware**
6. Haz clic en **Finish**

### 2. Ejecutar la aplicación

1. **Asegúrate de que el backend esté corriendo** (ver [paso 4](#4-ejecutar-el-backend))

2. **Selecciona el dispositivo:**
   - En Android Studio, usa el selector de dispositivos en la barra de herramientas
   - Elige tu emulador o dispositivo físico

3. **Ejecuta la app:**
   - Haz clic en el botón **Run ▶** (o presiona `Shift+F10`)
   - Espera a que compile y se instale

> **Nota importante:** El emulador usa la IP especial `10.0.2.2` para acceder a `localhost` de tu ordenador. La aplicación ya está configurada para usar `http://10.0.2.2:8000`.

### 3. Credenciales de prueba

El sistema incluye usuarios predefinidos para pruebas:

| Tipo de usuario | Usuario | Contraseña |
|----------------|---------|------------|
| Supervisor creador | `supervisor_prueba` | `supervisor123456` |
| Sujeto | `subject_prueba` | `subject123456` |

> **Importante:** Estos usuarios no tienen casas asociadas. Se recomienda **crear un usuario nuevo** desde la app para probar el flujo completo.

### 4. Flujo de registro

**Opción 1: Supervisor Creador**
1. Selecciona **"No tengo un hogar creado"**
2. Completa el formulario de registro
3. Proporciona un nombre para tu hogar
4. El sistema creará automáticamente el hogar y las habitaciones base

**Opción 2: Supervisor**
1. Selecciona **"Ya tengo un hogar creado"**
2. Completa el formulario de registro
3. Introduce el **nombre de usuario del sujeto** de las credenciales de prueba (identificador del hogar)
4. El sistema te vinculará al hogar existente

---
##  Detener el servidor

Pulsa `Ctrl + C` en la terminal donde se está ejecutando el backend.
---

## Ejecución de tests

Para ejecutar los tests del backend, estando en el directorio raíz del proyecto y con el entorno virtual activado, ejecuta:
```bash
venv\Scripts\activate
cd backend
pytest tests
pytest tests/test_device.py
deactivate
```
| Paso | Acción |
|------|--------|
| 1 | Activa el entorno virtual `venv/`|
| 2 | Se mueve a la carpeta backend |
| 3 | Ejecuta todos los tests |
| 4 | Ejecuta solo los tests de device (se puede cambiar por los otros archivos de tests) |
| 5 | Salir del entorno virtual `venv/` |


## Solución de problemas

### Backend

| Problema | Solución |
|----------|----------|
| Error de conexión a PostgreSQL | Verifica que el servicio de PostgreSQL esté en ejecución (abre `services.msc` y busca `postgresql-x64-17`). Comprueba también las credenciales en `.env`.|
| Puerto 8000 en uso | Cambia el puerto en `run.bat`/`run.sh`: `--port 8001` |
| Error al instalar dependencias | Actualiza pip: `pip install --upgrade pip` y vuelve a ejecutar `setup.bat`/`setup.sh` |
| `ModuleNotFoundError` | Asegúrate de activar el entorno virtual antes de ejecutar: `venv\Scripts\activate` (Windows). |

### Android

| Problema | Solución |
|----------|----------|
| Gradle sync falla | 1. Comprueba tu conexión a Internet<br>2. **File → Invalidate Caches → Invalidate and Restart**<br>3. Borra `.gradle/` y vuelve a sincronizar |
| JDK no encontrado | **File → Project Structure → SDK Location** → Selecciona JDK 17 |
| SDK 36 no disponible | **Tools → SDK Manager → SDK Platforms** → Marca Android 15.0 (API 36) → Apply |
| La app no conecta con el backend | 1. Verifica que el backend esté en ejecución (`http://localhost:8000/docs`)<br>2. Asegúrate de usar un **emulador** (no un dispositivo físico) ya que usa `10.0.2.2`<br>3. Si usas dispositivo físico, cambia la URL en `RetrofitClient.kt` a la IP local de tu ordenador |
| Error de compilación | 1. **Build → Clean Project**<br>2. **Build → Rebuild Project**<br>3. Verifica que todas las dependencias se descargaron correctamente |
| Emulador muy lento | 1. Habilita aceleración por hardware (HAXM en Intel, WHPX en AMD)<br>2. Asigna más RAM al emulador (4096 MB)<br>3. Usa una imagen de sistema sin Google Play para mejor rendimiento |

### Red (Emulador ↔ Backend)

| Escenario | URL del backend |
|-----------|-----------------|
| Emulador Android | `http://10.0.2.2:8000` |
| Dispositivo físico (misma WiFi) | `http://TU_IP_LOCAL:8000` (ej: `http://192.168.1.100:8000`) |
| Localhost (navegador) | `http://localhost:8000` |

**Para encontrar tu IP local:**
- **Windows:** `ipconfig` (busca "Dirección IPv4")

---