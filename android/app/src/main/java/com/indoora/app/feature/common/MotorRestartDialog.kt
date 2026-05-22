package com.indoora.app.feature.common

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MotorRestartDialog(
    visible: Boolean,
    isSendingConfig: Boolean,
    onDismiss: () -> Unit,
    onSendConfig: () -> Unit
) {
    if (visible) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    "Reiniciar motor",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Para aplicar los cambios, debes reiniciar el motor de posicionamiento.",
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Text(
                        "Cuando el motor esté listo, pulsa 'Enviar configuración'.",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 14.sp
                    )
                    Text(
                        "Si el motor ha recibido la configuración, cierra esta pestaña. En otro caso, vuélvela a enviar.",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 14.sp
                    )
                    if (isSendingConfig) {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color.White
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = onSendConfig,
                    enabled = !isSendingConfig
                ) {
                    if (isSendingConfig) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Enviar configuración", color = Color.White)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onDismiss
                ) {
                    Text("Cerrar", color = Color.White.copy(alpha = 0.7f))
                }
            },
            containerColor = Color(0xFF4A4458),
            shape = RoundedCornerShape(16.dp)
        )
    }
}