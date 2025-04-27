package dev.android.simplify.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dev.android.simplify.presentation.auth.forgot_password.ForgotPasswordScreen
import dev.android.simplify.presentation.auth.login.LoginScreen
import dev.android.simplify.presentation.auth.register.RegisterScreen
import dev.android.simplify.presentation.chat.home.ChatHomeScreen
import dev.android.simplify.presentation.home.TemporaryChatHome

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Routes.LOGIN
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        composable(Routes.LOGIN) {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(Routes.REGISTER)
                },
                onNavigateToForgotPassword = {
                    navController.navigate(Routes.FORGOT_PASSWORD)
                },
                onLoginSuccess = {
                    navController.navigate(Routes.CHAT_HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                onNavigateToLogin = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.REGISTER) { inclusive = true }
                    }
                },
                onRegistrationSuccess = {
                    navController.navigate(Routes.CHAT_HOME) {
                        popUpTo(Routes.REGISTER) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.FORGOT_PASSWORD) {
            ForgotPasswordScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Chat screens
        composable(Routes.CHAT_HOME) {
            ChatHomeScreen(
                onNavigateToChat = { chatId ->
                    navController.navigate("${Routes.CHAT}/$chatId")
                },
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.CHAT_HOME) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = "${Routes.CHAT}/{chatId}",
            arguments = listOf(navArgument("chatId") { type = NavType.StringType })
        ) { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("chatId") ?: ""
            ChatScreen(
                chatId = chatId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}