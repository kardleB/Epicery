package com.epicery.app.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.epicery.app.ui.budget.BudgetScreen
import com.epicery.app.ui.common.ApiErrorBanner
import com.epicery.app.ui.home.HomeScreen
import com.epicery.app.ui.navigation.EpiceryDestination
import com.epicery.app.ui.pricetracker.PriceTrackerScreen
import com.epicery.app.ui.settings.SettingsScreen
import com.epicery.app.ui.shoppinglist.ShoppingListScreen

/**
 * Punto de entrada de la UI: aloja el `NavHost` con las 5 secciones principales y la bottom
 * navigation persistente entre ellas (ver `docs/design/wireframes.md`). Las 5 secciones (Home,
 * Shopping List, Price Tracker, Budget y Ajustes) están implementadas por completo.
 */
@Composable
fun EpiceryApp() {
    val navController = rememberNavController()
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = { EpiceryBottomBar(navController) }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            ApiErrorBanner()
            NavHost(
                navController = navController,
                startDestination = EpiceryDestination.Home.route
            ) {
                composable(EpiceryDestination.Home.route) {
                    HomeScreen(
                        onNavigateToShoppingList = {
                            navController.navigateToBottomNavDestination(EpiceryDestination.ShoppingList)
                        },
                        onNavigateToSettings = {
                            navController.navigateToBottomNavDestination(EpiceryDestination.Settings)
                        }
                    )
                }
                composable(EpiceryDestination.ShoppingList.route) {
                    ShoppingListScreen()
                }
                composable(EpiceryDestination.PriceTracker.route) {
                    PriceTrackerScreen()
                }
                composable(EpiceryDestination.Budget.route) {
                    BudgetScreen()
                }
                composable(EpiceryDestination.Settings.route) {
                    SettingsScreen()
                }
            }
        }
    }
}

@Composable
private fun EpiceryBottomBar(navController: NavHostController) {
    val currentDestination = navController.currentBackStackEntryAsState().value?.destination
    NavigationBar {
        EpiceryDestination.bottomNavItems.forEach { destination ->
            val selected = currentDestination?.hierarchy?.any { it.route == destination.route } == true
            val label = stringResource(destination.labelRes)
            NavigationBarItem(
                selected = selected,
                onClick = { navController.navigateToBottomNavDestination(destination) },
                icon = { Icon(destination.icon, contentDescription = label) },
                label = { Text(label, style = MaterialTheme.typography.labelSmall) }
            )
        }
    }
}

private fun NavHostController.navigateToBottomNavDestination(destination: EpiceryDestination) {
    navigate(destination.route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
