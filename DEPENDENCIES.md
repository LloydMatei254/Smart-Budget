# Smart Budget - Dependencies Documentation

## Overview

This document explains all dependencies used in the Smart Budget Android application, their purpose, and configuration.

## Dependency Management

We use **Gradle Version Catalog** (`libs.versions.toml`) for centralized dependency management, making it easier to update versions across the project.

## Core Dependencies

### AndroidX Core Libraries

#### Core KTX (1.12.0)
```kotlin
implementation(libs.androidx.core.ktx)
```
- **Purpose**: Kotlin extensions for Android framework APIs
- **Usage**: Simplifies common Android tasks with Kotlin-friendly APIs
- **Example**: `context.getSystemService<SystemService>()`

#### AppCompat (1.6.1)
```kotlin
implementation(libs.androidx.appcompat)
```
- **Purpose**: Backward compatibility support for modern Android features
- **Usage**: Ensures app works on older Android versions (API 24+)
- **Features**: ActionBar, AppCompatActivity, AppCompatDelegate

#### ConstraintLayout (2.1.4)
```kotlin
implementation(libs.androidx.constraintlayout)
```
- **Purpose**: Flexible and efficient layout system
- **Usage**: Building responsive UIs with flat view hierarchies
- **Benefits**: Better performance than nested LinearLayouts

#### Activity KTX (1.8.2)
```kotlin
implementation(libs.androidx.activity.ktx)
```
- **Purpose**: Kotlin extensions for Activity class
- **Usage**: `by viewModels()` delegate, result contracts
- **Example**: `val viewModel: MyViewModel by viewModels()`

#### Fragment KTX (1.6.2)
```kotlin
implementation(libs.androidx.fragment.ktx)
```
- **Purpose**: Kotlin extensions for Fragment class
- **Usage**: Fragment transactions, `by activityViewModels()`
- **Example**: Fragment factories, lifecycle-aware operations

---

## Material Design

### Material Components (1.11.0)
```kotlin
implementation(libs.material)
```
- **Purpose**: Google's Material Design components
- **Usage**: Pre-built UI components following Material Design guidelines
- **Components Used**:
  - MaterialButton
  - TextInputLayout
  - CardView
  - BottomNavigationView
  - FloatingActionButton
  - Snackbar
  - MaterialDatePicker
  - Chip
  - TabLayout

---

## Architecture Components

### Lifecycle & ViewModel (2.7.0)

#### ViewModel KTX
```kotlin
implementation(libs.androidx.lifecycle.viewmodel.ktx)
```
- **Purpose**: Store and manage UI-related data in lifecycle-conscious way
- **Usage**: Survives configuration changes (rotation)
- **Example**: Holds UI state, coordinates use cases

#### LiveData KTX
```kotlin
implementation(libs.androidx.lifecycle.livedata.ktx)
```
- **Purpose**: Lifecycle-aware observable data holder
- **Usage**: Observe data changes in lifecycle-safe manner
- **Note**: Prefer StateFlow in new code

#### Runtime KTX
```kotlin
implementation(libs.androidx.lifecycle.runtime.ktx)
```
- **Purpose**: Lifecycle utilities and coroutines support
- **Usage**: `lifecycleScope`, `repeatOnLifecycle`
- **Example**: Collect flows safely in lifecycle

#### ViewModel SavedState
```kotlin
implementation(libs.androidx.lifecycle.viewmodel.savedstate)
```
- **Purpose**: Save ViewModel state during process death
- **Usage**: Preserve data when system kills app
- **Example**: SavedStateHandle for temporary state

---

## Navigation

### Navigation Component (2.7.6)

#### Navigation Fragment KTX
```kotlin
implementation(libs.androidx.navigation.fragment.ktx)
```
- **Purpose**: Type-safe navigation between fragments
- **Usage**: Single-activity architecture with multiple fragments
- **Features**: Deep linking, argument passing, animations

#### Navigation UI KTX
```kotlin
implementation(libs.androidx.navigation.ui.ktx)
```
- **Purpose**: UI components integration with Navigation
- **Usage**: BottomNavigationView, Toolbar, DrawerLayout
- **Example**: Automatic toolbar title updates

