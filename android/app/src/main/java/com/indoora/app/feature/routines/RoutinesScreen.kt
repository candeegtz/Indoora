package com.indoora.app.feature.routines

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indoora.app.data.model.ActivityRead
import com.indoora.app.data.model.RoutineRead
import com.indoora.app.data.repository.ActivityRepository
import com.indoora.app.feature.auth.UiState
import com.indoora.app.ui.theme.indooraBackground
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutinesScreen(
    viewModel: RoutinesViewModel,
    homeId: Int,
    onNavigateBack: () -> Unit
) {
    val routinesState by viewModel.routinesState.collectAsStateWithLifecycle()
    val createState by viewModel.createState.collectAsStateWithLifecycle()

    var showCreateDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rutinas", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = Color.White.copy(alpha = 0.2f),
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nueva rutina")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .indooraBackground()
                .padding(paddingValues)
        ) {
            when (routinesState) {
                is UiState.Loading -> {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        CircularProgressIndicator(color = Color.White)
                    }
                }
                is UiState.Success -> {
                    val routines = (routinesState as UiState.Success).data
                    if (routines.isEmpty()) {
                        Box(Modifier.fillMaxSize(), Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("No hay rutinas creadas", color = Color.White.copy(alpha = 0.7f), fontSize = 16.sp)
                                Spacer(Modifier.height(8.dp))
                                Text("Toca + para crear una", color = Color.White.copy(alpha = 0.5f), fontSize = 14.sp)
                            }
                        }
                    } else {
                        LazyColumn(
                            Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(routines, key = { it.id }) { routine ->
                                RoutineCard(routine = routine)
                            }
                        }
                    }
                }
                is UiState.Error -> {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Error al cargar rutinas", color = MaterialTheme.colorScheme.error, fontSize = 16.sp)
                            Spacer(Modifier.height(8.dp))
                            Text((routinesState as UiState.Error).message, color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f))
                            Spacer(Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.loadRoutines() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f))
                            ) {
                                Text("Reintentar", color = Color.White)
                            }
                        }
                    }
                }
                else -> {}
            }

            if (showCreateDialog) {
                CreateRoutineDialog(
                    homeId = homeId,
                    onDismiss = { showCreateDialog = false },
                    onCreate = { name, desc, startTime, endTime, days, activityId ->
                        viewModel.createRoutine(name, desc, startTime, endTime, days, activityId)
                    },
                    isCreating = createState is UiState.Loading
                )
            }

            val feedback = when (createState) {
                is UiState.Success -> "Rutina creada correctamente"
                is UiState.Error -> (createState as UiState.Error).message
                else -> null
            }
            feedback?.let {
                val isError = createState is UiState.Error
                Card(
                    colors = CardDefaults.cardColors(containerColor = if (isError) Color(0xFFCF6679) else Color(0xFF03DAC6)),
                    modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
                ) {
                    Text(it, Modifier.padding(16.dp), color = Color.Black, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
fun RoutineCard(routine: RoutineRead) {
    val spanishDays = mapOf(
        "MONDAY" to "Lunes", "TUESDAY" to "Martes", "WEDNESDAY" to "Miércoles",
        "THURSDAY" to "Jueves", "FRIDAY" to "Viernes", "SATURDAY" to "Sábado", "SUNDAY" to "Domingo"
    )
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.2f))
    ) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(routine.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
            routine.description?.let {
                Text(it, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.8f))
            }
            Text(
                "Horario: ${routine.startTime.take(5)} - ${routine.endTime.take(5)}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.7f)
            )
            val daysText = routine.days.mapNotNull { spanishDays[it] }.joinToString(", ")
            Text("Días: $daysText", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
            Text("Actividad ID: ${routine.activityId}", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.6f))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRoutineDialog(
    homeId: Int,
    onDismiss: () -> Unit,
    onCreate: (name: String, description: String?, startTime: String, endTime: String, days: List<String>, activityId: Int) -> Unit,
    isCreating: Boolean
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf("08:00") }
    var endTime by remember { mutableStateOf("09:00") }
    var selectedDays by remember { mutableStateOf(emptySet<String>()) }
    var selectedActivityId by remember { mutableStateOf<Int?>(null) }
    var dropdownExpanded by remember { mutableStateOf(false) }

    val activityRepository = remember { ActivityRepository() }
    var activities by remember { mutableStateOf<List<ActivityRead>>(emptyList()) }
    var isLoadingActivities by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var reloadTrigger by remember { mutableStateOf(0) }

    // Cargar actividades al abrir el diálogo o cuando se reintente
    LaunchedEffect(reloadTrigger) {
        isLoadingActivities = true
        loadError = null
        val result = activityRepository.getActivities(homeId)
        result.fold(
            onSuccess = { activities = it },
            onFailure = { loadError = it.message ?: "Error al cargar actividades" }
        )
        isLoadingActivities = false
    }

    val daysOfWeek = listOf(
        "MONDAY" to "Lunes", "TUESDAY" to "Martes", "WEDNESDAY" to "Miércoles",
        "THURSDAY" to "Jueves", "FRIDAY" to "Viernes", "SATURDAY" to "Sábado", "SUNDAY" to "Domingo"
    )
    val firstRowDays = daysOfWeek.take(4)
    val secondRowDays = daysOfWeek.drop(4)

    fun isValidTime(time: String): Boolean = time.matches(Regex("^([01]?[0-9]|2[0-3]):[0-5][0-9]$"))

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva rutina", fontWeight = FontWeight.Bold, color = Color.White) },
        text = {
            Column(
                Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre", color = Color.White.copy(alpha = 0.7f)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                        cursorColor = Color.White,
                        focusedLabelColor = Color.White,
                        unfocusedLabelColor = Color.White.copy(alpha = 0.6f)
                    ),
                    textStyle = androidx.compose.ui.text.TextStyle(color = Color.White)
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción (opcional)", color = Color.White.copy(alpha = 0.7f)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(/* igual */),
                    textStyle = androidx.compose.ui.text.TextStyle(color = Color.White)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = startTime,
                        onValueChange = { newValue ->
                            if (newValue.length <= 5) {
                                var filtered = newValue.filter { it.isDigit() || it == ':' }
                                if (filtered.length == 2 && !filtered.contains(":")) filtered += ":"
                                if (filtered.length <= 5) startTime = filtered
                            }
                        },
                        label = { Text("Hora inicio (HH:MM)", color = Color.White.copy(alpha = 0.7f)) },
                        isError = startTime.isNotBlank() && !isValidTime(startTime),
                        supportingText = {
                            if (startTime.isNotBlank() && !isValidTime(startTime))
                                Text("Formato HH:MM (24h)", color = Color.Red, fontSize = 10.sp)
                        },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(/* igual */),
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White)
                    )
                    OutlinedTextField(
                        value = endTime,
                        onValueChange = { newValue ->
                            if (newValue.length <= 5) {
                                var filtered = newValue.filter { it.isDigit() || it == ':' }
                                if (filtered.length == 2 && !filtered.contains(":")) filtered += ":"
                                if (filtered.length <= 5) endTime = filtered
                            }
                        },
                        label = { Text("Hora fin (HH:MM)", color = Color.White.copy(alpha = 0.7f)) },
                        isError = endTime.isNotBlank() && !isValidTime(endTime),
                        supportingText = {
                            if (endTime.isNotBlank() && !isValidTime(endTime))
                                Text("Formato HH:MM (24h)", color = Color.Red, fontSize = 10.sp)
                        },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(/* igual */),
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White)
                    )
                }

                Text("Días de la semana", color = Color.White, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    firstRowDays.forEach { (key, displayName) ->
                        FilterChip(
                            selected = selectedDays.contains(key),
                            onClick = {
                                selectedDays = if (selectedDays.contains(key)) selectedDays - key else selectedDays + key
                            },
                            label = { Text(displayName, color = if (selectedDays.contains(key)) Color.White else Color.White.copy(alpha = 0.7f)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color.White.copy(alpha = 0.3f),
                                disabledSelectedContainerColor = Color.White.copy(alpha = 0.1f),
                                containerColor = Color.White.copy(alpha = 0.1f)
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    secondRowDays.forEach { (key, displayName) ->
                        FilterChip(
                            selected = selectedDays.contains(key),
                            onClick = {
                                selectedDays = if (selectedDays.contains(key)) selectedDays - key else selectedDays + key
                            },
                            label = { Text(displayName, color = if (selectedDays.contains(key)) Color.White else Color.White.copy(alpha = 0.7f)) },
                            colors = FilterChipDefaults.filterChipColors(/* igual */),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Text("Actividad", color = Color.White, fontWeight = FontWeight.Medium, fontSize = 14.sp)

                when {
                    isLoadingActivities -> {
                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        }
                    }
                    loadError != null -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(loadError!!, color = Color.Red, fontSize = 12.sp)
                            Spacer(Modifier.height(4.dp))
                            Button(
                                onClick = { reloadTrigger++ },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f))
                            ) {
                                Text("Reintentar", color = Color.White)
                            }
                        }
                    }
                    activities.isEmpty() -> {
                        Text("No hay actividades en esta casa", color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp)
                    }
                    else -> {
                        // Usamos un DropdownMenu estándar en lugar de ExposedDropdownMenuBox
                        Column {
                            OutlinedTextField(
                                value = selectedActivityId?.let { id ->
                                    activities.find { it.id == id }?.name ?: ""
                                } ?: "",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Selecciona una actividad", color = Color.White.copy(alpha = 0.7f)) },
                                trailingIcon = {
                                    IconButton(onClick = { dropdownExpanded = !dropdownExpanded }) {
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Abrir menú", tint = Color.White)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color.White,
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                                    cursorColor = Color.White,
                                    focusedLabelColor = Color.White,
                                    unfocusedLabelColor = Color.White.copy(alpha = 0.6f)
                                ),
                                textStyle = androidx.compose.ui.text.TextStyle(color = Color.White)
                            )
                            DropdownMenu(
                                expanded = dropdownExpanded,
                                onDismissRequest = { dropdownExpanded = false },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                activities.forEach { activity ->
                                    DropdownMenuItem(
                                        text = { Text(activity.name, color = Color.White) },
                                        onClick = {
                                            selectedActivityId = activity.id
                                            dropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank() && selectedDays.isNotEmpty() && selectedActivityId != null && isValidTime(startTime) && isValidTime(endTime)) {
                        onCreate(
                            name,
                            description.takeIf { it.isNotBlank() },
                            startTime,
                            endTime,
                            selectedDays.toList(),
                            selectedActivityId!!
                        )
                    }
                },
                enabled = !isCreating && name.isNotBlank() && selectedDays.isNotEmpty() && selectedActivityId != null && isValidTime(startTime) && isValidTime(endTime) && !isLoadingActivities
            ) {
                if (isCreating) CircularProgressIndicator(Modifier.size(20.dp), color = Color.White)
                else Text("Crear", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = Color.White.copy(alpha = 0.7f))
            }
        },
        containerColor = Color(0xFF4A4458),
        shape = RoundedCornerShape(16.dp)
    )
}