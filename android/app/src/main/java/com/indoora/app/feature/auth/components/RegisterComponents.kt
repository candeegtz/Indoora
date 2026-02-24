package com.indoora.app.feature.auth.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.indoora.app.feature.auth.UiState

@Composable
fun RegisterHeader(step: RegisterStep, onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Volver",
                tint = MaterialTheme.colorScheme.onBackground
            )
        }

        Text(
            text = "Crear cuenta",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.size(48.dp))
    }
}

@Composable
fun StepIndicator(currentStep: RegisterStep) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        StepDot(active = currentStep.ordinal >= RegisterStep.CHOOSE_TYPE.ordinal, label = "Tipo")
        StepDivider()
        StepDot(active = currentStep.ordinal >= RegisterStep.FILL_FORM.ordinal, label = "Datos")
        StepDivider()
        StepDot(active = currentStep.ordinal >= RegisterStep.HOME_SETUP.ordinal, label = "Hogar")
    }
}

@Composable
private fun StepDot(active: Boolean, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(RoundedCornerShape(50))
                .background(
                    if (active) MaterialTheme.colorScheme.onBackground
                    else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = if (active) MaterialTheme.colorScheme.onBackground
            else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            fontSize = 11.sp
        )
    }
}

@Composable
private fun StepDivider() {
    HorizontalDivider(
        modifier = Modifier.width(32.dp),
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
        thickness = 1.dp
    )
}

@Composable
fun RegisterField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    enabled: Boolean = true,
    isPassword: Boolean = false
) {
    Column {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    placeholder,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                    fontSize = 14.sp
                )
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                focusedBorderColor = MaterialTheme.colorScheme.onBackground,
                unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                cursorColor = MaterialTheme.colorScheme.onBackground,
                disabledTextColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                disabledBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
            ),
            singleLine = true,
            enabled = enabled,
            visualTransformation = if (isPassword) PasswordVisualTransformation()
            else androidx.compose.ui.text.input.VisualTransformation.None
        )
    }
}

@Composable
fun ErrorMessage(registerState: UiState<*>, keywords: List<String>) {
    if (registerState is UiState.Error) {
        val errorMessage = (registerState as UiState.Error).message
        if (keywords.any { errorMessage.contains(it, ignoreCase = true) }) {
            Text(
                text = when {
                    errorMessage.contains("Subject not found", ignoreCase = true) ->
                        "No se encontró el sujeto indicado"
                    errorMessage.contains("already", ignoreCase = true) ->
                        "El usuario o email ya existe"
                    errorMessage.contains("connect", ignoreCase = true) ->
                        "No se puede conectar al servidor"
                    errorMessage.contains("404") -> "No se encontró el sujeto"
                    errorMessage.contains("400") -> "Datos incorrectos. Verifica la información."
                    else -> "Error: $errorMessage"
                },
                color = Color(0xFFFFCDD2),
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
