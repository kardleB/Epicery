package com.epicery.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Las 5 secciones principales de la app (ver `docs/design/wireframes.md`), usadas tanto para
 * las rutas del `NavHost` como para los items de la bottom navigation persistente.
 */
sealed class EpiceryDestination(val route: String, val label: String, val icon: ImageVector) {
    data object Home : EpiceryDestination("home", "Home", Icons.Default.Home)
    data object ShoppingList : EpiceryDestination("shopping_list", "Lista", Icons.Default.ShoppingCart)
    data object PriceTracker : EpiceryDestination("price_tracker", "Precios", Icons.Default.TrendingUp)
    data object Budget : EpiceryDestination("budget", "Budget", Icons.Default.AccountBalanceWallet)
    data object Settings : EpiceryDestination("settings", "Ajustes", Icons.Default.Settings)

    companion object {
        val bottomNavItems = listOf(Home, ShoppingList, PriceTracker, Budget, Settings)
    }
}
