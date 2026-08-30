import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.google.services)
}

// La API key de USDA FoodData Central es gratuita (https://fdc.nal.usda.gov/api-key-signup.html)
// pero no se versiona: cada desarrollador la coloca en su `local.properties` (gitignored),
// igual que `sdk.dir`. Sirve como fallback una variable de entorno para builds de CI (ver README).
val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use { load(it) }
    }
}
val usdaApiKey: String =
    (localProperties.getProperty("USDA_API_KEY") ?: System.getenv("USDA_API_KEY") ?: "")

// GroceryPulse (RF3, RF5, CA4) corre el actor de Apify "Canadian Grocery Price Comparison"
// para comparar precios de supermercados en Montreal. El token de API y el ID del actor
// suscrito tampoco se versionan (mismo mecanismo que USDA_API_KEY) porque son específicos
// de la cuenta/plan de Apify de cada desarrollador — ver README.
val apifyApiToken: String =
    (localProperties.getProperty("APIFY_API_TOKEN") ?: System.getenv("APIFY_API_TOKEN") ?: "")
val apifyGroceryActorId: String =
    (localProperties.getProperty("APIFY_GROCERY_ACTOR_ID")
        ?: System.getenv("APIFY_GROCERY_ACTOR_ID")
        ?: "")

android {
    namespace = "com.epicery.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.epicery.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "USDA_API_KEY", "\"$usdaApiKey\"")
        buildConfigField("String", "APIFY_API_TOKEN", "\"$apifyApiToken\"")
        buildConfigField("String", "APIFY_GROCERY_ACTOR_ID", "\"$apifyGroceryActorId\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    // AppCompatDelegate.setApplicationLocales (RF6, CA4): override de idioma por app persistido
    // en Settings, sin necesidad de que las Activities extiendan AppCompatActivity.
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // DataStore Preferences (RF5, RNF4): persiste los ajustes de usuario (Settings) entre
    // sesiones sin depender de una cuenta ni de la red.
    implementation(libs.androidx.datastore.preferences)

    implementation(libs.retrofit.core)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp.logging.interceptor)

    implementation(libs.kotlinx.coroutines.android)

    // Firebase (plan gratuito Spark): requiere app/google-services.json real,
    // descargado desde la consola de Firebase del proyecto (ver README).
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation(libs.room.testing)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
