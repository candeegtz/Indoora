@echo off
title Indoora - Motor de posicionamiento (prediccion.py)
echo ========================================
echo  Iniciando motor de posicionamiento
echo ========================================
echo.

REM Cambiar al directorio raíz del motor
cd /d "%~dp0\..\.." || (
    echo Error: No se pudo cambiar al directorio motor_indoor.
    pause
    exit /b 1
)

echo [1/3] Activando entorno virtual...
if not exist ".venv\Scripts\python.exe" (
    echo ERROR: No se encuentra el entorno virtual.
    echo Ejecuta primero runner.bat o setup_env.bat para crearlo.
    pause
    exit /b 1
)
call .venv\Scripts\activate

echo [2/3] Verificando dependencias...
pip show paho-mqtt >nul 2>&1
if errorlevel 1 (
    echo Instalando dependencias necesarias...
    pip install -r src\requirements.txt --quiet
)

echo [3/3] Lanzando motor de prediccion...
echo    (Presiona Ctrl+C para detener)
echo.
.venv\Scripts\python src\prediccion.py

pause