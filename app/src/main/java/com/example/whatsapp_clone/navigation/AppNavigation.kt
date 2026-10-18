package com.example.whatsapp_clone.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.whatsapp_clone.presentation.auth.AuthViewModel
import com.example.whatsapp_clone.presentation.auth.OtpScreen
import com.example.whatsapp_clone.presentation.auth.PhoneNumberScreen
import com.example.whatsapp_clone.presentation.auth.ProfileSetupScreen
import com.example.whatsapp_clone.presentation.call.CallScreen
import com.example.whatsapp_clone.presentation.chat.ChatScreen
import com.example.whatsapp_clone.presentation.chat.NewChatScreen
import com.example.whatsapp_clone.presentation.home.HomeScreen
import com.example.whatsapp_clone.presentation.settings.SettingsScreen
import com.example.whatsapp_clone.presentation.splash.SplashScreen
import com.example.whatsapp_clone.presentation.welcome.WelcomeScreen

sealed class Screen(val route: String) {
    object SplashScreen : Screen("splash_screen")
    object WelcomeScreen : Screen("welcome_screen")
    object PhoneNumberScreen : Screen("phone_number_screen")
    object OtpScreen : Screen("otp_screen/{phoneNumber}") {
        fun createRoute(phoneNumber: String) = "otp_screen/${Uri.encode(phoneNumber)}"
    }
    object ProfileSetupScreen : Screen("profile_setup_screen")
    object HomeScreen : Screen("home_screen")
    object SettingsScreen : Screen("settings_screen")
    object NewChatScreen : Screen("new_chat_screen")
    object ChatScreen : Screen("chat_screen/{chatId}/{otherUserId}/{otherUserName}") {
        fun createRoute(chatId: String, otherUserId: String, otherUserName: String) =
            "chat_screen/${Uri.encode(chatId)}/${Uri.encode(otherUserId)}/${Uri.encode(otherUserName)}"
    }
    object CallScreen : Screen("call_screen/{contactName}/{isVideo}") {
        fun createRoute(contactName: String, isVideo: Boolean) =
            "call_screen/${Uri.encode(contactName)}/$isVideo"
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    // Shared AuthViewModel for the entire onboarding / login flow
    val authViewModel: AuthViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Screen.SplashScreen.route
    ) {
        // --- 1. Splash Screen ---
        composable(Screen.SplashScreen.route) {
            SplashScreen(onNavigateToNext = { isLoggedIn ->
                val nextDestination = if (isLoggedIn) Screen.HomeScreen.route else Screen.WelcomeScreen.route
                navController.navigate(nextDestination) {
                    popUpTo(Screen.SplashScreen.route) { inclusive = true }
                }
            })
        }

        // --- 2. Welcome Screen ---
        composable(Screen.WelcomeScreen.route) {
            WelcomeScreen(onAgreeAndContinue = {
                navController.navigate(Screen.PhoneNumberScreen.route)
            })
        }

        // --- 3. Phone Number Screen ---
        composable(Screen.PhoneNumberScreen.route) {
            PhoneNumberScreen(
                onNavigateToOtp = { fullPhoneNumber ->
                    navController.navigate(Screen.OtpScreen.createRoute(fullPhoneNumber))
                },
                viewModel = authViewModel
            )
        }

        // --- 4. OTP Verification Screen ---
        composable(
            route = Screen.OtpScreen.route,
            arguments = listOf(navArgument("phoneNumber") { type = NavType.StringType })
        ) { backStackEntry ->
            val phoneNumber = backStackEntry.arguments?.getString("phoneNumber") ?: ""
            OtpScreen(
                phoneNumber = phoneNumber,
                onNavigateBack = { navController.popBackStack() },
                onVerificationSuccess = {
                    navController.navigate(Screen.ProfileSetupScreen.route) {
                        popUpTo(Screen.PhoneNumberScreen.route) { inclusive = true }
                    }
                },
                viewModel = authViewModel
            )
        }

        // --- 5. Profile Setup Screen ---
        composable(Screen.ProfileSetupScreen.route) {
            ProfileSetupScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.HomeScreen.route) {
                        popUpTo(Screen.ProfileSetupScreen.route) { inclusive = true }
                    }
                },
                viewModel = authViewModel
            )
        }

        // --- 6. Home Screen (Chats, Status, Calls) ---
        composable(Screen.HomeScreen.route) {
            HomeScreen(
                onNavigateToChat = { chatId, otherUserId, otherUserName ->
                    navController.navigate(Screen.ChatScreen.createRoute(chatId, otherUserId, otherUserName))
                },
                onNavigateToNewChat = {
                    navController.navigate(Screen.NewChatScreen.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.SettingsScreen.route)
                },
                onSignedOut = {
                    navController.navigate(Screen.WelcomeScreen.route) {
                        popUpTo(Screen.HomeScreen.route) { inclusive = true }
                    }
                }
            )
        }

        // --- 7. Settings Screen ---
        composable(Screen.SettingsScreen.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onSignedOut = {
                    navController.navigate(Screen.WelcomeScreen.route) {
                        popUpTo(Screen.HomeScreen.route) { inclusive = true }
                    }
                }
            )
        }

        // --- 8. New Chat / Contact Picker Screen ---
        composable(Screen.NewChatScreen.route) {
            NewChatScreen(
                onNavigateBack = { navController.popBackStack() },
                onContactSelected = { chatId, otherUserId, otherUserName ->
                    navController.navigate(Screen.ChatScreen.createRoute(chatId, otherUserId, otherUserName)) {
                        popUpTo(Screen.NewChatScreen.route) { inclusive = true }
                    }
                }
            )
        }

        // --- 9. 1-on-1 Chat Screen ---
        composable(
            route = Screen.ChatScreen.route,
            arguments = listOf(
                navArgument("chatId") { type = NavType.StringType },
                navArgument("otherUserId") { type = NavType.StringType },
                navArgument("otherUserName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("chatId") ?: ""
            val otherUserId = backStackEntry.arguments?.getString("otherUserId") ?: ""
            val otherUserName = backStackEntry.arguments?.getString("otherUserName") ?: ""

            ChatScreen(
                chatId = chatId,
                otherUserId = otherUserId,
                otherUserName = otherUserName,
                onNavigateBack = { navController.popBackStack() },
                onStartCall = { contactName, isVideo ->
                    navController.navigate(Screen.CallScreen.createRoute(contactName, isVideo))
                }
            )
        }

        // --- 10. Voice & Video Call Screen ---
        composable(
            route = Screen.CallScreen.route,
            arguments = listOf(
                navArgument("contactName") { type = NavType.StringType },
                navArgument("isVideo") { type = NavType.BoolType }
            )
        ) { backStackEntry ->
            val contactName = backStackEntry.arguments?.getString("contactName") ?: ""
            val isVideo = backStackEntry.arguments?.getBoolean("isVideo") ?: false

            CallScreen(
                contactName = contactName,
                isVideoCall = isVideo,
                onEndCall = { navController.popBackStack() }
            )
        }
    }
}