package com.indoora.app.feature.deviceconfig

data class ConfigStep(
    val stepNumber: Int,
    val title: String,
    val description: String,
    val details: List<String> = emptyList(),
    val imageRes: Int? = null  // Por si se quiere añadir alguna imagen descriptiva
)

object DeviceConfigSteps {
    val steps = listOf(
        ConfigStep(
            stepNumber = 1,
            title = "Puesta en marcha del servidor",
            description = "El técnico se encargará de inicializar el centro de comunicaciones del hogar. Esto permite que el sistema funcione de forma privada y segura.",
            details = listOf(
                "Activación de la red interna del sistema",
                "Preparación del entorno para recibir datos en tiempo real"
            )
        ),
        ConfigStep(
            stepNumber = 2,
            title = "Instalación de sensores y pulsera",
            description = "El instalador colocará los sensores en puntos clave de la casa y los sincronizará con la pulsera inteligente del usuario monitorizado.",
            details = listOf(
                "Conexión de los equipos a la red Wi-Fi del hogar",
                "Vinculación segura y única de la pulsera de actividad",
                "Comprobación de cobertura en las habitaciones"
            )
        ),
        ConfigStep(
            stepNumber = 3,
            title = "¡Instalación técnica completada!",
            description = "Una vez que el técnico te indique que ha terminado la instalación física, confirma este paso para comenzar a enseñarle tu hogar al sistema.",
            details = listOf(
                "Sensores instalados y sincronizados",
                "Red de comunicaciones operativa",
                "Sistema desbloqueado y listo para iniciar el entrenamiento"
            )
        )
    )
}