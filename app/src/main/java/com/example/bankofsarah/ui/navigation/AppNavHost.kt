package com.example.bankofsarah.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.bankofsarah.data.local.TokenStore
import com.example.bankofsarah.ui.accounts.AccountsScreen
import com.example.bankofsarah.ui.login.LoginScreen
import kotlinx.serialization.Serializable

@Serializable
object LoginRoute

@Serializable
object AccountsRoute

@Composable
fun AppNavHost(
    tokenStore: TokenStore,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()
    val token by tokenStore.token.collectAsStateWithLifecycle(initialValue = null)

    LaunchedEffect(token) {
        val onLogin = navController.currentDestination?.hasRoute<LoginRoute>() == true
        if (token == null && !onLogin) {
            navController.navigate(LoginRoute) {
                popUpTo(navController.graph.id) { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = LoginRoute,
        modifier = modifier,
    ) {
        composable<LoginRoute> {
            LoginScreen(
                onLoggedIn = {
                    navController.navigate(AccountsRoute) {
                        popUpTo(LoginRoute) { inclusive = true }
                    }
                },
            )
        }
        composable<AccountsRoute> {
            AccountsScreen()
        }
    }
}