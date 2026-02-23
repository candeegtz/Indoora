package com.indoora.app.feature.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.indoora.app.data.model.UserCreate
import com.indoora.app.ui.theme.indooraBackground

private enum class RegisterStep {
    CHOOSE_TYPE,
    FILL_FORM,
    HOME_SETUP
}

@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateBack: () -> Unit = {}
) {
    var step by remember { mutableStateOf(RegisterStep.CHOOSE_TYPE) }
    var isSupervisorCreator by remember { mutableStateOf(true) }

    var username by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var surnames by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var homeName by remember { mutableStateOf("") }
    var subjectUsername by remember { mutableStateOf("") }

    val registerAndLoginState by viewModel.registerAndLoginState.collectAsState()

    LaunchedEffect(registerAndLoginState) {
        if (registerAndLoginState is UiState.Success) {
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

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    when (step) {
                        RegisterStep.CHOOSE_TYPE -> onNavigateBack()
                        RegisterStep.FILL_FORM -> step = RegisterStep.CHOOSE_TYPE
                        RegisterStep.HOME_SETUP -> step = RegisterStep.FILL_FORM
                    }
                }
            ) {
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

        Spacer(modifier = Modifier.height(32.dp))

        StepIndicator(currentStep = step)

        Spacer(modifier = Modifier.height(32.dp))

        AnimatedContent(
            targetState = step,
            transitionSpec = {
                slideInHorizontally(
                    animationSpec = tween(300),
                    initialOffsetX = {
                        if (targetState.ordinal > initialState.ordinal) it else -it
                    }
                ) togetherWith slideOutHorizontally(
                    animationSpec = tween(300),
                    targetOffsetX = {
                        if (targetState.ordinal > initialState.ordinal) -it else it
                    }
                )
            },
            label = "step_transition"
        ) { currentStep ->
            when (currentStep) {
                RegisterStep.CHOOSE_TYPE -> {
                    ChooseTypeStep(
                        onSelectType = { isCreator ->
                            isSupervisorCreator = isCreator
                            step = RegisterStep.FILL_FORM
                        }
                    )
                }

                RegisterStep.FILL_FORM -> {
                    FormStep(
                        username = username,
                        onUsernameChange = { username = it },
                        name = name,
                        onNameChange = { name = it },
                        surnames = surnames,
                        onSurnamesChange = { surnames = it },
                        email = email,
                        onEmailChange = { email = it },
                        password = password,
                        onPasswordChange = { password = it },
                        registerState = registerAndLoginState,
                        onNext = { step = RegisterStep.HOME_SETUP }
                    )
                }

                RegisterStep.HOME_SETUP -> {
                    HomeSetupStep(
                        isSupervisorCreator = isSupervisorCreator,
                        homeName = homeName,
                        onHomeNameChange = { homeName = it },
                        subjectUsername = subjectUsername,
                        onSubjectUsernameChange = { subjectUsername = it },
                        registerState = registerAndLoginState,
                        onSubmit = {
                            val userCreate = UserCreate(
                                username = username.trim(),
                                name = name.trim(),
                                surnames = surnames.trim(),
                                email = email.trim(),
                                password = password,
                                userType = if (isSupervisorCreator) "SUPERVISOR_CREATOR" else "SUPERVISOR",
                                homeName = if (isSupervisorCreator) homeName.trim() else null,
                                subjectUsername = if (!isSupervisorCreator) subjectUsername.trim() else null
                            )
                            viewModel.registerAndLogin(userCreate)
                        }
                    )
                }
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

@Composable
private fun StepIndicator(currentStep: RegisterStep) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        StepDot(
            active = currentStep.ordinal >= RegisterStep.CHOOSE_TYPE.ordinal,
            label = "Tipo"
        )
        HorizontalDivider(
            modifier = Modifier.width(32.dp),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
            thickness = 1.dp
        )
        StepDot(
            active = currentStep.ordinal >= RegisterStep.FILL_FORM.ordinal,
            label = "Datos"
        )
        HorizontalDivider(
            modifier = Modifier.width(32.dp),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
            thickness = 1.dp
        )
        StepDot(
            active = currentStep.ordinal >= RegisterStep.HOME_SETUP.ordinal,
            label = "Hogar"
        )
    }
}

@Composable
private fun StepDot(active: Boolean, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(RoundedCornerShape(50))
                .background(
                    if (active) MaterialTheme.colorScheme.onBackground
                    else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = if (active) MaterialTheme.colorScheme.onBackground
            else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            fontSize = 11.sp
        )
    }
}

@Composable
private fun ChooseTypeStep(
    onSelectType: (isSupervisorCreator: Boolean) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "¿Tienes ya una casa\ncreada en Indoora?",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        TypeOptionCard(
            icon = Icons.Default.Home,
            title = "No, crear una nueva casa",
            description = "Serás el supervisor principal y crearás el hogar",
            onClick = { onSelectType(true) }
        )

        TypeOptionCard(
            icon = Icons.Default.Person,
            title = "Sí, ya existe una casa",
            description = "Te unirás como supervisor de un sujeto existente",
            onClick = { onSelectType(false) }
        )
    }
}