#### Safe Args Plugin
```kotlin
alias(libs.plugins.navigation.safeargs)
```
- **Purpose**: Type-safe argument passing between destinations
- **Usage**: Generates classes for navigation arguments
- **Example**: `NavGraphDirections.actionLoginToHome(userId)`

---

## Concurrency

### Kotlin Coroutines (1.7.3)

#### Coroutines Android
```kotlin
implementation(libs.kotlinx.coroutines.android)
```
- **Purpose**: Asynchronous programming with coroutines
- **Usage**: Background tasks, API calls, database operations
- **Dispatchers**: Main, IO, Default

#### Coroutines Core
```kotlin
implementation(libs.kotlinx.coroutines.core)
```
- **Purpose**: Core coroutines functionality
- **Usage**: Flow, StateFlow, SharedFlow
- **Example**: Repository returns Flow<List<Expense>>

---

## Dependency Injection

### Hilt (2.50)

#### Hilt Android
```kotlin
implementation(libs.hilt.android)
kapt(libs.hilt.compiler)
```
- **Purpose**: Dependency injection framework (built on Dagger)
- **Usage**: Provide and inject dependencies throughout app
- **Annotations**: @HiltAndroidApp, @AndroidEntryPoint, @Inject

#### Hilt Navigation Fragment (1.1.0)
```kotlin
implementation(libs.androidx.hilt.navigation.fragment)
```
- **Purpose**: Hilt integration with Navigation Component
- **Usage**: `by hiltNavGraphViewModels()` delegate
- **Example**: Share ViewModel across navigation graph

#### Hilt WorkManager (1.1.0)
```kotlin
implementation(libs.androidx.hilt.work)
kapt(libs.androidx.hilt.compiler)
```
- **Purpose**: Hilt integration with WorkManager
- **Usage**: Inject dependencies into Workers
- **Example**: SyncWorker with injected repositories

---

## Local Storage

### Room Database (2.6.1)

#### Room Runtime
```kotlin
implementation(libs.androidx.room.runtime)
```
- **Purpose**: SQLite abstraction layer
- **Usage**: Local data caching and offline support
- **Features**: Type safety, compile-time verification

#### Room KTX
```kotlin
implementation(libs.androidx.room.ktx)
```
- **Purpose**: Kotlin extensions and coroutines support
- **Usage**: Suspend functions, Flow queries
- **Example**: `@Query suspend fun getExpenses(): List<ExpenseEntity>`

#### Room Compiler
```kotlin
kapt(libs.androidx.room.compiler)
```
- **Purpose**: Annotation processor for Room
- **Usage**: Generates DAO implementations
- **Process**: Compile-time code generation

---

## Networking

### Retrofit (2.9.0)

#### Retrofit Core
```kotlin
implementation(libs.retrofit)
```
- **Purpose**: Type-safe HTTP client for Android
- **Usage**: REST API communication
- **Features**: Converts API responses to Kotlin objects

#### Gson Converter
```kotlin
implementation(libs.retrofit.converter.gson)
```
- **Purpose**: JSON serialization/deserialization
- **Usage**: Convert JSON to/from Kotlin data classes
- **Alternative**: kotlinx.serialization

### OkHttp (4.12.0)

#### OkHttp Core
```kotlin
implementation(libs.okhttp)
```
- **Purpose**: HTTP client (used by Retrofit)
- **Usage**: Connection pooling, interceptors, caching
- **Features**: Efficient HTTP/2 support

#### Logging Interceptor
```kotlin
implementation(libs.okhttp.logging.interceptor)
```
- **Purpose**: Log HTTP requests/responses
- **Usage**: Debug network calls (debug builds only)
- **Example**: See full request/response in Logcat

### Gson (2.10.1)
```kotlin
implementation(libs.gson)
```
- **Purpose**: JSON library by Google
- **Usage**: Parse JSON strings manually when needed
- **Example**: Custom JSON parsing, complex structures

---

## Preferences & Settings

### DataStore (1.0.0)
```kotlin
implementation(libs.androidx.datastore.preferences)
```
- **Purpose**: Modern replacement for SharedPreferences
- **Usage**: Store key-value preferences asynchronously
- **Benefits**: Type safety, coroutines support, no blocking I/O
- **Example**: User settings, app preferences
- **NOT for**: Sensitive data (use SecureStoage instead)

---

## Background Work

