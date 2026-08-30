package com.epicery.app.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.ui.graphics.vector.ImageVector
import com.epicery.app.R

/**
 * Las 5 secciones principales de la app (ver `docs/design/wireframes.md`), usadas tanto para
 * las rutas del `NavHost` como para los items de la bottom navigation persistente. [labelRes]
 * apunta a un string resource (en vez de un `String` fijo) para que el label se resuelva en el
 * idioma activo de la app (RF6, CA4) al leerlo con `stringResource` en `EpiceryApp`.
 */
sealed class EpiceryDestination(val route: String, @StringRes val labelRes: Int, val icon: ImageVector) {
    data object Home : EpiceryDestination("home", R.string.nav_home, Icons.Default.Home)
    data object ShoppingList : EpiceryDestination("shopping_list", R.string.nav_shopping_list, Icons.Default.ShoppingCart)
    data object PriceTracker : EpiceryDestination("price_tracker", R.string.nav_price_tracker, Icons.Default.TrendingUp)
    data object Budget : EpiceryDestination("budget", R.string.budget_title, Icons.Default.AccountBalanceWallet)
    data object Settings : EpiceryDestination("settings", R.string.settings_title, Icons.Default.Settings)

    companion object {
        val bottomNavItems = listOf(Home, ShoppingList, PriceTracker, Budget, Settings)
    }
}
