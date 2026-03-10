package com.alphacity.stamptour.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.alphacity.stamptour.ui.screen.ForgotPasswordScreen
import com.alphacity.stamptour.ui.screen.LoginScreen
import com.alphacity.stamptour.ui.screen.RegisterScreen
import com.alphacity.stamptour.ui.screen.MainScreen
import com.alphacity.stamptour.ui.screen.SplashScreen

object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val FORGOT_PASSWORD = "forgot_password"
    const val HOME = "home"
}

@Composable
fun AppNavigation(deepLinkProgramId: Int? = null) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = if (deepLinkProgramId != null) Routes.HOME else Routes.SPLASH,
    ) {
        composable(Routes.SPLASH) {
            SplashScreen(
                onStartClick = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }
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
                    navController.navigate(Routes.REGISTER)
                },
            )
        }
    }
}
