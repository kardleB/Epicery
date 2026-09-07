# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# isMinifyEnabled está en false por ahora (ver README, "Firma de release"), así que estas
# reglas todavía no se aplican a ningún build real, pero quedan listas para cuando se active.

# Gson (usado por el converter de Retrofit, ver data/remote/NetworkModule) deserializa por
# reflection los data class de request/response; sin mantenerlos, R8 puede eliminar o renombrar
# campos que solo se leen/escriben reflexivamente al parsear JSON, rompiendo el mapeo en tiempo
# de ejecución sin ningún error en compilación.
-keep class com.epicery.app.data.remote.** { *; }
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Firebase, Room y Hilt ya incluyen sus propias reglas de consumidor dentro de cada AAR
# (consumer-rules.pro), R8 las aplica automáticamente: no hace falta duplicarlas acá.
