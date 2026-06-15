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
fun StepIndicator(currentStep: RegisterStep, isSupervisorCreator: Boolean) {
    val commonSteps = listOf(
        RegisterStep.CHOOSE_TYPE  to "Tipo",
        RegisterStep.FILL_FORM    to "Datos",
        RegisterStep.HOME_SETUP   to "Hogar"
    )

    val creatorSteps = listOf(
        RegisterStep.CREATE_SUBJECT  to "Sujeto",
        RegisterStep.ADD_ROOMS       to "Habitaciones",
        RegisterStep.ADD_POSITIONS   to "Posiciones",
        RegisterStep.CONFIRM_SETUP   to "Confirmar"
    )

    val allSteps = if (isSupervisorCreator) commonSteps + creatorSteps else commonSteps

    val currentIndex = allSteps.indexOfFirst { it.first == currentStep }

    val visibleSteps = when {
        currentIndex <= 0 -> {
            allSteps.take(3.coerceAtMost(allSteps.size))
        }
        currentIndex >= allSteps.lastIndex -> {
            allSteps.takeLast(3.coerceAtMost(allSteps.size))
        }
        else -> {
            allSteps.subList(
                (currentIndex - 1).coerceAtLeast(0),
                (currentIndex + 2).coerceAtMost(allSteps.size)
            )
        }
    }

    val hasStepsBefore = currentIndex > 1
    val hasStepsAfter = currentIndex < allSteps.lastIndex - 1

    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        if (hasStepsBefore) {
            Text(
                text = "···",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(end = 8.dp)
            )
        }

        visibleSteps.forEachIndexed { index, (stepEnum, label) ->
            val isActive = currentStep.ordinal >= stepEnum.ordinal
            val isCurrent = currentStep == stepEnum

            StepDot(
                active = isActive,
                current = isCurrent,
                label = label
            )

            if (index < visibleSteps.lastIndex) {
                StepDivider(active = currentStep.ordinal > stepEnum.ordinal)
            }
        }

        if (hasStepsAfter) {
            Text(
                text = "···",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@Composable
private fun StepDot(active: Boolean, current: Boolean, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(if (current) 16.dp else 10.dp)
                .clip(RoundedCornerShape(50))
                .background(
                    when {
                        current -> MaterialTheme.colorScheme.onBackground
                        active  -> MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                        else    -> MaterialTheme.colorScheme.onBackground.copy(alpha = 0.25f)
                    }
                )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = when {
                current -> MaterialTheme.colorScheme.onBackground
                active  -> MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                else    -> MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
            },
            fontSize = if (current) 11.sp else 9.sp,
            fontWeight = if (current) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun StepDivider(active: Boolean = false) {
    HorizontalDivider(
        modifier = Modifier.width(20.dp),
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = if (active) 0.6f else 0.25f),
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
