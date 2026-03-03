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
fun CreateSubjectStep(
    subjectFormData: SubjectFormData,
    createSubjectState: UiState<*>,
    onSubmit: () -> Unit,
) {
    val isLoading = createSubjectState is UiState.Loading
    val isFormValid = subjectFormData.isValid()

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Crea el sujeto de tu hogar",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "El sujeto es la persona que será supervisada en este hogar",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
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
                RegisterField(
                    label = "Nombre de usuario",
                    value = subjectFormData.username,
                    onValueChange = { subjectFormData.username = it },
                    placeholder = "sujeto123",
                    enabled = !isLoading
                )

                RegisterField(
                    label = "Nombre",
                    value = subjectFormData.name,
                    onValueChange = { subjectFormData.name = it },
                    placeholder = "Juan",
                    enabled = !isLoading
                )

                RegisterField(
                    label = "Apellidos",
                    value = subjectFormData.surnames,
                    onValueChange = { subjectFormData.surnames = it },
                    placeholder = "Pérez García",
                    enabled = !isLoading
                )

                RegisterField(
                    label = "Email",
                    value = subjectFormData.email,
                    onValueChange = { subjectFormData.email = it },
                    placeholder = "juan@gmail.com",
                    enabled = !isLoading
                )

                RegisterField(
                    label = "Contraseña",
                    value = subjectFormData.password,
                    onValueChange = { subjectFormData.password = it },
                    placeholder = "mínimo 6 caracteres",
                    enabled = !isLoading,
                    isPassword = true
                )

                ErrorMessage(
                    registerState = createSubjectState,
                    keywords = listOf("username", "email", "already", "400")
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
                    "Crear sujeto",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

    }
}