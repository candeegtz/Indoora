@echo off
title Eliminar datos de entrenamiento

set "ARCHIVO=..\logs\datosparaEntrenar.csv"

echo ========================================
echo  Eliminando archivo de entrenamiento
echo ========================================
echo.

if exist "%ARCHIVO%" (
    del /f /q "%ARCHIVO%"
    echo [OK] Archivo eliminado: %ARCHIVO%
) else (
    echo [ADVERTENCIA] El archivo no existe: %ARCHIVO%
)

echo.
echo Proceso completado.
timeout /t 3 >nul
exit /b