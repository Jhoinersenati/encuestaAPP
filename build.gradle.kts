// Archivo: build.gradle.kts (Raíz del Proyecto: encuestaAPP)
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false

    // Solo una forma de declarar Google Services.
    // Usamos 'id' para evitar conflictos con el catálogo de librerías.
    id("com.google.gms.google-services") version "4.4.2" apply false
}