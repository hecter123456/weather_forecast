import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt)
    alias { libs.plugins.kotlin.kapt }
    alias { libs.plugins.kotlin.serialization }
    alias(libs.plugins.room)
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
        minSdk = 24
        // BuildConfig constant read by Hilt via @Named("owmApiKey")
        buildConfigField("String", "OPENWEATHER_API_KEY", "\"$openWeatherKey\"")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    room {
        schemaDirectory("$projectDir/schemas") // Specify the directory for schema files
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
    //room
    implementation(libs.room.ktx)
    implementation(libs.room.runtime)
    kapt(libs.room.compiler)
    //datastore
    implementation(libs.androidx.dataStore.preference)

    //test
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.turbine)
    //androidTest
    androidTestImplementation(libs.androidx.junit.ktx)
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.turbine)
}