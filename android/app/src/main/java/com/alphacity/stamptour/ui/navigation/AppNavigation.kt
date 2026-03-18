package com.alphacity.stamptour.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.alphacity.stamptour.network.TokenManager
import com.alphacity.stamptour.ui.screen.ForgotPasswordScreen
import com.alphacity.stamptour.ui.screen.LoginScreen
import com.alphacity.stamptour.ui.screen.RegisterScreen
import com.alphacity.stamptour.ui.screen.MainScreen
import com.alphacity.stamptour.ui.screen.SplashScreen
import androidx.compose.ui.platform.LocalContext

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val FORGOT_PASSWORD = "forgot_password"
    const val HOME = "home"
}

@Composable
fun AppNavigation(deepLinkProgramId: Int? = null) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val tokenManager = TokenManager(context)
    val isLoggedIn = tokenManager.isLoggedIn
    var showSplash by rememberSaveable { mutableStateOf(!isLoggedIn) }

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = if (isLoggedIn) Routes.HOME else Routes.LOGIN,
        ) {
            composable(Routes.LOGIN) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    },
                    onGuestClick = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    },
                    onRegisterClick = {
                        navController.navigate(Routes.REGISTER)
                    },
                    onForgotPasswordClick = {
                        navController.navigate(Routes.FORGOT_PASSWORD)
                    },
                )
            }
            composable(Routes.REGISTER) {
                RegisterScreen(
                    onRegisterSuccess = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    },
                    onBackClick = {
                        navController.popBackStack()
                    },
                )
            }
            composable(Routes.FORGOT_PASSWORD) {
                ForgotPasswordScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                )
            }
            composable(Routes.HOME) {
                MainScreen(
                    deepLinkProgramId = deepLinkProgramId,
                    onLogout = {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.HOME) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.HOME) { inclusive = true }
                        }
                    },
                )
            }
        }

        if (showSplash && deepLinkProgramId == null) {
            SplashScreen(
                onStartClick = { showSplash = false }
            )
        }
    }
}
