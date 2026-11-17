package com.connectx.social.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.connectx.social.ui.screens.admin.AdminScreen
import com.connectx.social.ui.screens.auth.LoginScreen
import com.connectx.social.ui.screens.auth.RegisterScreen
import com.connectx.social.ui.screens.chat.ChatScreen
import com.connectx.social.ui.screens.chat.ConversationsScreen
import com.connectx.social.ui.screens.feed.FeedScreen
import com.connectx.social.ui.screens.profile.EditProfileScreen
import com.connectx.social.ui.screens.profile.ProfileScreen
import com.connectx.social.ui.screens.common.SplashScreen
import com.connectx.social.ui.viewmodel.AuthViewModel

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object Feed : Screen("feed")
    data object Profile : Screen("profile/{userId}") {
        fun create(userId: String) = "profile/$userId"
    }
    data object EditProfile : Screen("edit_profile")
    data object Conversations : Screen("conversations")
    data object Chat : Screen("chat/{conversationId}") {
        fun create(conversationId: String) = "chat/$conversationId"
    }
    data object Admin : Screen("admin")
}

@Composable
fun ConnectXNavHost() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()

    NavHost(navController = navController, startDestination = Screen.Splash.route) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onAuthenticated = {
                    navController.navigate(Screen.Feed.route) { popUpTo(Screen.Splash.route) { inclusive = true } }
                },
                onUnauthenticated = {
                    navController.navigate(Screen.Login.route) { popUpTo(Screen.Splash.route) { inclusive = true } }
                }
            )
        }
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Feed.route) { popUpTo(Screen.Login.route) { inclusive = true } }
                },
                onNavigateToRegister = { navController.navigate(Screen.Register.route) }
            )
        }
        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Screen.Feed.route) { popUpTo(Screen.Login.route) { inclusive = true } }
                },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }
        composable(Screen.Feed.route) {
            FeedScreen(
                onProfileClick = { userId -> navController.navigate(Screen.Profile.create(userId)) },
                onConversationsClick = { navController.navigate(Screen.Conversations.route) },
                onAdminClick = { navController.navigate(Screen.Admin.route) },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) { popUpTo(0) { inclusive = true } }
                }
            )
        }
        composable(route = Screen.Profile.route, arguments = listOf(navArgument("userId") { type = NavType.StringType })) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: return@composable
            ProfileScreen(
                userId = userId,
                onEditProfile = { navController.navigate(Screen.EditProfile.route) },
                onMessage = { convId -> navController.navigate(Screen.Chat.create(convId)) },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.EditProfile.route) {
            EditProfileScreen(onDone = { navController.popBackStack() })
        }
        composable(Screen.Conversations.route) {
            ConversationsScreen(
                onOpenChat = { convId -> navController.navigate(Screen.Chat.create(convId)) },
                onBack = { navController.popBackStack() }
            )
        }
        composable(route = Screen.Chat.route, arguments = listOf(navArgument("conversationId") { type = NavType.StringType })) { backStackEntry ->
            val convId = backStackEntry.arguments?.getString("conversationId") ?: return@composable
            ChatScreen(conversationId = convId, onBack = { navController.popBackStack() })
        }
        composable(Screen.Admin.route) {
            AdminScreen(onBack = { navController.popBackStack() })
        }
    }
}