### WorkManager (2.9.0)
```kotlin
implementation(libs.androidx.work.runtime.ktx)
```
- **Purpose**: Deferrable background tasks
- **Usage**: Data synchronization, periodic tasks
- **Features**: Guaranteed execution, constraints, chaining
- **Example**: Sync pending expenses when connected to WiFi
- **Benefits**: Battery efficient, respects system constraints

---

## Security

### Security Crypto (1.1.0-alpha06)
```kotlin
implementation(libs.androidx.security.crypto)
```
- **Purpose**: Encryption and secure storage
- **Usage**: Encrypt SharedPreferences, files
- **Use Case**: Store JWT tokens, sensitive user data
- **Technology**: Uses Android Keystore System
- **Example**: EncryptedSharedPreferences for tokens

---

## Charts & Visualization

### MPAndroidChart (3.1.0)
```kotlin
implementation(libs.mpandroidchart)
```
- **Purpose**: Powerful charting library
- **Usage**: Financial charts, spending visualization
- **Chart Types**:
  - PieChart (spending by category)
  - LineChart (monthly trend)
  - BarChart (income vs expenses)
  - CombinedChart (multiple data sets)
- **Features**: Animations, touch gestures, customization

---

## Image Loading

### Coil (2.5.0)
```kotlin
implementation(libs.coil)
```
- **Purpose**: Image loading library for Android
- **Usage**: Load category icons, future profile pictures
- **Features**: Kotlin-first, coroutines, caching
- **Benefits**: Lightweight, fast, modern API
- **Example**: `imageView.load(url)`

---

## Logging

### Timber (5.0.1)
```kotlin
implementation(libs.timber)
```
- **Purpose**: Logging utility
- **Usage**: Debug logging throughout app
- **Features**: Tag generation, log trees
- **Example**: `Timber.d("Expense created: $expense")`
- **Production**: Plant release tree (no debug logs)

---

## Testing

### Unit Testing

#### JUnit (4.13.2)
```kotlin
testImplementation(libs.junit)
```
- **Purpose**: Unit testing framework
- **Usage**: Test use cases, ViewModels, utilities
- **Example**: Test business logic in isolation

#### MockK (1.13.8)
```kotlin
testImplementation(libs.mockk)
androidTestImplementation(libs.mockk)
```
- **Purpose**: Mocking library for Kotlin
- **Usage**: Mock dependencies in tests
- **Example**: Mock repository, API, database
- **Features**: Kotlin-friendly, coroutines support

#### Coroutines Test (1.7.3)
```kotlin
testImplementation(libs.kotlinx.coroutines.test)
```
- **Purpose**: Test coroutines
- **Usage**: Test suspend functions, flows
- **Features**: TestDispatcher, runTest

#### Turbine (1.0.0)
```kotlin
testImplementation(libs.turbine)
```
- **Purpose**: Test Kotlin Flows easily
- **Usage**: Assert on Flow emissions
- **Example**: `flow.test { awaitItem() shouldBe expected }`

#### Truth (1.1.5)
```kotlin
testImplementation(libs.truth)
androidTestImplementation(libs.truth)
```
- **Purpose**: Fluent assertion library by Google
- **Usage**: More readable test assertions
- **Example**: `assertThat(result).isEqualTo(expected)`

#### Room Testing (2.6.1)
```kotlin
testImplementation(libs.androidx.room.testing)
```
- **Purpose**: Test Room databases
- **Usage**: In-memory database for tests
- **Example**: Test DAOs in isolation

### Android Testing (Instrumented)

#### AndroidX JUnit (1.1.5)
```kotlin
androidTestImplementation(libs.androidx.junit)
```
- **Purpose**: JUnit for Android tests
- **Usage**: Test on real/emulated devices
- **Example**: Test UI interactions

#### Espresso (3.5.1)
```kotlin
androidTestImplementation(libs.androidx.espresso.core)
```
- **Purpose**: UI testing framework
- **Usage**: Test user interactions, UI flows
- **Example**: Click button, verify text displayed

---

## Build Configuration

### Kotlin Version: 1.9.20
- **Target JVM**: 17
- **Compiler Options**: Opt-in for coroutines APIs
- **Kapt**: correctErrorTypes = true