@Composable
private fun TypeOptionCard(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.15f)
        )
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(32.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun FormStep(
    username: String, onUsernameChange: (String) -> Unit,
    name: String, onNameChange: (String) -> Unit,
    surnames: String, onSurnamesChange: (String) -> Unit,
    email: String, onEmailChange: (String) -> Unit,
    password: String, onPasswordChange: (String) -> Unit,
    registerState: UiState<*>,
    onNext: () -> Unit
) {
    val isFormValid = username.isNotBlank()
            && name.isNotBlank()
            && surnames.isNotBlank()
            && email.isNotBlank()
            && password.length >= 6

    Column(modifier = Modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF8B7AB8).copy(alpha = 0.25f)
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                RegisterField(label = "Nombre de usuario", value = username, onValueChange = onUsernameChange, placeholder = "usuario123", enabled = true)
                RegisterField(label = "Nombre", value = name, onValueChange = onNameChange, placeholder = "María", enabled = true)
                RegisterField(label = "Apellidos", value = surnames, onValueChange = onSurnamesChange, placeholder = "García López", enabled = true)
                RegisterField(label = "Email", value = email, onValueChange = onEmailChange, placeholder = "maria@gmail.com", enabled = true)
                RegisterField(label = "Contraseña", value = password, onValueChange = onPasswordChange, placeholder = "mínimo 6 caracteres", enabled = true, isPassword = true)

                if (registerState is UiState.Error) {
                    val errorMessage = (registerState as UiState.Error).message
                    if (errorMessage.contains("username", ignoreCase = true) ||
                        errorMessage.contains("email", ignoreCase = true) ||
                        errorMessage.contains("password", ignoreCase = true) ||
                        errorMessage.contains("already", ignoreCase = true) ||
                        errorMessage.contains("400")
                    ) {
                        Text(
                            text = when {
                                errorMessage.contains("already") -> "El email o usuario ya está registrado"
                                errorMessage.contains("400") -> "Datos incorrectos. Comprueba los campos."
                                else -> "Error en los datos del usuario"
                            },
                            color = Color(0xFFFFCDD2),
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                disabledContainerColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.15f)
            ),
            shape = RoundedCornerShape(12.dp),
            enabled = isFormValid
        ) {
            Text("Continuar", color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        }
    }
}

@Composable
private fun HomeSetupStep(
    isSupervisorCreator: Boolean,
    homeName: String, onHomeNameChange: (String) -> Unit,
    subjectUsername: String, onSubjectUsernameChange: (String) -> Unit,
    registerState: UiState<*>,
    onSubmit: () -> Unit
) {
    val isLoading = registerState is UiState.Loading
    val isFormValid = if (isSupervisorCreator) homeName.isNotBlank() else subjectUsername.isNotBlank()

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = if (isSupervisorCreator) "Crea tu hogar" else "Únete al hogar",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF8B7AB8).copy(alpha = 0.25f)
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (isSupervisorCreator) {
                    RegisterField(
                        label = "Nombre de la casa",
                        value = homeName,
                        onValueChange = onHomeNameChange,
                        placeholder = "Casa García",
                        enabled = !isLoading
                    )
                    Text(
                        text = "Este será el nombre de tu hogar en Indoora",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )
                } else {
                    RegisterField(
                        label = "Username del sujeto",
                        value = subjectUsername,
                        onValueChange = onSubjectUsernameChange,
                        placeholder = "Username del sujeto existente",
                        enabled = !isLoading
                    )
                    Text(
                        text = "Introduce el nombre de usuario del sujeto al que deseas supervisar",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )
                }

                // Mostrar TODOS los errores en este paso
                if (registerState is UiState.Error) {
                    val errorMessage = (registerState as UiState.Error).message
                    Text(
                        text = when {
                            errorMessage.contains("Subject not found", ignoreCase = true) ->
                                "No se encontró el sujeto indicado"
                            errorMessage.contains("already", ignoreCase = true) ->
                                "El usuario o email ya existe"
                            errorMessage.contains("connect", ignoreCase = true) ->
                                "No se puede conectar al servidor"
                            errorMessage.contains("404") ->
                                "No se encontró el sujeto"
                            errorMessage.contains("400") ->
                                "Datos incorrectos. Verifica la información."
                            else -> "Error: ${errorMessage}"
                        },
                        color = Color(0xFFFFCDD2),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onSubmit,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                disabledContainerColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.15f)
            ),
            shape = RoundedCornerShape(12.dp),
            enabled = isFormValid && !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text("Crear cuenta", color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
private fun RegisterField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    enabled: Boolean,
    isPassword: Boolean = false
) {
    Column {
        Text(text = label, color = MaterialTheme.colorScheme.onBackground, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f), fontSize = 14.sp) },
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
            visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None
        )
    }
}