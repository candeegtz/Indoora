@echo off
echo ========================================
echo  Indoora - Configuracion y Entrenamiento
echo ========================================
echo.

REM Cambiar al directorio raíz del proyecto (motor_indoor)
cd /d "%~dp0\..\.." || (
    echo Error: No se pudo cambiar al directorio raíz.
    pause
    exit /b 1
)

echo [1/3] Verificando / creando entorno virtual...
if not exist ".venv\Scripts\python.exe" (
    echo Creando entorno virtual...
    python -m venv .venv
    if errorlevel 1 (
        echo Error al crear el entorno virtual.
        pause
        exit /b 1
    )
    echo Entorno virtual creado.
) else (
    echo Entorno virtual ya existe.
)

echo Activando entorno virtual...
call .venv\Scripts\activate

echo Instalando dependencias desde src\requirements.txt...
pip install --upgrade pip
pip install -r src\requirements.txt
if errorlevel 1 (
    echo Error al instalar dependencias.
    pause
    exit /b 1
)

echo.
echo [2/3] Iniciando recogida de datos (save2.0.py)...
echo    Esperando a que la app Android envie la secuencia...
REM Ejecutar save2.0.py (se quedará en espera hasta que la app lo active)
call .venv\Scripts\python src\save2.0.py
if errorlevel 1 (
    echo Error durante la recogida de datos.
    pause
    exit /b 1
)

echo.
echo [3/3] Recogida completada. Entrenando modelo...
call .venv\Scripts\python src\xgboostmodel.py
if errorlevel 1 (
    echo Error durante el entrenamiento.
    pause
    exit /b 1
)

echo.
echo ========================================
echo ¡Proceso completado con exito!
echo El modelo ha sido entrenado y guardado.
echo ========================================
pause