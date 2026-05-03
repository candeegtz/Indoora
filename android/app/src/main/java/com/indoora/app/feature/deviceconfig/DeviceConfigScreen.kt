package com.indoora.app.feature.deviceconfig

import DeviceConfigViewModelFactory
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
import com.indoora.app.data.repository.HomeRepository
import com.indoora.app.feature.deviceconfig.components.NavigationButtons
import com.indoora.app.feature.deviceconfig.components.StepCard
import com.indoora.app.ui.theme.indooraBackground
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceConfigScreen(
    homeId: Int,
    onNavigateBack: () -> Unit,
    homeRepository: HomeRepository,
    viewModel: DeviceConfigViewModel = viewModel(
        factory = DeviceConfigViewModelFactory(homeRepository)
    )
) {
    val currentStepIndex by viewModel.currentStep.collectAsState()
    val currentStep = DeviceConfigSteps.steps[currentStepIndex]

    var showExitDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Resetear pasos al entrar
    LaunchedEffect(Unit) {
        viewModel.resetSteps()
    }

    // Observar eventos del ViewModel
    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                is DeviceConfigEvent.ConfigurationFinished -> {
                    onNavigateBack()
                }
                is DeviceConfigEvent.Error -> {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(event.message)
                    }
                }
            }
        }
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
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
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

            StepCard(
                step = currentStep,
                currentStep = currentStepIndex,
                totalSteps = viewModel.totalSteps
            )

            Spacer(modifier = Modifier.height(24.dp))

            NavigationButtons(
                canGoPrevious = viewModel.canGoPrevious(),
                canGoNext = viewModel.canGoNext(),
                isLastStep = viewModel.isLastStep(),
                onPrevious = { viewModel.previousStep() },
                onNext = { viewModel.nextStep() },
                onFinish = {
                    viewModel.finishConfiguration(homeId)
                }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Diálogo de confirmación para salir
    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("¿Salir de la configuración?", fontWeight = FontWeight.Bold) },
            text = { Text("Si sales ahora, tendrás que empezar desde el principio la próxima vez.") },
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