package com.indoora.app.feature.splash

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.indoora.app.network.RetrofitClient
import com.indoora.app.network.TokenManager
import com.indoora.app.ui.theme.AnekTamil
import com.indoora.app.ui.theme.indooraBackground
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToHome: (Int) -> Unit   // ← NUEVO: callback para ir directamente a Home
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            val token = TokenManager.getAccessToken(context)
            if (!token.isNullOrEmpty()) {
                // Restaurar token en RetrofitClient
                RetrofitClient.setToken(token)
                try {
                    // Verificar que el token es válido
                    val response = RetrofitClient.api.getMe()
                    if (response.isSuccessful && response.body() != null) {
                        val homeId = response.body()!!.homeId ?: 0
                        onNavigateToHome(homeId)
                        return@launch
                    } else {
                        TokenManager.clearTokens(context)
                        RetrofitClient.setToken(null)
                    }
                } catch (e: Exception) {
                    // Error de red, limpiar token
                    TokenManager.clearTokens(context)
                    RetrofitClient.setToken(null)
                }
            }
            isLoading = false
        }
    }

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize().indooraBackground(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .indooraBackground()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "Bienvenido\na Indoora",
                fontFamily = AnekTamil,
                fontSize = 45.sp,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onNavigateToRegister,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                        contentColor = MaterialTheme.colorScheme.onBackground
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Registrarse")
                }

                Button(
                    onClick = onNavigateToLogin,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                        contentColor = MaterialTheme.colorScheme.onBackground
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Iniciar sesión")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}