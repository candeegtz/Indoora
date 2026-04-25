package com.indoora.app.feature.activities

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.indoora.app.feature.auth.UiState
import com.indoora.app.ui.theme.indooraBackground
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivitiesScreen(
    viewModel: ActivitiesViewModel,
    homeId: Int,
    onNavigateBack: () -> Unit
) {
    val activitiesState by viewModel.activitiesState.collectAsState()
    val createState by viewModel.createState.collectAsState()
    val updateState by viewModel.updateState.collectAsState()
    val deleteState by viewModel.deleteState.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var editingActivity by remember { mutableStateOf<ActivityRead?>(null) }
    var deletingActivity by remember { mutableStateOf<ActivityRead?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(homeId) {
        println("🔄 Loading activities for home: $homeId") // Debug
        viewModel.loadActivities(homeId)
    }

    LaunchedEffect(createState, updateState, deleteState) {
        when {
            createState is UiState.Success || createState is UiState.Error -> {
                kotlinx.coroutines.delay(3000)
                viewModel.resetCreateState()
            }
            updateState is UiState.Success || updateState is UiState.Error -> {
                kotlinx.coroutines.delay(3000)
                viewModel.resetUpdateState()
            }
            deleteState is UiState.Success || deleteState is UiState.Error -> {
                kotlinx.coroutines.delay(3000)
                viewModel.resetDeleteState()
            }
        }
    }

    // ✅ Debug - Ver qué estado tiene
    LaunchedEffect(activitiesState) {
        println("📊 Activities state changed: $activitiesState")
        when (val state = activitiesState) {
            is UiState.Success -> println("   ✅ Success with ${state.data.size} activities")
            is UiState.Error -> println("   ❌ Error: ${state.message}")
            is UiState.Loading -> println("   ⏳ Loading...")
            else -> println("   ⚪ Idle")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Actividades",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Atrás",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                contentColor = MaterialTheme.colorScheme.onBackground,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nueva actividad")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .indooraBackground()
                .padding(paddingValues)
        ) {
            when (val state = activitiesState) {
                is UiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
                is UiState.Success -> {
                    val activities = state.data
                    if (activities.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "No hay actividades creadas",
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                                    fontSize = 16.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Toca + para crear una",
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                                    fontSize = 14.sp
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(activities, key = { it.id }) { activity ->
                                ActivityCard(
                                    activity = activity,
                                    onEditClick = { editingActivity = activity },
                                    onDeleteClick = {
                                        deletingActivity = activity
                                        showDeleteConfirm = true
                                    }
                                )
                            }
                        }
                    }
                }
                is UiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Error al cargar actividades",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = state.message,
                                color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.loadActivities(homeId) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                                )
                            ) {
                                Text("Reintentar")
                            }
                        }
                    }
                }
                else -> {
                    // Idle - Mostrar loading
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }

            // Diálogos
            if (showCreateDialog) {
                CreateActivityDialog(
                    viewModel = viewModel,
                    homeId = homeId,
                    onDismiss = { showCreateDialog = false }
                )
            }

            editingActivity?.let { activity ->
                EditActivityDialog(
                    viewModel = viewModel,
                    activity = activity,
                    homeId = homeId,
                    onDismiss = { editingActivity = null }
                )
            }

            if (showDeleteConfirm && deletingActivity != null) {
                AlertDialog(
                    onDismissRequest = {
                        showDeleteConfirm = false
                        deletingActivity = null
                    },
                    title = {
                        Text(
                            "Eliminar actividad",
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = {
                        Text(
                            "¿Estás seguro de que quieres eliminar \"${deletingActivity?.name}\"?",
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                deletingActivity?.let { act ->
                                    viewModel.deleteActivity(act.id, homeId)
                                }
                                showDeleteConfirm = false
                                deletingActivity = null
                            }
                        ) {
                            Text(
                                "Eliminar",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                showDeleteConfirm = false
                                deletingActivity = null
                            }
                        ) {
                            Text(
                                "Cancelar",
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                            )
                        }
                    },
                    containerColor = Color(0xFF4A4458), // Color sólido
                    shape = RoundedCornerShape(16.dp)
                )
            }

            // Mensajes de feedback
            val message = when {
                createState is UiState.Success -> "Actividad creada correctamente"
                createState is UiState.Error -> (createState as UiState.Error).message
                updateState is UiState.Success -> "Actividad actualizada correctamente"
                updateState is UiState.Error -> (updateState as UiState.Error).message
                deleteState is UiState.Success -> "Actividad eliminada correctamente"
                deleteState is UiState.Error -> (deleteState as UiState.Error).message
                else -> null
            }

            message?.let {
                val isError = createState is UiState.Error ||
                        updateState is UiState.Error ||
                        deleteState is UiState.Error

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isError) {
                            Color(0xFFCF6679)
                        } else {
                            Color(0xFF03DAC6)
                        }
                    ),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) {
                    Text(
                        text = it,
                        modifier = Modifier.padding(16.dp),
                        color = Color.Black,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun ActivityCard(
    activity: ActivityRead,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.2f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = activity.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                modifier = Modifier.weight(1f)
            )
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

@Composable
fun CreateActivityDialog(
    viewModel: ActivitiesViewModel,
    homeId: Int,
    onDismiss: () -> Unit
) {
    var activityName by remember { mutableStateOf("") }
    val roomsState by viewModel.roomsState.collectAsState()
    val positionsState by viewModel.positionsState.collectAsState()
    val createState by viewModel.createState.collectAsState()
    var selectedPositionIds by remember { mutableStateOf(emptySet<Int>()) }
    var expandedRoomId by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadRoomsAndPositions(homeId)
    }

    LaunchedEffect(createState) {
        if (createState is UiState.Success) onDismiss()
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.resetCreateState() }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva actividad", fontWeight = FontWeight.Bold, color = Color.White) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = activityName,
                    onValueChange = { activityName = it },
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

                Text(
                    text = "Posiciones donde ocurre:",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = Color.White
                )

                when (roomsState) {
                    is UiState.Loading -> {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Color.White)
                        }
                    }
                    is UiState.Success -> {
                        val rooms = (roomsState as UiState.Success).data
                        if (rooms.isEmpty()) {
                            Text("No hay habitaciones en esta casa", color = Color.White.copy(alpha = 0.6f))
                        } else {
                            LazyColumn(
                                modifier = Modifier.heightIn(max = 300.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(rooms) { room ->
                                    val isExpanded = expandedRoomId == room.id
                                    val positionsUi = positionsState[room.id]
                                    Column {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(
                                                    Color.White.copy(alpha = 0.2f),
                                                    RoundedCornerShape(12.dp)
                                                )
                                                .clickable { expandedRoomId = if (isExpanded) null else room.id }
                                                .padding(horizontal = 16.dp, vertical = 14.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                room.name,
                                                fontWeight = FontWeight.Medium,
                                                color = Color.White
                                            )
                                            Icon(
                                                if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                                contentDescription = if (isExpanded) "Colapsar" else "Expandir",
                                                tint = Color.White
                                            )
                                        }

                                        AnimatedVisibility(
                                            visible = isExpanded,
                                            enter = expandVertically(animationSpec = tween(200)) + fadeIn(),
                                            exit = shrinkVertically(animationSpec = tween(200)) + fadeOut()
                                        ) {
                                            Column(modifier = Modifier.padding(start = 16.dp, top = 8.dp)) {
                                                when (positionsUi) {
                                                    is UiState.Success -> {
                                                        val positions = positionsUi.data
                                                        if (positions.isEmpty()) {
                                                            Text(
                                                                "No hay posiciones en esta habitación",
                                                                fontSize = 12.sp,
                                                                color = Color.White.copy(alpha = 0.6f)
                                                            )
                                                        } else {
                                                            positions.forEach { pos ->
                                                                Row(
                                                                    modifier = Modifier.padding(vertical = 4.dp),
                                                                    verticalAlignment = Alignment.CenterVertically
                                                                ) {
                                                                    Checkbox(
                                                                        checked = selectedPositionIds.contains(pos.id),
                                                                        onCheckedChange = { isChecked ->
                                                                            selectedPositionIds = if (isChecked)
                                                                                selectedPositionIds + pos.id
                                                                            else
                                                                                selectedPositionIds - pos.id
                                                                        },
                                                                        colors = CheckboxDefaults.colors(
                                                                            checkedColor = Color.White,
                                                                            uncheckedColor = Color.White.copy(alpha = 0.5f)
                                                                        )
                                                                    )
                                                                    Text(pos.name, color = Color.White)
                                                                }
                                                            }
                                                        }
                                                    }
                                                    is UiState.Loading -> CircularProgressIndicator(color = Color.White)
                                                    else -> {}
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    is UiState.Error -> {
                        Text(
                            "Error: ${(roomsState as UiState.Error).message}",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    else -> {}
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (activityName.isNotBlank() && selectedPositionIds.isNotEmpty()) {
                        viewModel.createActivity(activityName, homeId, selectedPositionIds.toList())
                    }
                },
                enabled = activityName.isNotBlank() && selectedPositionIds.isNotEmpty() && createState !is UiState.Loading
            ) {
                if (createState is UiState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                } else {
                    Text("Crear", color = Color.White)
                }
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

@Composable
fun EditActivityDialog(
    viewModel: ActivitiesViewModel,
    activity: ActivityRead,
    homeId: Int,
    onDismiss: () -> Unit
) {
    var activityName by remember { mutableStateOf(activity.name) }
    val roomsState by viewModel.roomsState.collectAsState()
    val positionsState by viewModel.positionsState.collectAsState()
    val updateState by viewModel.updateState.collectAsState()
    var selectedPositionIds by remember { mutableStateOf(emptySet<Int>()) }
    var expandedRoomId by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadRoomsAndPositions(homeId)
    }

    LaunchedEffect(updateState) {
        if (updateState is UiState.Success) onDismiss()
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.resetUpdateState() }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar actividad", fontWeight = FontWeight.Bold, color = Color.White) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = activityName,
                    onValueChange = { activityName = it },
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

                Text(
                    text = "Posiciones asociadas:",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = Color.White
                )

                when (roomsState) {
                    is UiState.Loading -> {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Color.White)
                        }
                    }
                    is UiState.Success -> {
                        val rooms = (roomsState as UiState.Success).data
                        if (rooms.isEmpty()) {
                            Text("No hay habitaciones en esta casa", color = Color.White.copy(alpha = 0.6f))
                        } else {
                            LazyColumn(
                                modifier = Modifier.heightIn(max = 300.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(rooms) { room ->
                                    val isExpanded = expandedRoomId == room.id
                                    val positionsUi = positionsState[room.id]
                                    Column {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(
                                                    Color.White.copy(alpha = 0.2f),
                                                    RoundedCornerShape(12.dp)
                                                )
                                                .clickable { expandedRoomId = if (isExpanded) null else room.id }
                                                .padding(horizontal = 16.dp, vertical = 14.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                room.name,
                                                fontWeight = FontWeight.Medium,
                                                color = Color.White
                                            )
                                            Icon(
                                                if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                                contentDescription = if (isExpanded) "Colapsar" else "Expandir",
                                                tint = Color.White
                                            )
                                        }

                                        AnimatedVisibility(
                                            visible = isExpanded,
                                            enter = expandVertically(animationSpec = tween(200)) + fadeIn(),
                                            exit = shrinkVertically(animationSpec = tween(200)) + fadeOut()
                                        ) {
                                            Column(modifier = Modifier.padding(start = 16.dp, top = 8.dp)) {
                                                when (positionsUi) {
                                                    is UiState.Success -> {
                                                        val positions = positionsUi.data
                                                        if (positions.isEmpty()) {
                                                            Text(
                                                                "No hay posiciones en esta habitación",
                                                                fontSize = 12.sp,
                                                                color = Color.White.copy(alpha = 0.6f)
                                                            )
                                                        } else {
                                                            positions.forEach { pos ->
                                                                Row(
                                                                    modifier = Modifier.padding(vertical = 4.dp),
                                                                    verticalAlignment = Alignment.CenterVertically
                                                                ) {
                                                                    Checkbox(
                                                                        checked = selectedPositionIds.contains(pos.id),
                                                                        onCheckedChange = { isChecked ->
                                                                            selectedPositionIds = if (isChecked)
                                                                                selectedPositionIds + pos.id
                                                                            else
                                                                                selectedPositionIds - pos.id
                                                                        },
                                                                        colors = CheckboxDefaults.colors(
                                                                            checkedColor = Color.White,
                                                                            uncheckedColor = Color.White.copy(alpha = 0.5f)
                                                                        )
                                                                    )
                                                                    Text(pos.name, color = Color.White)
                                                                }
                                                            }
                                                        }
                                                    }
                                                    is UiState.Loading -> CircularProgressIndicator(color = Color.White)
                                                    else -> {}
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    is UiState.Error -> {
                        Text(
                            "Error: ${(roomsState as UiState.Error).message}",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    else -> {}
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (activityName.isNotBlank()) {
                        viewModel.updateActivity(activity.id, activityName, selectedPositionIds.toList(), homeId)
                    }
                },
                enabled = activityName.isNotBlank() && updateState !is UiState.Loading
            ) {
                if (updateState is UiState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                } else {
                    Text("Guardar", color = Color.White)
                }
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