package com.indoora.app.feature.deviceconfig

data class ConfigStep(
    val stepNumber: Int,
    val title: String,
    val description: String,
    val details: List<String> = emptyList(),
    val imageRes: Int? = null  // Opcional: para añadir imágenes más adelante
)

object DeviceConfigSteps {
    val steps = listOf(
        ConfigStep(
            stepNumber = 1,
            title = "Configuración de dispositivos",
            description = "Recibirás un email con todo lo necesario:",
            details = listOf(
                "Archivos .ino personalizados para tus ESP32",
                "Enlaces de descarga (Arduino IDE, Docker, Drivers)",
                "Instrucciones paso a paso detalladas",
                "Tu configuración WiFi y servidor MQTT"
            )
        ),
        ConfigStep(
            stepNumber = 2,
            title = "Revisa tu email",
            description = "Abre el email que te hemos enviado:",
            details = listOf(
                "Asunto: 'Configuración de tus dispositivos Indoora'",
                "Descarga todos los archivos adjuntos (.ino)",
                "Haz click en los enlaces para descargar el software",
                "Si no aparece, revisa la carpeta de spam"
            )
        ),
        ConfigStep(
            stepNumber = 3,
            title = "Instala el software necesario",
            description = "Sigue los enlaces del email:",
            details = listOf(
                "Arduino IDE (para programar las ESP32)",
                "Docker Desktop (para el servidor MQTT)",
                "Drivers USB-Serial (solo si no reconoce el puerto)",
                "Todo está explicado en el email con enlaces directos"
            )
        ),
        ConfigStep(
            stepNumber = 4,
            title = "Flashea tus dispositivos",
            description = "Sigue las instrucciones del email:",
            details = listOf(
                "Abre cada archivo .ino en Arduino IDE",
                "Conecta la ESP32 por USB",
                "Selecciona la placa y puerto correcto",
                "Haz click en Subir (→)",
                "Repite para cada dispositivo"
            )
        ),
        ConfigStep(
            stepNumber = 5,
            title = "Inicia el servidor MQTT",
            description = "Ejecuta el comando que aparece en el email:",
            details = listOf(
                "Abre Docker Desktop",
                "Abre una terminal (CMD o PowerShell)",
                "Copia y pega el comando del email",
                "Verifica que Mosquitto está corriendo"
            )
        ),
        ConfigStep(
            stepNumber = 6,
            title = "Vincular dispositivos en la app",
            description = "Registra tus dispositivos:",
            details = listOf(
                "En la app, ve a la sección Dispositivos",
                "Añade cada dispositivo con su MAC address",
                "Asigna cada receptor a su habitación",
                "Verifica que aparecen como 'Conectados'"
            )
        ),
        ConfigStep(
            stepNumber = 7,
            title = "¡Configuración completada!",
            description = "Todo listo para usar Indoora:",
            details = listOf(
                "Dispositivos configurados y conectados",
                "Servidor MQTT funcionando",
                "Ahora puedes entrenar el sistema",
                "Revisa el email si necesitas ayuda"
            )
        )
    )
}