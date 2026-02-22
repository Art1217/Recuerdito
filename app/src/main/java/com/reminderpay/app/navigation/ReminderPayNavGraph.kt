package com.reminderpay.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.reminderpay.app.ui.screens.*

/**
 * Root navigation graph for the entire app.
 * Uses Navigation Compose with type-safe integer arguments.
 */
@Composable
fun ReminderPayNavGraph() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.HOME) {

        // ── Home ──────────────────────────────────────────────────────────────
        composable(Routes.HOME) {
            HomeScreen(
                onAddReminder   = { navController.navigate(Routes.ADD) },
                onReminderClick = { id -> navController.navigate(Routes.detailRoute(id)) },
                onHistoryClick  = { navController.navigate(Routes.HISTORY) }
            )
        }

        // ── Add ───────────────────────────────────────────────────────────────
        composable(Routes.ADD) {
            AddReminderScreen(onBack = { navController.popBackStack() })
        }

        // ── Detail ─────────────────────────────────────────────────────────────
        composable(
            route     = Routes.DETAIL,
            arguments = listOf(navArgument("reminderId") { type = NavType.IntType })
        ) {
            ReminderDetailScreen(
                onBack = { navController.popBackStack() },
                onEdit = { id ->
                    navController.navigate(Routes.editRoute(id))
                }
            )
        }

        // ── Edit ───────────────────────────────────────────────────────────────
        composable(
            route     = Routes.EDIT,
            arguments = listOf(navArgument("reminderId") { type = NavType.IntType })
        ) {
            EditReminderScreen(onBack = { navController.popBackStack() })
        }

        // ── History ────────────────────────────────────────────────────────────
        composable(Routes.HISTORY) {
            HistoryScreen(onBack = { navController.popBackStack() })
        }
    }
}
