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

## Licencia

Este proyecto está licenciado bajo la [Licencia MIT](LICENSE).
