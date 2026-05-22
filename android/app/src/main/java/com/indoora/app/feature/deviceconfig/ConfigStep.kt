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
            title = "'Mosquitto' en funcionamiento",
            description = "Entrar en docker desktop y encender el contenedor de la imagen 'mosquitto'.",
        ),
        ConfigStep(
            stepNumber = 2,
            title = "Configura los dispositivos ESP32",
            description = "Accede a 'Arduino IDE' y compila el código correspondiente para cada dispositivo. Pero antes, ten en cuenta:",
            details = listOf(
                "Debes de modificar la dirección MAC de la pulsera BLE",
                "Indica el nombre y la contraseña del wifi del hogar",
                "Adapta el nombre y ruta '/receivers' para cada dispositivo"
            )
        ),
        ConfigStep(
            stepNumber = 3,
            title = "¡Configuración de dispositivos completada!",
            description = "Todo listo para seguir con el entrenamiento del sistema",
            details = listOf(
                "Dispositivos configurados y conectados",
                "Servidor MQTT funcionando",
                "Ahora puedes entrenar el sistema"
            )
        )
    )
}