### Android Configuration
- **Compile SDK**: 34
- **Min SDK**: 24 (Android 7.0 Nougat - 94% devices)
- **Target SDK**: 34 (Android 14)
- **Java Version**: 17

### Build Features
- **ViewBinding**: Enabled (type-safe view access)
- **BuildConfig**: Enabled (API URL, version)
- **Compose**: Disabled (not using Compose)

### Build Variants

#### Debug
- **Application ID**: `com.example.smartbudget.debug`
- **API URL**: `http://10.0.2.2:8080/api/v1/` (localhost for emulator)
- **Minification**: Disabled
- **Debuggable**: Yes
- **Logging**: Full logging with Timber

#### Release
- **Application ID**: `com.example.smartbudget`
- **API URL**: `https://api.smartbudget.app/api/v1/`
- **Minification**: Enabled (ProGuard)
- **Resource Shrinking**: Enabled
- **Debuggable**: No
- **Logging**: Release tree (no debug logs)

---

## ProGuard Configuration

### Included Rules
- Kotlin standard library
- Coroutines
- Retrofit + OkHttp
- Gson serialization
- Room database
- Hilt dependency injection
- MPAndroidChart
- Data models (keep all)
- Remove logging in release

### What Gets Obfuscated
- Internal implementation code
- Private methods and fields
- Non-exported classes

### What Stays Intact
- Public API classes
- Data models (DTOs, entities)
- Retrofit interfaces
- Room entities and DAOs
- Parcelable implementations

---

## Dependency Size Impact

### Large Dependencies
1. **Material Components** (~3 MB): UI components
2. **MPAndroidChart** (~1 MB): Charts library
3. **Hilt** (~500 KB): DI framework
4. **Room** (~400 KB): Database
5. **Retrofit + OkHttp** (~600 KB): Networking

### Total APK Size Estimate
- **Debug**: ~15-20 MB (unminified)
- **Release**: ~8-12 MB (minified + shrunk)

### Optimization Strategies
- ProGuard removes unused code
- Resource shrinking removes unused resources
- APK splits for different architectures (future)

---

## Adding New Dependencies

### Process
1. Add version to `libs.versions.toml`:
   ```toml
   [versions]
   newLib = "1.0.0"
   ```

2. Add library to `libs.versions.toml`:
   ```toml
   [libraries]
   new-lib = { group = "com.example", name = "lib", version.ref = "newLib" }
   ```

3. Add to `app/build.gradle.kts`:
   ```kotlin
   implementation(libs.new.lib)
   ```

4. Sync Gradle

### When to Add Dependencies
- ✅ Solves a real problem
- ✅ Maintained and popular
- ✅ Has good documentation
- ✅ Reasonable size
- ❌ Just for experimentation
- ❌ Unmaintained libraries
- ❌ Duplicates existing functionality

---

## Dependency Updates

### Update Strategy
- **Security updates**: Immediately
- **Major versions**: Review changelog, test thoroughly
- **Minor versions**: Monthly update cycle
- **Patch versions**: As needed

### Tools
- **Gradle Version Catalog**: Centralized management
- **Dependabot**: Automated PR for updates (GitHub)
- **Gradle Versions Plugin**: Check for updates

### Testing After Updates
1. Run unit tests
2. Run instrumented tests
3. Manual testing on debug build
4. Test release build
5. Monitor crash reports

---

## Common Issues

### Multidex Required
If APK exceeds 64K methods:
```kotlin
defaultConfig {
    multiDexEnabled = true
}
dependencies {
    implementation("androidx.multidex:multidex:2.0.1")
}
```

### Duplicate Classes
Use `exclude` in dependencies:
```kotlin
implementation(libs.some.lib) {
    exclude(group = "com.example", module = "conflicting-module")
}
```

### ProGuard Issues
Add specific keep rules to `proguard-rules.pro`

---

## Further Reading

- [Android Developers - Dependencies](https://developer.android.com/studio/build/dependencies)
- [Gradle Version Catalogs](https://docs.gradle.org/current/userguide/platforms.html)
- [ProGuard Manual](https://www.guardsquare.com/manual/home)
- [Hilt Documentation](https://dagger.dev/hilt/)
- [Retrofit Documentation](https://square.github.io/retrofit/)
- [Room Documentation](https://developer.android.com/training/data-storage/room)
