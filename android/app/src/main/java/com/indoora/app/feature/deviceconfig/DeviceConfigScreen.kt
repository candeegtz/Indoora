package com.indoora.app.feature.deviceconfig

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.indoora.app.feature.deviceconfig.components.NavigationButtons
import com.indoora.app.feature.deviceconfig.components.StepCard
import com.indoora.app.ui.theme.indooraBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceConfigScreen(
    homeId: Int,
    onNavigateBack: () -> Unit,
    viewModel: DeviceConfigViewModel = viewModel()
) {
    val currentStepIndex by viewModel.currentStep.collectAsState()
    val currentStep = DeviceConfigSteps.steps[currentStepIndex]

    var showExitDialog by remember { mutableStateOf(false) }

    // Resetear pasos al entrar
    LaunchedEffect(Unit) {
        viewModel.resetSteps()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Configuración de Dispositivos",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { showExitDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Salir",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = androidx.compose.ui.graphics.Color.Transparent
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .indooraBackground()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Card del paso actual
            StepCard(
                step = currentStep,
                currentStep = currentStepIndex,
                totalSteps = viewModel.totalSteps
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Botones de navegación
            NavigationButtons(
                canGoPrevious = viewModel.canGoPrevious(),
                canGoNext = viewModel.canGoNext(),
                isLastStep = viewModel.isLastStep(),
                onPrevious = { viewModel.previousStep() },
                onNext = { viewModel.nextStep() },
                onFinish = { onNavigateBack() }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Diálogo de confirmación para salir
    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = {
                Text(
                    "¿Salir de la configuración?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("Si sales ahora, tendrás que empezar desde el principio la próxima vez.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showExitDialog = false
                        onNavigateBack()
                    }
                ) {
                    Text("Salir", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}