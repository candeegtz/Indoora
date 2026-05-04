package com.indoora.app.feature.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.indoora.app.data.model.UserUpdate
import com.indoora.app.feature.auth.UiState
import com.indoora.app.ui.theme.indooraBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val userState by viewModel.userState.collectAsState()
    val updateState by viewModel.updateState.collectAsState()

    var username by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var surnames by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var userId by remember { mutableIntStateOf(0) }

    var showSuccessDialog by remember { mutableStateOf(false) }

    // Cargar usuario al iniciar
    LaunchedEffect(Unit) {
        viewModel.loadCurrentUser()
    }

    // Cargar datos del usuario cuando lleguen
    LaunchedEffect(userState) {
        if (userState is UiState.Success) {
            val user = (userState as UiState.Success).data
            userId = user.id
            username = user.username
            name = user.name
            surnames = user.surnames
            email = user.email
        }
    }

    // Mostrar diálogo de éxito
    LaunchedEffect(updateState) {
        if (updateState is UiState.Success) {
            showSuccessDialog = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Mi Perfil",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
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
            Spacer(modifier = Modifier.height(24.dp))

            // Avatar
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "Avatar",
                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                modifier = Modifier.size(80.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            when (userState) {
                is UiState.Loading -> {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                is UiState.Success -> {
                    val user = (userState as UiState.Success).data
                    Text(
                        text = user.userType.replace("_", " "),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                }

                is UiState.Error -> {
                    Text(
                        text = "Error al cargar datos",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 13.sp
                    )
                }

                else -> {}
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Formulario
            ProfileField(
                label = "Nombre de usuario",
                value = username,
                onValueChange = { username = it },
                placeholder = "usuario123"
            )

            Spacer(modifier = Modifier.height(16.dp))

            ProfileField(
                label = "Nombre",
                value = name,
                onValueChange = { name = it },
                placeholder = "Juan"
            )

            Spacer(modifier = Modifier.height(16.dp))

            ProfileField(
                label = "Apellidos",
                value = surnames,
                onValueChange = { surnames = it },
                placeholder = "Pérez García"
            )

            Spacer(modifier = Modifier.height(16.dp))

            ProfileField(
                label = "Email",
                value = email,
                onValueChange = { email = it },
                placeholder = "email@example.com"
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Cambiar contraseña
            Text(
                text = "Cambiar contraseña (opcional)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            ProfileField(
                label = "Nueva contraseña",
                value = password,
                onValueChange = { password = it },
                placeholder = "Dejar vacío para no cambiar",
                isPassword = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            ProfileField(
                label = "Confirmar contraseña",
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                placeholder = "Repetir contraseña",
                isPassword = true
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Botón guardar
            Button(
                onClick = {
                    // Validar contraseñas si se quieren cambiar
                    if (password.isNotEmpty() && password != confirmPassword) {
                        return@Button
                    }

                    val updateData = UserUpdate(
                        username = if (username.isNotEmpty()) username else null,
                        name = if (name.isNotEmpty()) name else null,
                        surnames = if (surnames.isNotEmpty()) surnames else null,
                        email = if (email.isNotEmpty()) email else null,
                        password = if (password.isNotEmpty()) password else null
                    )

                    viewModel.updateUser(userId, updateData)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = updateState !is UiState.Loading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (updateState is UiState.Loading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        "Guardar cambios",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Mensajes de error/validación
            if (password.isNotEmpty() && password != confirmPassword) {
                Text(
                    text = "Las contraseñas no coinciden",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            }

            when (updateState) {
                is UiState.Error -> {
                    Text(
                        text = (updateState as UiState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
                else -> {}
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Diálogo de éxito
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                viewModel.resetUpdateState()
                onNavigateBack()
            },
            title = {
                Text("Perfil actualizado")
            },
            text = {
                Text("Los cambios se han guardado correctamente.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSuccessDialog = false
                        viewModel.resetUpdateState()
                        onNavigateBack()
                    }
                ) {
                    Text("Aceptar")
                }
            }
        )
    }
}

@Composable
private fun ProfileField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isPassword: Boolean = false
) {
    Column {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    placeholder,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                    fontSize = 14.sp
                )
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                focusedBorderColor = MaterialTheme.colorScheme.onBackground,
                unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                cursorColor = MaterialTheme.colorScheme.onBackground
            ),
            singleLine = true,
            visualTransformation = if (isPassword) PasswordVisualTransformation()
            else androidx.compose.ui.text.input.VisualTransformation.None
        )
    }
}