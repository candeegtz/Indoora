package com.indoora.app.network.mqtt

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence

class MqttManager(
    private val serverUri: String,
    private val clientId: String,
) {
    private lateinit var mqttClient: MqttAsyncClient
    private val _messages = Channel<MqttMessagePayload>(Channel.UNLIMITED)
    val messages = _messages.receiveAsFlow()

    data class MqttMessagePayload(
        val topic: String,
        val message: String,
    )

    fun connect(username: String = "", password: String = "") {
        mqttClient = MqttAsyncClient(serverUri, clientId, MemoryPersistence())
        val options = MqttConnectOptions().apply {
            isCleanSession = true
            connectionTimeout = 10
            keepAliveInterval = 20
            if (username.isNotBlank()) {
                userName = username
                this.password = password.toCharArray()
            }
        }

        mqttClient.setCallback(object : MqttCallback {
            override fun connectionLost(cause: Throwable?) {
                // Opcional: reintentar conexión
            }

            override fun messageArrived(topic: String, message: MqttMessage) {
                val payload = String(message.payload)
                CoroutineScope(Dispatchers.IO).launch {
                    _messages.send(MqttMessagePayload(topic, payload))
                }
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {
                // No necesario para este caso
            }
        })

        mqttClient.connect(options, null, object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken?) {
                // Conexión exitosa
            }
            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                // Error de conexión
            }
        })
    }

    fun subscribe(topic: String, qos: Int = 1) {
        if (::mqttClient.isInitialized && mqttClient.isConnected) {
            mqttClient.subscribe(topic, qos)
        }
    }

    fun publish(topic: String, payload: String, qos: Int = 1, retained: Boolean = false) {
        if (::mqttClient.isInitialized && mqttClient.isConnected) {
            val message = MqttMessage(payload.toByteArray()).apply {
                this.qos = qos
                isRetained = retained
            }
            mqttClient.publish(topic, message)
        }
    }

    fun disconnect() {
        if (::mqttClient.isInitialized && mqttClient.isConnected) {
            mqttClient.disconnect()
        }
    }
}