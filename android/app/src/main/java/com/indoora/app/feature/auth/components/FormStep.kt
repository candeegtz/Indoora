package com.indoora.app.feature.auth.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.indoora.app.feature.auth.UiState

@Composable
fun FormStep(
    formData: RegisterFormData,
    registerState: UiState<*>,
    onNext: () -> Unit
) {
    val isFormValid = formData.isBasicFormValid()

    Column(modifier = Modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF8B7AB8).copy(alpha = 0.25f)
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                RegisterField(
                    label = "Nombre de usuario",
                    value = formData.username,
                    onValueChange = { formData.username = it },
                    placeholder = "usuario123"
                )

                RegisterField(
                    label = "Nombre",
                    value = formData.name,
                    onValueChange = { formData.name = it },
                    placeholder = "María"
                )

                RegisterField(
                    label = "Apellidos",
                    value = formData.surnames,
                    onValueChange = { formData.surnames = it },
                    placeholder = "García López"
                )

                RegisterField(
                    label = "Email",
                    value = formData.email,
                    onValueChange = { formData.email = it },
                    placeholder = "maria@gmail.com"
                )

                RegisterField(
                    label = "Contraseña",
                    value = formData.password,
                    onValueChange = { formData.password = it },
                    placeholder = "mínimo 6 caracteres",
                    isPassword = true
                )

                ErrorMessage(
                    registerState = registerState,
                    keywords = listOf("username", "email", "password", "already", "400")
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                disabledContainerColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.15f)
            ),
            shape = RoundedCornerShape(12.dp),
            enabled = isFormValid
        ) {
            Text(
                "Continuar",
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
        }
    }
}
