import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt)
    alias { libs.plugins.kotlin.kapt }
    alias { libs.plugins.kotlin.serialization }
//    id("kotlinx-serialization")
}

val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}
val openWeatherKey = (localProps.getProperty("OPENWEATHER_API_KEY")
    ?: System.getenv("OPENWEATHER_API_KEY")
    ?: "")

android {
    namespace = "com.example.weatherforecast.core"
    compileSdk = (project.findProperty("ANDROID_COMPILE_SDK") as String).toInt()
    defaultConfig {
        // BuildConfig constant read by Hilt via @Named("owmApiKey")
        buildConfigField("String", "OPENWEATHER_API_KEY", "\"$openWeatherKey\"")
    }

    android.buildFeatures.buildConfig = true
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    //hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    //retrofit
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.kotlin.serialization)
    //kotlin serialization
    implementation(libs.kotlinx.serialization.json)
    // http logging
    implementation(libs.okhttp.logging)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}