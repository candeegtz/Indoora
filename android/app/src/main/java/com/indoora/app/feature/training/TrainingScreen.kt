package com.indoora.app.feature.training

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.indoora.app.feature.deviceconfig.ConfigStep
import com.indoora.app.feature.deviceconfig.components.NavigationButtons
import com.indoora.app.feature.deviceconfig.components.StepCard
import com.indoora.app.ui.theme.indooraBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainingScreen(
    homeId: Int,
    onNavigateBack: () -> Unit,
    viewModel: TrainingViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentStepIndex by viewModel.currentStepIndex.collectAsState()
    val isTrainingActive by viewModel.isTrainingActive.collectAsState()
    val instruction by viewModel.instruction.collectAsState(initial = "")
    val progress by viewModel.progress.collectAsState()
    var showExitDialog by remember { mutableStateOf(false) }

    // Cargar la secuencia al entrar
    LaunchedEffect(Unit) {
        viewModel.loadTrainingSequence(homeId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Entrenamiento del Sistema", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = { showExitDialog = true }) {
                        Icon(Icons.Default.Close, contentDescription = "Salir")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
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
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                return@Column
            }

            if (uiState.error != null) {
                Text("Error: ${uiState.error}", color = MaterialTheme.colorScheme.error)
                return@Column
            }

            if (!isTrainingActive && !uiState.isTraining) {
                // Mostrar resumen de la secuencia a entrenar
                StepCard(
                    step = ConfigStep(
                        stepNumber = 1,
                        title = "Pre-entrenamiento",
                        description = "A continuación se entrenarán las siguientes posiciones:",
                        details = uiState.sequence.map { "${it.room} - ${it.position}" }
                    ),
                    currentStep = 0,
                    totalSteps = 1
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { viewModel.startTraining() },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Iniciar Entrenamiento")
                }
            } else if (isTrainingActive) {
                // Mostrar instrucción actual y progreso
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.15f))
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            text = "Instrucción",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = instruction.ifEmpty { "Esperando instrucción del motor..." },
                            fontSize = 16.sp,
                            lineHeight = 24.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        LinearProgressIndicator(
                            progress = progress / 100f,
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.confirmStep() },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = instruction.isNotEmpty()
                        ) {
                            Text("Listo, ya estoy en la posición")
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        TextButton(
                            onClick = { viewModel.cancelTraining() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Cancelar entrenamiento", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            } else if (uiState.trainingComplete) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.15f))
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text("🎉 Entrenamiento completado", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Los datos han sido recogidos. Ahora puedes ejecutar el entrenamiento del modelo desde el ordenador.")
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(onClick = onNavigateBack, modifier = Modifier.fillMaxWidth()) {
                            Text("Finalizar")
                        }
                    }
                }
            }
        }
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("¿Salir del entrenamiento?") },
            text = { Text("Si sales ahora, el progreso se perderá y tendrás que empezar de nuevo.") },
            confirmButton = {
                TextButton(onClick = { viewModel.cancelTraining(); onNavigateBack() }) {
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