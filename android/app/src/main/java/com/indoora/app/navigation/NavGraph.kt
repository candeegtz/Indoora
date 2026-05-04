package com.indoora.app.navigation

import HomeViewModel
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.indoora.app.data.repository.ActivityRepository
import androidx.navigation.navArgument
import com.indoora.app.data.repository.AuthRepository
import com.indoora.app.data.repository.HomeRepository
import com.indoora.app.data.repository.RoutineRepository
import com.indoora.app.feature.activities.ActivitiesScreen
import com.indoora.app.feature.activities.ActivitiesViewModel
import com.indoora.app.feature.auth.AuthViewModel
import com.indoora.app.feature.auth.AuthViewModelFactory
import com.indoora.app.feature.auth.LoginScreen
import com.indoora.app.feature.auth.RegisterScreen
import com.indoora.app.feature.deviceconfig.DeviceConfigScreen
import com.indoora.app.feature.home.HomeScreen
import com.indoora.app.feature.home.HomeViewModelFactory
import com.indoora.app.feature.profile.ProfileScreen
import com.indoora.app.feature.profile.ProfileViewModel
import com.indoora.app.feature.profile.ProfileViewModelFactory
import com.indoora.app.feature.routines.RoutinesScreen
import com.indoora.app.feature.routines.RoutinesViewModel
import com.indoora.app.feature.splash.SplashScreen
import com.indoora.app.feature.training.TrainingScreen
import com.indoora.app.feature.training.TrainingViewModel
import com.indoora.app.feature.training.TrainingViewModelFactory
import com.indoora.app.network.mqtt.MqttManager

sealed class Screen(val route: String) {
    object Splash          : Screen("splash")
    object Login           : Screen("login")
    object Register        : Screen("register")
    object Home            : Screen("home/{homeId}") {
        fun createRoute(homeId: Int) = "home/$homeId"
    }
    object DeviceConfig    : Screen("device_config/{homeId}") {
        fun createRoute(homeId: Int) = "device_config/$homeId"
    }
    object SystemTraining  : Screen("training/{homeId}") {
        fun createRoute(homeId: Int) = "training/$homeId"
    }
    object Profile         : Screen("profile")
    object Routines : Screen("routines/{homeId}") {
        fun createRoute(homeId: Int) = "routines/$homeId"
    }

    object Activities : Screen("activities/{homeId}") {
        fun createRoute(homeId: Int) = "activities/$homeId"
    }
}

@Composable
fun NavGraph(navController: NavHostController = rememberNavController()) {
    val context = LocalContext.current
    val authRepository = AuthRepository(context)
    val homeRepository = HomeRepository()

    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(authRepository)
    )

    val profileViewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModelFactory(authRepository)
    )

    // Crear el MqttManager (se conectará a continuación)
    val mqttManager = MqttManager(
        serverUri = "tcp://192.168.0.18:1883",   // Cambia por la IP de tu broker
        clientId = "android_app_${System.currentTimeMillis()}"
    )

    // Referencia para poder avisar al TrainingViewModel cuando la conexión esté lista
    var trainingViewModel: TrainingViewModel? = null

    // Iniciar conexión MQTT (asíncrona)
    mqttManager.connect(username = "", password = "") {
        // Cuando la conexión se complete, si ya existe el ViewModel, se lo notificamos
        trainingViewModel?.onMqttConnected()
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = { homeId ->
                    navController.navigate(Screen.Home.createRoute(homeId)) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    authViewModel.resetRegisterState()
                    navController.navigate(Screen.Register.route)
                },
                onNavigateBack = {
                    navController.navigate(Screen.Splash.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                viewModel = authViewModel,
                onRegisterSuccess = { homeId ->
                    navController.navigate(Screen.Home.createRoute(homeId)) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    authViewModel.resetRegisterState()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.navigate(Screen.Splash.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) { backStackEntry ->
            val homeId = backStackEntry.arguments?.getString("homeId")?.toIntOrNull() ?: 0
            val homeViewModel: HomeViewModel = viewModel(
                factory = HomeViewModelFactory(homeRepository)
            )
            HomeScreen(
                viewModel = homeViewModel,
                homeId = homeId,
                onNavigateToDeviceConfig = {
                    navController.navigate(Screen.DeviceConfig.createRoute(homeId))
                },
                onNavigateToSystemTraining = {
                    navController.navigate(Screen.SystemTraining.createRoute(homeId))
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                },
                onNavigateToRoutines = {
                    navController.navigate(Screen.Routines.createRoute(homeId))
                },
                onNavigateToActivities = { navController.navigate(Screen.Activities.createRoute(homeId)) }
            )
        }

        composable(Screen.DeviceConfig.route) { backStackEntry ->
            val homeId = backStackEntry.arguments?.getString("homeId")?.toIntOrNull() ?: 0
            DeviceConfigScreen(
                homeId = homeId,
                onNavigateBack = { navController.popBackStack() },
                homeRepository = homeRepository
            )
        }

        composable(Screen.Activities.route) { backStackEntry ->
            val homeId = backStackEntry.arguments?.getString("homeId")?.toIntOrNull() ?: 0
            val activityRepository = ActivityRepository()
            val homeRepository = HomeRepository()
            val viewModel = ActivitiesViewModel(activityRepository, homeRepository, homeId)
            ActivitiesScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Activities.route) { backStackEntry ->
            val homeId = backStackEntry.arguments?.getString("homeId")?.toIntOrNull() ?: 0
            val activityRepository = ActivityRepository()
            val homeRepository = HomeRepository()
            val viewModel = ActivitiesViewModel(activityRepository, homeRepository, homeId)
            ActivitiesScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.SystemTraining.route) { backStackEntry ->
            val homeId = backStackEntry.arguments?.getString("homeId")?.toIntOrNull() ?: 0
            val vm: TrainingViewModel = viewModel(
                factory = TrainingViewModelFactory(homeRepository, mqttManager)
            )
            trainingViewModel = vm
            // Si la conexión MQTT ya está lista, avisamos al ViewModel inmediatamente
            if (mqttManager.isConnected) {
                vm.onMqttConnected()
            }
            TrainingScreen(
                homeId = homeId,
                onNavigateBack = { navController.popBackStack() },
                viewModel = vm
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                viewModel = profileViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Routines.route) { backStackEntry ->
            val homeId = backStackEntry.arguments?.getString("homeId")?.toIntOrNull() ?: 0
            val routineRepository = RoutineRepository()
            val viewModel = RoutinesViewModel(routineRepository, homeId)
            RoutinesScreen(
                viewModel = viewModel,
                homeId = homeId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}