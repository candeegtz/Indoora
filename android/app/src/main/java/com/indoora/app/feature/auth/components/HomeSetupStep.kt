package com.indoora.app.feature.auth.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.indoora.app.feature.auth.UiState

@Composable
fun HomeSetupStep(
    isSupervisorCreator: Boolean,
    formData: RegisterFormData,
    registerState: UiState<*>,
    onSubmit: () -> Unit
) {
    val isLoading = registerState is UiState.Loading
    val isFormValid = if (isSupervisorCreator) {
        formData.homeName.isNotBlank()
    } else {
        formData.subjectUsername.isNotBlank()
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = if (isSupervisorCreator) "Crea tu hogar" else "Únete al hogar",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

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
                if (isSupervisorCreator) {
                    RegisterField(
                        label = "Nombre de la casa",
                        value = formData.homeName,
                        onValueChange = { formData.homeName = it },
                        placeholder = "Casa García",
                        enabled = !isLoading
                    )
                    Text(
                        text = "Este será el nombre de tu hogar en Indoora",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )
                } else {
                    RegisterField(
                        label = "Username del sujeto",
                        value = formData.subjectUsername,
                        onValueChange = { formData.subjectUsername = it },
                        placeholder = "Username del sujeto existente",
                        enabled = !isLoading
                    )
                    Text(
                        text = "Introduce el nombre de usuario del sujeto al que deseas supervisar",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )
                }

                ErrorMessage(
                    registerState = registerState,
                    keywords = listOf("subject", "404", "400", "connect")
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onSubmit,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                disabledContainerColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.15f)
            ),
            shape = RoundedCornerShape(12.dp),
            enabled = isFormValid && !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    "Crear cuenta",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
            }
        }
    }
}
