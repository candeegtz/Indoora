package com.indoora.app.feature.home

import HomeViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.indoora.app.data.model.EstadoConfig
import com.indoora.app.feature.auth.UiState
import com.indoora.app.ui.theme.indooraBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    homeId: Int,
    onNavigateToDeviceConfig: () -> Unit = {},
    onNavigateToSystemTraining: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToRoutines: () -> Unit = {}
) {
    val homeState by viewModel.homeState.collectAsState()
    val refreshTrigger by viewModel.refreshTrigger.collectAsState()

    LaunchedEffect(refreshTrigger) {
        viewModel.loadHome(homeId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Indoora",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        bottomBar = {
            BottomNavigationBar(
                onNavigateToHome = { /* ya estamos aquí */ },
                onNavigateToProfile = onNavigateToProfile,
                onNavigateToRoutines = onNavigateToRoutines,
                currentScreen = "home"
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .indooraBackground()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            when (homeState) {
                is UiState.Loading -> {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                is UiState.Success -> {
                    val home = (homeState as UiState.Success).data
                    NotificationCard(
                        estadoConfig = home.estadoConfig,
                        onNavigateToDeviceConfig = onNavigateToDeviceConfig,
                        onNavigateToSystemTraining = onNavigateToSystemTraining
                    )
                }

                is UiState.Error -> {
                    Text(
                        text = "Error al cargar el hogar",
                        color = MaterialTheme.colorScheme.error
                    )
                }

                else -> {}
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun NotificationCard(
    estadoConfig: EstadoConfig,
    onNavigateToDeviceConfig: () -> Unit,
    onNavigateToSystemTraining: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.15f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Notificaciones",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            when (estadoConfig) {

                EstadoConfig.NOT_CONFIG -> {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        modifier = Modifier.size(48.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Configura tus dispositivos",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Para empezar a usar Indoora, necesitas configurar los dispositivos de tu hogar.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onNavigateToDeviceConfig,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "Configurar dispositivos",
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                EstadoConfig.ONLY_DEVICES_CONFIG -> {
                    Icon(
                        imageVector = Icons.Default.List,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        modifier = Modifier.size(48.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Entrena el sistema",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Completa el entrenamiento del motor Indoor para comenzar a detectar actividades.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onNavigateToSystemTraining,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "Iniciar entrenamiento",
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                EstadoConfig.CONFIG_COMPLETED -> {
                    Text(
                        text = "Sin notificaciones",
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomNavigationBar(
    onNavigateToHome: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToRoutines: () -> Unit,
    currentScreen: String
) {
    NavigationBar(
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) {
        NavigationBarItem(
            icon = {
                Icon(Icons.Default.Home, contentDescription = "Inicio")
            },
            label = { Text("Inicio") },
            selected = currentScreen == "home",
            onClick = onNavigateToHome,
            colors = navBarItemColors()
        )

        NavigationBarItem(
            icon = {
                Icon(Icons.Default.List, contentDescription = "Rutinas")
            },
            label = { Text("Rutinas") },
            selected = currentScreen == "routines",
            onClick = onNavigateToRoutines,
            colors = navBarItemColors()
        )

        NavigationBarItem(
            icon = {
                Icon(Icons.Default.AccountCircle, contentDescription = "Perfil")
            },
            label = { Text("Perfil") },
            selected = currentScreen == "profile",
            onClick = onNavigateToProfile,
            colors = navBarItemColors()
        )
    }
}

@Composable
private fun navBarItemColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = MaterialTheme.colorScheme.onBackground,
    selectedTextColor = MaterialTheme.colorScheme.onBackground,
    unselectedIconColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
    unselectedTextColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
    indicatorColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
)