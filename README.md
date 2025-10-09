# Weather Forecast (Jetpack Compose + Clean Architecture)

An Android app that shows **today’s** and **7‑day** weather forecasts, lets users **search & pick cities** and **save favorites**

Built with:
- **Kotlin**, **Jetpack Compose** (Material 3), **Navigation Compose**
- **Clean Architecture** (domain/use cases, data, presentation)
- **Hilt** for DI
- **Retrofit** + **OkHttp** + **Kotlinx Serialization**
- **OpenWeatherMap** (One Call & Geocoding APIs)
- **Room** + **DataStore** (favorites & selected city)
- Tests: **JUnit**, **Mockito, **Turbine**, **Compose UI tests** (Robolectric)

---

## Table of Contents
- [Project Structure](#project-structure)
- [Features](#features)
- [Requirements](#requirements)
- [Quick Start](#quick-start)
- [Provide API keys (`local.properties`)](#provide-api-keys-localproperties)
  - [OpenWeatherMap (`OWM_API_KEY`)](#openweathermap-owm_api_key)
- [Build & Run](#build--run)
- [Testing](#testing)
- [CI/CD (GitHub Actions)](#cicd-github-actions)
- [License](#license)

---

## Project Structure

```
root
├─ app/                 # App module (DI entry points, navigation host, theming)
├─ core/                # Core domain & data abstractions (models, use cases, repos)
└─ feature/             # UI features (Compose screens, ViewModels, mappers, impls)
```

**Module dependencies**
- `app` → depends on `core` and `feature`
- `feature` → depends on `core`
- `core` → pure Kotlin/Android library (no app dependency)

---

## Features

- **Today forecast** (current conditions, temperature, wind)
- **7‑day forecast**
- **City search** (OWM Geocoding)
- **Favorites**: add / update / remove (Room)
- **Selected city** persisted with DataStore

---

## Requirements

- **Android Studio** Giraffe/Koala or newer
- **JDK 17**
- **minSdk 24**
- OpenWeatherMap API key

---

## Quick Start

1. **Clone**
   ```bash
   git clone https://github.com/<you>/<repo>.git
   cd <repo>
   ```

2. **Create `local.properties`** at the **project root** and add your API key(s) as described below.

3. **Build & run**
   ```bash
   ./gradlew assembleDebug
   # or from Android Studio: Run ▶
   ```

---

## Provide API keys (`local.properties`)

> `local.properties` is **not** committed to VCS. It’s ideal for local secrets.

### OpenWeatherMap (`OPENWEATHER_API_KEY`)

1. Open (or create) `local.properties` in the **project root**.
2. Add your key:
   ```properties
   OPENWEATHER_API_KEY=YOUR_OWM_API_KEY_HERE
   ```
3. Ensure the app exposes this value to `BuildConfig` and DI.

**Gradle (e.g., `core/build.gradle.kts`)**

```kotlin
import java.util.Properties

val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}
val openWeatherKey = (localProps.getProperty("OPENWEATHER_API_KEY")
    ?: System.getenv("OPENWEATHER_API_KEY")
    ?: "")

android {
    defaultConfig {
        // Expose as BuildConfig so it can be injected
        buildConfigField("String", "OPENWEATHER_API_KEY", "\"$openWeatherKey\"")
    }
}
```

**Hilt provider**

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object ApiKeyModule {
    @Provides
    @Named("owmApiKey")
    fun provideOwmApiKey(): String = BuildConfig.OWM_API_KEY
}
```

**Usage in data source**

```kotlin
class WeatherNetworkDataSourceImpl @Inject constructor(
    private val api: OpenWeatherApi,
    @Named("owmApiKey") private val apiKey: String
) : WeatherNetworkDataSource {
    // pass apiKey to Retrofit calls…
}
```

---

## Build & Run

```bash
# Clean build
./gradlew clean assembleDebug

# Install on a connected device
./gradlew :app:installDebug
```

---

## Testing

**Unit tests (incl. Robolectric):**
```bash
./gradlew testDebugUnitTest
```

**Instrumented tests (on emulator/device):**
```bash
./gradlew connectedDebugAndroidTest
```

**Compose UI tests**
- Robolectric (JVM) or instrumented (androidTest) supported.
- For Robolectric, set:
  ```kotlin
  android { testOptions.unitTests.isIncludeAndroidResources = true }
  ```

---

## CI/CD (GitHub Actions)

### CI (unit tests + Lint + assemble)

`.github/workflows/android-ci.yml`

```yaml
name: Android CI
on:
  push: { branches: [ main ] }
  pull_request: { branches: [ main ] }
jobs:
  build-test:
    runs-on: ubuntu-latest
    steps:
      - name: Checkout
        uses: actions/checkout@v4
      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with: { distribution: temurin, java-version: 17, cache: gradle }
      - name: Gradle wrapper permissions
        run: chmod +x ./gradlew
      - name: Write local.properties
        run: |
          cat > local.properties <<'EOF'
          OWM_API_KEY=${{ secrets.OWM_API_KEY }}
          EOF
      - name: Build + Unit tests + Lint
        run: ./gradlew clean testDebugUnitTest lintDebug assembleDebug --stacktrace --no-daemon
      - name: Upload artifacts
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: reports-and-apk
          path: |
            **/build/reports/**
            app/build/outputs/apk/debug/*.apk
```
