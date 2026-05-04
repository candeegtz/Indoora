@echo off
echo ========================================
echo   Indoora Backend - Setup Automático
echo ========================================
echo.

REM Obtener la ruta actual (RAÍZ del proyecto = indoora-cbd/)
set PROJECT_ROOT=%CD%

REM Verificar si Python está instalado
echo [1/5] Verificando Python...
python --version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Python no esta instalado.
    echo Por favor, descarga Python desde https://www.python.org/downloads/
    pause
    exit /b 1
)
echo OK - Python encontrado
echo.

REM Verificar si PostgreSQL está instalado
echo [2/5] Verificando PostgreSQL...
psql --version >nul 2>&1
if errorlevel 1 (
    echo ADVERTENCIA: PostgreSQL no encontrado.
    echo Por favor, descarga PostgreSQL desde https://www.postgresql.org/download/
    echo.
)
echo OK - PostgreSQL verificado
echo.

REM Crear entorno virtual en la RAÍZ (indoora-cbd/venv)
echo [3/5] Creando entorno virtual en %PROJECT_ROOT%\venv...
if exist "%PROJECT_ROOT%\venv" (
    echo El entorno virtual ya existe. Eliminando...
    rmdir /s /q "%PROJECT_ROOT%\venv"
)
python -m venv "%PROJECT_ROOT%\venv"
echo OK - Entorno virtual creado en %PROJECT_ROOT%\venv
echo.

REM Activar entorno virtual e instalar dependencias
echo [4/5] Instalando dependencias desde %PROJECT_ROOT%\requirements.txt...
call "%PROJECT_ROOT%\venv\Scripts\activate.bat"
pip install --upgrade pip
pip install -r "%PROJECT_ROOT%\requirements.txt"
echo OK - Dependencias instaladas
echo.

REM Configurar archivo .env (si no existe)
echo [5/5] Configurando archivo .env...
if not exist "%PROJECT_ROOT%\.env" (
    echo Creando archivo .env en %PROJECT_ROOT%
    (
        echo # PostgreSQL Database URL
        echo DATABASE_URL=postgresql://postgres:tu_contraseña@localhost:5432/indoora
    ) > "%PROJECT_ROOT%\.env"
    echo OK - Archivo .env creado
    echo.
    echo ========================================
    echo   IMPORTANTE: Configurar .env
    echo ========================================
    echo.
    echo Edita el archivo .env en la RAÍZ del proyecto y cambia:
    echo   postgres:tu_contraseña por tu usuario y contraseña real
    echo.
) else (
    echo OK - Archivo .env ya existe
)
echo.

echo ========================================
echo   ¡Configuracion completada!
echo ========================================
echo.
echo Para iniciar el backend, ejecuta: run.bat
echo.
pause