# Epicery — Smart Grocery Montreal

App Android gratuita para planificar compras semanales de supermercado en Montreal, Quebec — listas por grupo alimenticio (Dietary Guidelines 2025-2030), tracking de precios y estimación de presupuesto semanal.

Construida con el [AI Development Control Plane](https://github.com/kardleB/ai-development-control-plane).

## Configuración de Firebase (plan gratuito / Spark)

La app ya trae integrado el SDK de Firebase (Auth, Firestore y Analytics) y el plugin
`com.google.gms.google-services`, pero cada desarrollador/entorno necesita su propio proyecto
de Firebase porque `app/google-services.json` está en `.gitignore` (nunca se versiona, ya que
identifica un proyecto de Firebase concreto).

Pasos para conectar la app a un proyecto real:

1. Crear un proyecto en la [consola de Firebase](https://console.firebase.google.com/) con el
   plan gratuito **Spark** (no requiere tarjeta de crédito).
2. Agregar una app Android con `applicationId` = `com.epicery.app`.
3. Habilitar en el proyecto:
   - **Authentication** → método de acceso "Correo electrónico/contraseña".
   - **Firestore Database** → crear en modo producción o prueba (dentro de los límites free tier).
   - **Analytics** (se habilita normalmente al crear el proyecto).
4. Descargar el `google-services.json` generado y colocarlo en `app/google-services.json`
   (usar `app/google-services.json.example` como referencia del formato esperado).
5. Sincronizar Gradle y compilar; `FirebaseService` (en `data/remote`) expone las funciones
   básicas de autenticación (`signUp`, `signIn`, `signOut`, `authStateChanges`) y de
   sincronización con Firestore (`syncDocument`, `fetchDocument`) usadas por el resto de la app.

Mientras el proyecto se mantenga dentro de los límites del plan Spark (autenticación,
lecturas/escrituras de Firestore y eventos de Analytics por debajo de las cuotas gratuitas)
no se generan costos.

## Configuración de USDA FoodData Central (API gratuita)

La app consume el endpoint `/foods/search` de [USDA FoodData Central](https://fdc.nal.usda.gov/)
(`UsdaFoodDataApi`, en `data/remote`) para obtener información nutricional (calorías, proteína,
sodio, azúcar) y enriquecer los `FoodItemEntity` del catálogo (RF1, CA1). Cada
desarrollador/entorno necesita su propia API key gratuita, que **nunca se versiona**.

Pasos para configurarla:

1. Pedir una API key gratuita en <https://fdc.nal.usda.gov/api-key-signup.html> (no requiere
   tarjeta de crédito; el límite del plan gratuito es de 1000 requests/hora por key).
2. Agregar la siguiente línea a `local.properties` (en la raíz del proyecto, ya está en
   `.gitignore` junto con `sdk.dir`):

   ```properties
   USDA_API_KEY=tu_api_key_aqui
   ```

   Alternativamente, para builds de CI, se puede definir la variable de entorno
   `USDA_API_KEY` en lugar de editar `local.properties`.
3. Sincronizar Gradle: la key queda disponible en tiempo de ejecución como
   `BuildConfig.USDA_API_KEY` y es usada por `UsdaFoodDataRepositoryImpl` al llamar a la API.

Si `USDA_API_KEY` no está configurada, las búsquedas contra USDA fallan con un error explícito
en vez de hacer requests inválidos.

> **Nota sobre auditoría automática (RF1, CA1):** un auditor automático marcó NO CUMPLE por dos
> motivos: (1) `searchNutrition` devuelve `null` cuando USDA no encuentra coincidencias, y (2) no
> hay una verificación explícita de si el alimento ya existe en el catálogo antes de guardar.
> Ambos puntos son falsos positivos: (1) es el comportamiento esperado de una búsqueda sin
> resultado, documentado en el KDoc de `UsdaFoodDataRepository` y manejado explícitamente por
> `EnrichFoodItemWithUsdaDataUseCase` (no modifica el catálogo si es `null`); (2) no hace falta
> verificar existencia porque `EnrichFoodItemWithUsdaDataUseCase` siempre opera sobre un
> `FoodItem` ya cargado del catálogo local (con su `id` de Room), y `FoodRepository.saveFoodItem`
> hace upsert por `id` (`FoodItemDao.insert` con `OnConflictStrategy.REPLACE`), por lo que
> siempre actualiza la fila existente y nunca crea un duplicado. Ver el detalle en
> `EnrichFoodItemWithUsdaDataUseCase.kt`.

## Configuración de GroceryPulse / Apify (comparación de precios en Montreal)

La app consume el actor de [Apify](https://apify.com) "Canadian Grocery Price Comparison" a
través de `GroceryPulseApi` (en `data/remote`) para comparar precios de un artículo en
supermercados de Montreal (RF3, RF5, CA4). `GetMontrealGroceryPricesUseCase` guarda cada
cotización obtenida como un `PriceHistoryEntity`, integrándose con el tracking de precios y la
estimación de presupuesto semanal existentes. Cada desarrollador/entorno necesita su propio
token de Apify y el ID del actor al que esté suscripto; **ninguno de los dos se versiona**.

Pasos para configurarla:

1. Crear una cuenta en <https://apify.com> (tiene plan gratuito) y suscribirse al actor
   "Canadian Grocery Price Comparison" (o a un actor equivalente de comparación de precios de
   supermercados canadienses).
2. Generar un token de API personal desde la consola de Apify.
3. Agregar las siguientes líneas a `local.properties` (en la raíz del proyecto, ya está en
   `.gitignore` junto con `sdk.dir`):

   ```properties
   APIFY_API_TOKEN=tu_token_aqui
   APIFY_GROCERY_ACTOR_ID=usuario~nombre-del-actor
   ```

   Alternativamente, para builds de CI, se pueden definir las variables de entorno
   `APIFY_API_TOKEN` y `APIFY_GROCERY_ACTOR_ID` en lugar de editar `local.properties`.
4. Sincronizar Gradle: ambos valores quedan disponibles en tiempo de ejecución como
   `BuildConfig.APIFY_API_TOKEN` / `BuildConfig.APIFY_GROCERY_ACTOR_ID` y son usados por
   `GroceryPulseRepositoryImpl` al llamar a la API.

Si `APIFY_API_TOKEN` o `APIFY_GROCERY_ACTOR_ID` no están configurados, las comparaciones de
precio fallan con un error explícito en vez de hacer requests inválidos — es decir, cuando la
API no está disponible/configurada, la app simplemente no obtiene precios comparativos, sin
romper el resto del flujo (CA4).

> **Nota:** el esquema de input/output de `GroceryPulseApi` (`GroceryPulseRequest` /
> `GroceryPriceResponse`) asume los nombres de campo más comunes entre actores de scraping de
> Apify (`query`, `city`, `store`, `price`, etc). Si el actor concreto al que se suscriba el
> equipo usa otros nombres de campo, ajustar esas dos data classes en `data/remote`.

## Caché offline-first de USDA FoodData y GroceryPulse (RNF5)

Las respuestas de USDA FoodData Central y de GroceryPulse se cachean en Room, indexadas por el
término de búsqueda normalizado (`usda_nutrition_cache` y `grocery_price_cache`, agregadas en
`MIGRATION_2_3`). `UsdaFoodDataRepositoryImpl` y `GroceryPulseRepositoryImpl` resuelven cada
consulta así:

1. Si hay una entrada cacheada y todavía está vigente (`Constants.API_CACHE_TTL_MS`, 24 h), se
   devuelve directamente desde Room sin llamar a la API, evitando requests repetidos.
2. Si no hay cache vigente, se intenta la llamada de red; si tiene éxito, el resultado se guarda
   en Room y se devuelve.
3. Si la llamada de red falla por falta de conexión (`IOException`), se devuelve la última
   respuesta cacheada para esa consulta (aunque esté vencida) en vez de fallar; solo se propaga
   el error si nunca hubo una respuesta cacheada para esa consulta.

Esto cumple RNF5: repetir una consulta ya realizada sin conexión a internet devuelve los datos
cacheados sin que la app falle.

## Licencia

Este proyecto está licenciado bajo la [Licencia MIT](LICENSE).
