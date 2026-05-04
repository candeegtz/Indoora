package com.indoora.app.feature.routines

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
import com.indoora.app.data.model.RoutineCreate
import com.indoora.app.data.model.RoutineRead
import com.indoora.app.data.model.RoutineUpdate
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
    val updateState by viewModel.updateState.collectAsStateWithLifecycle()
    val deleteState by viewModel.deleteState.collectAsStateWithLifecycle()

    // Estado del filtro y lista filtrada/ordenada
    val selectedDay by viewModel.selectedDay.collectAsStateWithLifecycle()
    val filteredRoutines by viewModel.filteredRoutines.collectAsStateWithLifecycle()

    var showCreateDialog by remember { mutableStateOf(false) }
    var routineToEdit by remember { mutableStateOf<RoutineRead?>(null) }
    var routineToDelete by remember { mutableStateOf<RoutineRead?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    // Cargar actividades para mostrar el nombre en lugar del ID
    val activityRepository = remember { ActivityRepository() }
    var activities by remember { mutableStateOf<List<ActivityRead>>(emptyList()) }
    var isLoadingActivities by remember { mutableStateOf(true) }
    var activityNameMap by remember { mutableStateOf<Map<Int, String>>(emptyMap()) }

    LaunchedEffect(Unit) {
        isLoadingActivities = true
        val result = activityRepository.getActivities(homeId)
        result.fold(
            onSuccess = {
                activities = it
                activityNameMap = it.associate { it.id to it.name }
            },
            onFailure = { /* manejar error */ }
        )
        isLoadingActivities = false
    }

    // Cerrar diálogo de creación cuando la operación sea exitosa
    LaunchedEffect(createState) {
        if (createState is UiState.Success) {
            showCreateDialog = false
        }
    }

    // Cerrar diálogo de edición cuando la operación sea exitosa
    LaunchedEffect(updateState) {
        if (updateState is UiState.Success) {
            routineToEdit = null
        }
    }

    // Limpiar mensajes después de 3 segundos
    LaunchedEffect(createState, updateState, deleteState) {
        if (createState is UiState.Success || createState is UiState.Error) delay(3000)
        if (updateState is UiState.Success || updateState is UiState.Error) delay(3000)
        if (deleteState is UiState.Success || deleteState is UiState.Error) delay(3000)
        viewModel.resetCreateState()
        viewModel.resetUpdateState()
        viewModel.resetDeleteState()
    }

    // Días para el filtro (null = Todos)
    val daysFilter = listOf(
        null to "Todos",
        "MONDAY" to "Lunes",
        "TUESDAY" to "Martes",
        "WEDNESDAY" to "Miércoles",
        "THURSDAY" to "Jueves",
        "FRIDAY" to "Viernes",
        "SATURDAY" to "Sábado",
        "SUNDAY" to "Domingo"
    )

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
                    Column {
                        // Fila de filtros (chips horizontales)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(vertical = 8.dp, horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            daysFilter.forEach { (dayCode, dayName) ->
                                FilterChip(
                                    selected = selectedDay == dayCode,
                                    onClick = { viewModel.setSelectedDay(dayCode) },
                                    label = { Text(dayName, color = if (selectedDay == dayCode) Color.White else Color.White.copy(alpha = 0.7f)) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color.White.copy(alpha = 0.3f),
                                        disabledSelectedContainerColor = Color.White.copy(alpha = 0.1f),
                                        containerColor = Color.White.copy(alpha = 0.1f)
                                    ),
                                    modifier = Modifier.padding(horizontal = 2.dp)
                                )
                            }
                        }

                        if (filteredRoutines.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    if (selectedDay == null) "No hay rutinas creadas"
                                    else "No hay rutinas para este día",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 16.sp
                                )
                            }
                        } else {
                            LazyColumn(
                                Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(filteredRoutines, key = { it.id }) { routine ->
                                    RoutineCard(
                                        routine = routine,
                                        activityName = activityNameMap[routine.activityId] ?: "Actividad ${routine.activityId}",
                                        onEditClick = { routineToEdit = routine },
                                        onDeleteClick = {
                                            routineToDelete = routine
                                            showDeleteConfirm = true
                                        }
                                    )
                                }
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

            // Diálogo de creación
            if (showCreateDialog) {
                CreateRoutineDialog(
                    homeId = homeId,
                    existingActivities = activities,
                    onDismiss = { showCreateDialog = false },
                    onCreate = { routineCreate ->
                        viewModel.createRoutine(routineCreate)
                    },
                    isCreating = createState is UiState.Loading
                )
            }

            // Diálogo de edición
            if (routineToEdit != null) {
                EditRoutineDialog(
                    routine = routineToEdit!!,
                    existingActivities = activities,
                    onDismiss = { routineToEdit = null },
                    onUpdate = { routineUpdate ->
                        viewModel.updateRoutine(routineToEdit!!.id, routineUpdate)
                    },
                    isUpdating = updateState is UiState.Loading
                )
            }

            // Confirmación de eliminación
            if (showDeleteConfirm && routineToDelete != null) {
                AlertDialog(
                    onDismissRequest = {
                        showDeleteConfirm = false
                        routineToDelete = null
                    },
                    title = { Text("Eliminar rutina", color = Color.White) },
                    text = { Text("¿Estás seguro de que quieres eliminar \"${routineToDelete?.name}\"?", color = Color.White.copy(alpha = 0.8f)) },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                routineToDelete?.let { viewModel.deleteRoutine(it.id) }
                                showDeleteConfirm = false
                                routineToDelete = null
                            }
                        ) {
                            Text("Eliminar", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                showDeleteConfirm = false
                                routineToDelete = null
                            }
                        ) {
                            Text("Cancelar", color = Color.White.copy(alpha = 0.7f))
                        }
                    },
                    containerColor = Color(0xFF4A4458),
                    shape = RoundedCornerShape(16.dp)
                )
            }

            val feedback = when {
                createState is UiState.Success -> "Rutina creada correctamente"
                createState is UiState.Error -> (createState as UiState.Error).message
                updateState is UiState.Success -> "Rutina actualizada correctamente"
                updateState is UiState.Error -> (updateState as UiState.Error).message
                deleteState is UiState.Success -> "Rutina eliminada correctamente"
                deleteState is UiState.Error -> (deleteState as UiState.Error).message
                else -> null
            }
            feedback?.let {
                val isError = createState is UiState.Error || updateState is UiState.Error || deleteState is UiState.Error
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
fun RoutineCard(
    routine: RoutineRead,
    activityName: String,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val spanishDays = mapOf(
        "MONDAY" to "Lunes", "TUESDAY" to "Martes", "WEDNESDAY" to "Miércoles",
        "THURSDAY" to "Jueves", "FRIDAY" to "Viernes", "SATURDAY" to "Sábado", "SUNDAY" to "Domingo"
    )
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
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
                Text("Actividad: $activityName", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
            }
            Row {
                IconButton(onClick = onEditClick) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color.White)
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.White)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRoutineDialog(
    homeId: Int,
    existingActivities: List<ActivityRead>,
    onDismiss: () -> Unit,
    onCreate: (RoutineCreate) -> Unit,
    isCreating: Boolean
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf("08:00") }
    var endTime by remember { mutableStateOf("09:00") }
    var selectedDays by remember { mutableStateOf(emptySet<String>()) }
    var selectedActivityId by remember { mutableStateOf<Int?>(null) }
    var expanded by remember { mutableStateOf(false) }

    val daysOfWeek = listOf(
        "MONDAY" to "Lunes", "TUESDAY" to "Martes", "WEDNESDAY" to "Miércoles",
        "THURSDAY" to "Jueves", "FRIDAY" to "Viernes", "SATURDAY" to "Sábado", "SUNDAY" to "Domingo"
    )

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
                    value = name, onValueChange = { name = it },
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
                    value = description, onValueChange = { description = it },
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
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.height(140.dp),
                    contentPadding = PaddingValues(4.dp)
                ) {
                    items(daysOfWeek) { (key, displayName) ->
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
                            modifier = Modifier.padding(4.dp).fillMaxWidth()
                        )
                    }
                }

                Text("Actividad", color = Color.White, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                Box {
                    OutlinedTextField(
                        value = selectedActivityId?.let { id ->
                            existingActivities.find { it.id == id }?.name ?: ""
                        } ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Selecciona una actividad", color = Color.White.copy(alpha = 0.7f)) },
                        trailingIcon = {
                            IconButton(onClick = { expanded = !expanded }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Abrir", tint = Color.White)
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
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        existingActivities.forEach { activity ->
                            DropdownMenuItem(
                                text = { Text(activity.name, color = Color.White) },
                                onClick = {
                                    selectedActivityId = activity.id
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank() && selectedDays.isNotEmpty() && selectedActivityId != null && isValidTime(startTime) && isValidTime(endTime)) {
                        val routineCreate = RoutineCreate(
                            name = name,
                            description = description.takeIf { it.isNotBlank() },
                            startTime = startTime,
                            endTime = endTime,
                            days = selectedDays.toList(),
                            activityId = selectedActivityId!!
                        )
                        onCreate(routineCreate)
                    }
                },
                enabled = !isCreating && name.isNotBlank() && selectedDays.isNotEmpty() && selectedActivityId != null && isValidTime(startTime) && isValidTime(endTime)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditRoutineDialog(
    routine: RoutineRead,
    existingActivities: List<ActivityRead>,
    onDismiss: () -> Unit,
    onUpdate: (RoutineUpdate) -> Unit,
    isUpdating: Boolean
) {
    var name by remember { mutableStateOf(routine.name) }
    var description by remember { mutableStateOf(routine.description ?: "") }
    var startTime by remember { mutableStateOf(routine.startTime.take(5)) }
    var endTime by remember { mutableStateOf(routine.endTime.take(5)) }
    var selectedDays by remember { mutableStateOf(routine.days.toSet()) }
    var selectedActivityId by remember { mutableStateOf(routine.activityId) }
    var expanded by remember { mutableStateOf(false) }

    val daysOfWeek = listOf(
        "MONDAY" to "Lunes", "TUESDAY" to "Martes", "WEDNESDAY" to "Miércoles",
        "THURSDAY" to "Jueves", "FRIDAY" to "Viernes", "SATURDAY" to "Sábado", "SUNDAY" to "Domingo"
    )

    fun isValidTime(time: String): Boolean = time.matches(Regex("^([01]?[0-9]|2[0-3]):[0-5][0-9]$"))

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar rutina", fontWeight = FontWeight.Bold, color = Color.White) },
        text = {
            Column(
                Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    label = { Text("Nombre", color = Color.White.copy(alpha = 0.7f)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(/* igual */),
                    textStyle = androidx.compose.ui.text.TextStyle(color = Color.White)
                )
                OutlinedTextField(
                    value = description, onValueChange = { description = it },
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
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.height(140.dp),
                    contentPadding = PaddingValues(4.dp)
                ) {
                    items(daysOfWeek) { (key, displayName) ->
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
                            modifier = Modifier.padding(4.dp).fillMaxWidth()
                        )
                    }
                }

                Text("Actividad", color = Color.White, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                Box {
                    OutlinedTextField(
                        value = existingActivities.find { it.id == selectedActivityId }?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Selecciona una actividad", color = Color.White.copy(alpha = 0.7f)) },
                        trailingIcon = {
                            IconButton(onClick = { expanded = !expanded }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Abrir", tint = Color.White)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(/* igual */),
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White)
                    )
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        existingActivities.forEach { activity ->
                            DropdownMenuItem(
                                text = { Text(activity.name, color = Color.White) },
                                onClick = {
                                    selectedActivityId = activity.id
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank() && selectedDays.isNotEmpty() && selectedActivityId != null && isValidTime(startTime) && isValidTime(endTime)) {
                        val routineUpdate = RoutineUpdate(
                            name = name,
                            description = description.takeIf { it.isNotBlank() },
                            startTime = startTime,
                            endTime = endTime,
                            days = selectedDays.toList(),
                            activityId = selectedActivityId
                        )
                        onUpdate(routineUpdate)
                    }
                },
                enabled = !isUpdating && name.isNotBlank() && selectedDays.isNotEmpty() && selectedActivityId != null && isValidTime(startTime) && isValidTime(endTime)
            ) {
                if (isUpdating) CircularProgressIndicator(Modifier.size(20.dp), color = Color.White)
                else Text("Guardar", color = Color.White)
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