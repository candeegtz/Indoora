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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.indoora.app.data.model.PositionCreate
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
    val subjectFormData = remember { SubjectFormData() }
    val rooms = remember { mutableStateListOf<RoomData>() }
    val positionsByRoom = remember { mutableStateMapOf<RoomData, MutableList<PositionData>>() }

    val registerState by viewModel.registerAndLoginState.collectAsState()
    val createSubjectState by viewModel.createSubjectState.collectAsState()
    val createRoomsState by viewModel.createRoomsState.collectAsState()
    val createPositionsState by viewModel.createPositionsState.collectAsState()

    // Después de registrarse
    LaunchedEffect(registerState) {
        if (registerState is UiState.Success) {
            if (isSupervisorCreator) {
                step = RegisterStep.CREATE_SUBJECT
            } else {
                onRegisterSuccess()
            }
        }
    }

    // Después de crear el subject → ir a añadir rooms
    // A partir de aquí no se puede volver atrás (paso irreversible)
    LaunchedEffect(createSubjectState) {
        if (createSubjectState is UiState.Success) {
            step = RegisterStep.ADD_ROOMS
        }
    }

    // Cuando las rooms se crean → crear positions (o marcar como completo si no hay)
    LaunchedEffect(createRoomsState) {
        if (createRoomsState is UiState.Success) {
            val createdRooms = (createRoomsState as UiState.Success).data
            val roomMap = createdRooms.associateBy { it.name }
            val allPositions = mutableListOf<PositionCreate>()

            positionsByRoom.forEach { (localRoom, positions) ->
                val createdRoom = roomMap[localRoom.name]
                if (createdRoom != null) {
                    positions.forEach { position ->
                        if (position.name.isNotBlank()) {
                            allPositions.add(
                                PositionCreate(
                                    name = position.name,
                                    roomId = createdRoom.id
                                )
                            )
                        }
                    }
                }
            }

            if (allPositions.isNotEmpty()) {
                viewModel.createPositionsFromList(allPositions)
            } else {
                viewModel.markPositionsAsComplete()
            }
        }
    }

    // Cuando rooms Y positions están listas → navegar al Home
    LaunchedEffect(createRoomsState, createPositionsState) {
        if (createRoomsState is UiState.Success && createPositionsState is UiState.Success) {
            onRegisterSuccess()
        }
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
                    RegisterStep.CREATE_SUBJECT -> {}
                    RegisterStep.ADD_ROOMS -> {}
                    RegisterStep.ADD_POSITIONS -> step = RegisterStep.ADD_ROOMS
                    RegisterStep.CONFIRM_SETUP -> step = RegisterStep.ADD_POSITIONS
                }
            }
        )

        Spacer(modifier = Modifier.height(32.dp))
        StepIndicator(currentStep = step, isSupervisorCreator = isSupervisorCreator)
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
                    onSubmit = {
                        viewModel.registerAndLogin(
                            formData.toUserCreate(isSupervisorCreator)
                        )
                    }
                )

                RegisterStep.CREATE_SUBJECT -> CreateSubjectStep(
                    subjectFormData = subjectFormData,
                    createSubjectState = createSubjectState,
                    onSubmit = {
                        viewModel.createSubject(subjectFormData.toUserCreate())
                    }
                )

                RegisterStep.ADD_ROOMS -> AddRoomsStep(
                    homeName = formData.homeName,
                    rooms = rooms,
                    onAddRoom = { rooms.add(RoomData()) },
                    onRemoveRoom = { room ->
                        positionsByRoom.remove(room)
                        rooms.remove(room)
                    },
                    createRoomsState = UiState.Idle,
                    onContinue = {
                        rooms.forEach { room ->
                            if (!positionsByRoom.containsKey(room)) {
                                positionsByRoom[room] = mutableStateListOf()
                            }
                        }
                        step = RegisterStep.ADD_POSITIONS
                    },
                    onBack = {} // bloqueado — paso irreversible
                )

                RegisterStep.ADD_POSITIONS -> AddPositionsStep(
                    rooms = rooms,
                    positionsByRoom = positionsByRoom,
                    onContinue = { step = RegisterStep.CONFIRM_SETUP },
                    onBack = { step = RegisterStep.ADD_ROOMS }
                )

                RegisterStep.CONFIRM_SETUP -> {
                    val isLoading = createRoomsState is UiState.Loading ||
                            createPositionsState is UiState.Loading

                    ConfirmSetupStep(
                        homeName = formData.homeName,
                        rooms = rooms,
                        positionsByRoom = positionsByRoom,
                        isLoading = isLoading,
                        onConfirm = {
                            // El backend asigna el homeId automáticamente según el usuario autenticado
                            viewModel.createRooms(rooms.map { it.toRoomCreate() })
                        },
                        onBack = { step = RegisterStep.ADD_POSITIONS }
                    )
                }
            }
        }

        if (step == RegisterStep.CHOOSE_TYPE ||
            step == RegisterStep.FILL_FORM ||
            step == RegisterStep.HOME_SETUP
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            TextButton(onClick = onNavigateToLogin) {
                Text(
                    "¿Ya tienes cuenta? Inicia sesión",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}