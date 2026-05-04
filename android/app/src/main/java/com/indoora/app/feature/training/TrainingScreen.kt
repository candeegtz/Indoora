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
import com.indoora.app.ui.theme.indooraBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainingScreen(
    homeId: Int,
    onNavigateBack: () -> Unit,
    viewModel: TrainingViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val instruction by viewModel.instruction.collectAsState()
    val progressReadings by viewModel.progressReadings.collectAsState()
    val progressTotal by viewModel.progressTotal.collectAsState()
    val navigateToHome by viewModel.navigateToHome.collectAsState()
    var showExitDialog by remember { mutableStateOf(false) }
    val isStepConfirmed by viewModel.isStepConfirmed.collectAsState()

    // Cargar secuencia al entrar
    LaunchedEffect(Unit) {
        viewModel.loadTrainingSequence(homeId)
    }

    // Cerrar pantalla cuando se solicite (desde ViewModel)
    LaunchedEffect(navigateToHome) {
        if (navigateToHome) {
            onNavigateBack()
        }
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
            when (uiState) {
                is TrainingUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is TrainingUiState.Error -> {
                    Text(
                        text = "Error: ${(uiState as TrainingUiState.Error).message}",
                        color = MaterialTheme.colorScheme.error
                    )
                }
                is TrainingUiState.Ready -> {
                    val state = uiState as TrainingUiState.Ready
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.15f))
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text("Pre‑entrenamiento", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Se entrenarán las siguientes posiciones:")
                            Spacer(modifier = Modifier.height(12.dp))
                            state.sequence.forEach { step ->
                                Text("• ${step.room} - ${step.position}", fontSize = 14.sp)
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = { viewModel.startTraining() },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Iniciar Entrenamiento")
                            }
                        }
                    }
                }
                TrainingUiState.Training -> {
                    Column {
                        Text("Progreso general: $progressTotal%", fontSize = 14.sp)
                        LinearProgressIndicator(
                            progress = progressTotal / 100f,
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Text("Lecturas de esta posición: $progressReadings%", fontSize = 14.sp)
                        LinearProgressIndicator(
                            progress = progressReadings / 100f,
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.15f))
                        ) {
                            Column(modifier = Modifier.padding(24.dp)) {
                                Text("Instrucción", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = instruction.ifEmpty { "Esperando instrucción del motor..." },
                                    fontSize = 16.sp,
                                    lineHeight = 24.sp
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { viewModel.confirmStep() },
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = instruction.isNotEmpty() && !isStepConfirmed
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
                    }
                }
                TrainingUiState.TrainingComplete -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.15f))
                    ) {
                        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Recogida de datos completada", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Todos los datos se han guardado correctamente.")
                        }
                    }
                }
                TrainingUiState.ModelTraining -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.15f))
                    ) {
                        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.onBackground)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Entrenando el modelo...", fontSize = 18.sp, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Este proceso puede tardar unos segundos.\nPuedes volver al inicio mientras tanto.", fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(onClick = onNavigateBack, modifier = Modifier.fillMaxWidth()) {
                                Text("Volver al inicio")
                            }
                        }
                    }
                }
                TrainingUiState.ModelReady -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.15f))
                    ) {
                        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Modelo entrenado con éxito", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("El sistema ya está completamente configurado.", fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(onClick = onNavigateBack, modifier = Modifier.fillMaxWidth()) {
                                Text("Ir al inicio")
                            }
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