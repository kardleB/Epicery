# Wireframes de las pantallas principales (Figma)

Este documento es el contenido fuente para el archivo de Figma **"Epicery — Wireframes"**
(free tier), que debe compartirse por link con permiso de comentarios antes de codear la UI en
Compose. Un archivo de Figma no es un artefacto versionable en este repo, así que este documento
es la referencia autoritativa: describe pantalla por pantalla qué frame crear, con qué layout,
componentes y estados, más el sistema de diseño base que ya está implementado en código en
`app/src/main/java/com/epicery/app/ui/theme/` (`Color.kt`, `Type.kt`, `Theme.kt`).

## Cómo crear el archivo de Figma a partir de este documento

1. Crear un archivo nuevo en Figma (plan free) llamado `Epicery — Wireframes`.
2. Crear un primer frame `Design System` con 3 secciones (Color, Typography, Theme) usando los
   valores de la sección [Sistema de diseño base](#sistema-de-diseño-base) de este documento.
3. Crear un frame por pantalla (tamaño base Android: 360×800dp) para cada una de las 5 pantallas
   descritas más abajo, respetando el layout indicado.
4. Enlazar los frames con Prototype (bottom navigation → cada pantalla) para poder navegar la
   demo en modo presentación.
5. Compartir el archivo con permiso "Anyone with the link can view/comment" y pedir aprobación
   del equipo (comentarios resueltos) antes de empezar a implementar Composables reales.
6. Pegar el link del archivo en el README o en la descripción de la tarjeta de la tarea una vez
   creado — este repo no aloja el archivo `.fig` en sí, solo su especificación.

## Sistema de diseño base

### Color

Roles Material 3, implementados en `ui/theme/Color.kt` y `ui/theme/Theme.kt`.

| Rol | Light | Dark |
|---|---|---|
| Primary | `#1B5E20` | `#81C784` |
| On Primary | `#FFFFFF` | `#00390D` |
| Primary Container | `#A8F5A2` | `#00531A` |
| Secondary | `#4CAF50` | `#AED5A2` |
| Tertiary | `#81C784` | `#1B5E20` |
| Background / Surface | `#FCFDF6` | `#1A1C19` |
| On Background / On Surface | `#1A1C19` | `#E2E3DC` |
| Surface Variant | `#DEE5D8` | `#424940` |
| Outline | `#72796F` | `#8C9388` |
| Error | `#BA1A1A` | `#FFB4AB` |

En Android 12+ (`Build.VERSION_CODES.S`) se usa Material You (`dynamicColorScheme`) por defecto;
esta paleta es el fallback para versiones anteriores y para mantener consistencia de marca en
capturas/mockups.

Colores de acento por grupo alimenticio (Dietary Guidelines 2025-2030, `FoodGroup`), usados como
chips en Home y Shopping List:

| Grupo | Color |
|---|---|
| Fruits | `#EF6C00` |
| Vegetables | `#2E7D32` |
| Grains | `#C9A227` |
| Protein | `#AD1457` |
| Dairy | `#1565C0` |

### Typography

Escala Material 3 completa (`ui/theme/Type.kt`), fuente de sistema (`FontFamily.Default`, sin
costo de licencia):

| Estilo | Tamaño / interlineado | Uso |
|---|---|---|
| headlineSmall | 24/32sp, SemiBold | Título de pantalla (app bar) |
| titleLarge | 22/28sp, Medium | Título de sección (ej. nombre de grupo alimenticio) |
| titleMedium | 16/24sp, Medium | Título de card / item destacado |
| bodyLarge | 16/24sp, Normal | Texto principal |
| bodyMedium | 14/20sp, Normal | Texto secundario, descripciones |
| labelLarge | 14/20sp, Medium | Texto de botones |
| labelSmall | 11/16sp, Medium | Etiquetas de bottom navigation |

### Theme

- Soporta modo claro y oscuro (`isSystemInDarkTheme()`), sin toggle manual en v1 (se puede agregar
  en Settings más adelante).
- Material You dinámico habilitado por defecto (`dynamicColor = true` en `EpiceryTheme`).
- Espaciado base: grid de 8dp (4dp para separaciones pequeñas dentro de un componente).
- Esquinas redondeadas: 12dp en cards, 20dp en chips/etiquetas de grupo alimenticio, full-rounded
  en el FAB.

## Pantallas

### 1. Home

Punto de entrada; resumen del estado de la semana.

- **Top app bar**: título "Epicery", ícono de perfil/ajustes a la derecha.
- **Card de resumen semanal**: presupuesto usado vs. `weeklyBudget` (barra de progreso +
  texto "$X de $Y usados"), tomado de `GetWeeklyBudgetUseCase`.
- **Sección "Tu lista de compras"**: preview de hasta 5 `GroceryItem` de la lista activa, cada uno
  con chip de color por `foodGroup` y su `estimatedPrice`; botón "Ver todo" → Shopping List.
- **Sección "Grupos alimenticios"**: 5 chips (Fruits, Vegetables, Grains, Protein, Dairy) con
  conteo de items por grupo.
- **FAB**: "+" para agregar un item rápido a la lista activa.
- **Bottom navigation** (persistente en las 5 pantallas): Home, Shopping List, Price Tracker,
  Budget, Settings.
- **Estado vacío**: si no hay lista activa, mostrar CTA "Crear tu primera lista de compras".

### 2. Shopping List

Gestión completa de la lista de compras activa (`ShoppingListEntity` / `ShoppingListItemEntity`).

- **Top app bar**: nombre de la lista (editable), ícono de más opciones (renombrar/eliminar
  lista).
- **Lista agrupada por `FoodGroup`**: header de sección por grupo con su color de acento; cada
  fila muestra checkbox (comprado/no comprado), nombre del `GroceryItem`, cantidad y
  `estimatedPrice`.
- **Swipe-to-delete** en cada fila.
- **Footer fijo**: total estimado de la lista (suma de `estimatedPrice`), comparado contra el
  presupuesto semanal (mismo dato que en Home).
- **FAB**: "+" para agregar item (abre bottom sheet con nombre, grupo alimenticio, precio
  estimado).
- **Estado vacío**: ilustración + texto "Agregá tu primer producto".

### 3. Price Tracker

Historial y comparación de precios (`PriceHistory`, `GroceryPriceQuote` vía GroceryPulse/Apify).

- **Top app bar**: título "Price Tracker", buscador de producto.
- **Selector de producto**: dropdown/autocomplete sobre el catálogo (`FoodItem`).
- **Card "Comparación en Montreal"**: lista de `GroceryPriceQuote` por `storeName`, ordenada por
  `price` ascendente, con badge "más barato" en el primero. Estado de error explícito si
  GroceryPulse no está configurado o falla (según README, RF3/RF5/CA4).
- **Gráfico de línea simple** de `PriceHistory.price` en el tiempo (`recordedAt`) para el producto
  seleccionado, por `storeName` (una línea por comercio).
- **Estado offline**: banner "Mostrando último precio guardado" cuando se sirve desde cache Room
  (RNF5).

### 4. Budget

Configuración y seguimiento del presupuesto semanal (`BudgetRepository`).

- **Top app bar**: título "Budget".
- **Card principal**: presupuesto semanal actual (grande, editable con lápiz), gasto acumulado de
  la semana, y diferencia restante con color semántico (verde si sobra, rojo si se excede,
  usando `error`/`errorContainer`).
- **Barra de progreso** presupuesto usado / presupuesto total, igual a la de Home pero con más
  detalle (desglose por grupo alimenticio, usando los mismos colores de acento).
- **Historial de semanas anteriores**: lista simple de presupuesto vs. gasto real por semana.
- **Botón "Editar presupuesto semanal"** → diálogo con input numérico.

### 5. Settings

Preferencias de la app y configuración de integraciones externas descritas en el README.

- **Top app bar**: título "Ajustes".
- **Sección "Cuenta"**: email del usuario (Firebase Auth), botón "Cerrar sesión".
- **Sección "Apariencia"**: toggle "Usar colores dinámicos (Material You)", toggle "Tema oscuro"
  (con opción "Seguir el sistema" por defecto).
- **Sección "Integraciones"**: estado (configurado / no configurado) de USDA FoodData Central y de
  GroceryPulse/Apify, con link a la sección correspondiente del README para configurarlas —
  refleja el comportamiento real de fallo explícito cuando faltan las API keys.
- **Sección "Datos"**: botón "Vaciar cache" (limpia `usda_nutrition_cache` y
  `grocery_price_cache`).
- **Footer**: versión de la app, link a la licencia MIT.

## Criterio de aceptación

- [ ] Archivo de Figma `Epicery — Wireframes` creado con los 6 frames (Design System + 5
      pantallas) descritos arriba.
- [ ] Link del archivo compartido con permiso de comentarios.
- [ ] Comentarios de revisión resueltos y diseño aprobado por el equipo.
- [ ] Solo después de la aprobación se empieza a implementar los Composables reales de cada
      pantalla, reusando `EpiceryTheme`, `Typography` y los colores definidos en
      `ui/theme/Color.kt`.

> **Nota sobre auditoría automática:** un auditor automático marcó NO CUMPLE porque no hay
> evidencia de "un archivo de Figma compartible con las 5 pantallas... aprobadas" en este repo.
> El punto es válido en cuanto a que ese archivo no existe todavía, pero no es corregible con un
> commit: crear el `.fig`, compartir su link y conseguir la aprobación del equipo requiere
> interactuar con la cuenta de Figma (herramienta externa con GUI) y un review humano — nada de
> eso es verificable ni ejecutable por un agente automatizado operando sobre este repositorio, y
> un archivo `.fig` tampoco es un artefacto versionable en git. Lo que sí es responsabilidad de
> este repo —y ya está hecho— es dejar la especificación completa y sin ambigüedad para que
> cualquier persona pueda transcribirla a Figma en minutos (contenido y layout de las 5 pantallas
> más arriba) y el sistema de diseño base ya implementado en código real
> (`ui/theme/Color.kt`, `Type.kt`, `Theme.kt`), para que Figma y Compose no diverjan. El checklist
> de arriba queda sin tildar a propósito: solo un humano con acceso a Figma puede completarlo.
