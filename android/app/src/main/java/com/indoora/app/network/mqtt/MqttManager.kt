package com.indoora.app.network.mqtt

import android.util.Log
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

    private var onConnectedCallback: (() -> Unit)? = null
    private var _isConnected = false
    val isConnected: Boolean get() = _isConnected

    data class MqttMessagePayload(
        val topic: String,
        val message: String,
    )

    fun connect(username: String = "", password: String = "", onConnected: (() -> Unit)? = null) {
        this.onConnectedCallback = onConnected
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
                _isConnected = false
                Log.d("MqttManager", "Conexión perdida")
            }

            override fun messageArrived(topic: String, message: MqttMessage) {
                val payload = String(message.payload)
                CoroutineScope(Dispatchers.IO).launch {
                    _messages.send(MqttMessagePayload(topic, payload))
                }
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {}
        })

        mqttClient.connect(options, null, object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken?) {
                _isConnected = true
                Log.d("MqttManager", "Conectado a $serverUri")
                onConnectedCallback?.invoke()
            }
            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                Log.e("MqttManager", "Error de conexión: ${exception?.message}")
            }
        })
    }

    fun subscribe(topic: String, qos: Int = 1) {
        if (::mqttClient.isInitialized && _isConnected) {
            mqttClient.subscribe(topic, qos)
            Log.d("MqttManager", "Suscrito a $topic")
        } else {
            Log.w("MqttManager", "No conectado, no se pudo suscribir a $topic")
        }
    }

    fun unsubscribe(topic: String) {
        if (::mqttClient.isInitialized && _isConnected) {
            mqttClient.unsubscribe(topic)
            Log.d("MqttManager", "Desuscrito de $topic")
        } else {
            Log.w("MqttManager", "No conectado, no se pudo desuscribir de $topic")
        }
    }

    fun publish(topic: String, payload: String, qos: Int = 1, retained: Boolean = false) {
        if (::mqttClient.isInitialized && _isConnected) {
            val message = MqttMessage(payload.toByteArray()).apply {
                this.qos = qos
                isRetained = retained
            }
            mqttClient.publish(topic, message)
            Log.d("MqttManager", "Publicado en $topic: $payload")
        } else {
            Log.w("MqttManager", "No conectado, no se pudo publicar en $topic")
        }
    }

    fun disconnect() {
        if (::mqttClient.isInitialized && _isConnected) {
            mqttClient.disconnect()
        }
    }
}