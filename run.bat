@echo off
echo ========================================
echo   Iniciando Indoora Backend
echo ========================================
echo.

REM Obtener la ruta actual (RAÍZ del proyecto = indoora-cbd/)
set PROJECT_ROOT=%CD%

REM Verificar si el entorno virtual existe (en la RAÍZ)
if not exist "%PROJECT_ROOT%\venv\" (
    echo ERROR: El entorno virtual no existe.
    echo Ejecuta primero setup.bat para configurar el proyecto.
    pause
    exit /b 1
)

REM Verificar si el archivo .env existe
if not exist "%PROJECT_ROOT%\.env" (
    echo ADVERTENCIA: No se encuentra el archivo .env
    echo Ejecuta setup.bat para crearlo o crealo manualmente.
    echo.
)

REM Activar entorno virtual (desde la RAÍZ)
echo Activando entorno virtual...
call "%PROJECT_ROOT%\venv\Scripts\activate.bat"

REM Entrar a la carpeta backend
echo Entrando a la carpeta backend...
cd "%PROJECT_ROOT%\backend"

echo.
echo Iniciando servidor FastAPI...
echo.
echo API disponible en: http://localhost:8000
echo Documentacion: http://localhost:8000/docs
echo.
echo Presiona CTRL+C para detener el servidor
echo.

uvicorn app.main:app --reload --host 0.0.0.0 --port 8000

REM Volver a la raíz al cerrar (opcional)
cd "%PROJECT_ROOT%"
pause