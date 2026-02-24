package com.indoora.app.feature.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.indoora.app.feature.auth.components.*
import com.indoora.app.ui.theme.indooraBackground

@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateBack: () -> Unit = {}
) {
    var step by remember { mutableStateOf(RegisterStep.CHOOSE_TYPE) }
    var isSupervisorCreator by remember { mutableStateOf(true) }

    val formData = remember { RegisterFormData() }
    val registerState by viewModel.registerAndLoginState.collectAsState()

    LaunchedEffect(registerState) {
        if (registerState is UiState.Success) onRegisterSuccess()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .indooraBackground()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        RegisterHeader(
            step = step,
            onBack = {
                when (step) {
                    RegisterStep.CHOOSE_TYPE -> onNavigateBack()
                    RegisterStep.FILL_FORM -> step = RegisterStep.CHOOSE_TYPE
                    RegisterStep.HOME_SETUP -> step = RegisterStep.FILL_FORM
                }
            }
        )

        Spacer(modifier = Modifier.height(32.dp))
        StepIndicator(currentStep = step)
        Spacer(modifier = Modifier.height(32.dp))

        AnimatedContent(
            targetState = step,
            transitionSpec = {
                slideInHorizontally(
                    animationSpec = tween(300),
                    initialOffsetX = { if (targetState.ordinal > initialState.ordinal) it else -it }
                ) togetherWith slideOutHorizontally(
                    animationSpec = tween(300),
                    targetOffsetX = { if (targetState.ordinal > initialState.ordinal) -it else it }
                )
            },
            label = "step_transition"
        ) { currentStep ->
            when (currentStep) {
                RegisterStep.CHOOSE_TYPE -> ChooseTypeStep(
                    onSelectType = {
                        isSupervisorCreator = it
                        step = RegisterStep.FILL_FORM
                    }
                )
                RegisterStep.FILL_FORM -> FormStep(
                    formData = formData,
                    registerState = registerState,
                    onNext = { step = RegisterStep.HOME_SETUP }
                )
                RegisterStep.HOME_SETUP -> HomeSetupStep(
                    isSupervisorCreator = isSupervisorCreator,
                    formData = formData,
                    registerState = registerState,
                    onSubmit = { viewModel.registerAndLogin(formData.toUserCreate(isSupervisorCreator)) }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        TextButton(onClick = onNavigateToLogin) {
            Text(
                "¿Ya tienes cuenta? Inicia sesión",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}