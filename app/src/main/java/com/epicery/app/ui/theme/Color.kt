package com.epicery.app.ui.theme

import androidx.compose.ui.graphics.Color

// Paleta base (marca) — reflejada en el archivo de Figma "Epicery — Design System".
val GreenPrimary = Color(0xFF1B5E20)
val GreenSecondary = Color(0xFF4CAF50)
val GreenTertiary = Color(0xFF81C784)

// Tema claro — roles Material 3.
val LightPrimary = GreenPrimary
val LightOnPrimary = Color(0xFFFFFFFF)
val LightPrimaryContainer = Color(0xFFA8F5A2)
val LightOnPrimaryContainer = Color(0xFF002204)
val LightSecondary = GreenSecondary
val LightOnSecondary = Color(0xFFFFFFFF)
val LightSecondaryContainer = Color(0xFFC8F5C0)
val LightOnSecondaryContainer = Color(0xFF042100)
val LightTertiary = GreenTertiary
val LightOnTertiary = Color(0xFF00390D)
val LightBackground = Color(0xFFFCFDF6)
val LightOnBackground = Color(0xFF1A1C19)
val LightSurface = Color(0xFFFCFDF6)
val LightOnSurface = Color(0xFF1A1C19)
val LightSurfaceVariant = Color(0xFFDEE5D8)
val LightOnSurfaceVariant = Color(0xFF424940)
val LightOutline = Color(0xFF72796F)
val LightError = Color(0xFFBA1A1A)
val LightOnError = Color(0xFFFFFFFF)
val LightErrorContainer = Color(0xFFFFDAD6)
val LightOnErrorContainer = Color(0xFF410002)

// Tema oscuro — roles Material 3.
val DarkPrimary = GreenTertiary
val DarkOnPrimary = Color(0xFF00390D)
val DarkPrimaryContainer = Color(0xFF00531A)
val DarkOnPrimaryContainer = Color(0xFFA8F5A2)
val DarkSecondary = Color(0xFFAED5A2)
val DarkOnSecondary = Color(0xFF163817)
val DarkSecondaryContainer = Color(0xFF2D4E2B)
val DarkOnSecondaryContainer = Color(0xFFC8F5C0)
val DarkTertiary = GreenPrimary
val DarkOnTertiary = Color(0xFFA8F5A2)
val DarkBackground = Color(0xFF1A1C19)
val DarkOnBackground = Color(0xFFE2E3DC)
val DarkSurface = Color(0xFF1A1C19)
val DarkOnSurface = Color(0xFFE2E3DC)
val DarkSurfaceVariant = Color(0xFF424940)
val DarkOnSurfaceVariant = Color(0xFFC2C9BC)
val DarkOutline = Color(0xFF8C9388)
val DarkError = Color(0xFFFFB4AB)
val DarkOnError = Color(0xFF690005)
val DarkErrorContainer = Color(0xFF93000A)
val DarkOnErrorContainer = Color(0xFFFFDAD6)

/**
 * Colores de acento por grupo alimenticio (Dietary Guidelines 2025-2030), usados como
 * chips/etiquetas en Home y Shopping List para distinguir de un vistazo cada grupo
 * (`FoodGroup` en `data/local`). Mismos valores en el archivo de Figma, capa "Food group tags".
 */
val FoodGroupFruits = Color(0xFFEF6C00)
val FoodGroupVegetables = Color(0xFF2E7D32)
val FoodGroupGrains = Color(0xFFC9A227)
val FoodGroupProtein = Color(0xFFAD1457)
val FoodGroupDairy = Color(0xFF1565C0)
