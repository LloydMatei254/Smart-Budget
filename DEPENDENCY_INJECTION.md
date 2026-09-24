# Smart Budget - Dependency Injection with Hilt

## Overview

Smart Budget uses **Hilt** (built on Dagger) for dependency injection, providing a standardized way to manage dependencies throughout the application.

## Why Hilt?

- **Android-Specific**: Built specifically for Android by Google
- **Less Boilerplate**: Reduces setup code compared to raw Dagger
- **Lifecycle-Aware**: Automatic integration with Android lifecycle
- **Compile-Time Safety**: Errors caught at compile time
- **Standard Components**: Predefined components for Activities, Fragments, etc.
- **WorkManager Integration**: Built-in support for injecting into Workers

## Architecture

```
Application (@HiltAndroidApp)
    ↓
Hilt Modules (@Module + @InstallIn)
    ├── AppModule (Application-level dependencies)
    ├── NetworkModule (Retrofit, OkHttp, API services)
    ├── DatabaseModule (Room, DAOs)
    ├── RepositoryModule (Repository implementations)
    └── WorkerModule (WorkManager)
    ↓
Injectable Classes (@AndroidEntryPoint, @Inject)
    ├── Activities
    ├── Fragments
    ├── ViewModels
    └── Workers
```

---

## Setup

### 1. Application Class

```kotlin
@HiltAndroidApp
class SmartBudgetApplication : Application() {
    // Hilt generates code based on this annotation
}
```

### 2. AndroidManifest.xml

```xml
<application
    android:name=".SmartBudgetApplication"
    ...>
</application>
```

---

## Hilt Modules

### AppModule

**Purpose**: Provides application-level dependencies

**Location**: `di/AppModule.kt`

**Provides**:
- `Context` (Application Context)
- `DataStore<Preferences>` (for non-sensitive preferences)
- `PreferencesManager` (wrapper around DataStore)
- `SecureStorage` (EncryptedSharedPreferences for tokens)
- `CoroutineDispatcher` (IO, Default, Main)

**Usage**:
```kotlin
@Inject
lateinit var preferencesManager: PreferencesManager

@Inject
lateinit var secureStorage: SecureStorage

@Inject
@IoDispatcher
lateinit var ioDispatcher: CoroutineDispatcher
```

---

### NetworkModule

**Purpose**: Provides networking dependencies

**Location**: `di/NetworkModule.kt`

**Provides**:
- `Gson` (JSON serialization)
- `HttpLoggingInterceptor` (logs HTTP requests/responses in debug)
- `OkHttpClient` (HTTP client with interceptors)
- `Retrofit` (REST client)
- API Services:
  - `AuthApi`
  - `ExpensesApi`
  - `IncomeApi`
  - `CategoriesApi`
  - `PaymentMethodsApi`
  - `ReportsApi`
  - `SyncApi`

**Configuration**:
- **Base URL**: From BuildConfig (different for debug/release)
- **Timeouts**: 30 seconds for connect/read/write
- **Retry**: Enabled on connection failure
- **Logging**: Full body logging in debug, none in release

**Interceptors**:
1. **AuthInterceptor**: Adds `Authorization: Bearer <token>` header
2. **ErrorInterceptor**: Handles common HTTP errors
3. **HttpLoggingInterceptor**: Logs requests/responses

**Usage**:
```kotlin
@Inject
lateinit var expensesApi: ExpensesApi

@Inject
lateinit var authApi: AuthApi
```

---

### DatabaseModule

**Purpose**: Provides Room database and DAOs

**Location**: `di/DatabaseModule.kt`

**Provides**:
- `SmartBudgetDatabase` (Room database instance)
- `UserDao`
- `ExpenseDao`
- `IncomeDao`
- `CategoryDao`
- `PaymentMethodDao`

**Database Configuration**:
- **Name**: `smart_budget_database`
- **Migration Strategy**: `fallbackToDestructiveMigration()` (TODO: Implement proper migrations for production)

**Usage**:
```kotlin
@Inject
lateinit var expenseDao: ExpenseDao

@Inject
lateinit var categoryDao: CategoryDao
```

---

### RepositoryModule

**Purpose**: Provides repository implementations

**Location**: `di/RepositoryModule.kt`

**Provides**:
- `AuthRepository`
- `ExpenseRepository`
- `IncomeRepository`
- `CategoryRepository`
- `PaymentMethodRepository`
- `ReportRepository`
- `SyncRepository`

**Pattern**: Binds concrete implementations to interfaces

**Usage**:
```kotlin
@Inject
lateinit var expenseRepository: ExpenseRepository

@Inject
lateinit var authRepository: AuthRepository
```

---

### WorkerModule

**Purpose**: Provides WorkManager

**Location**: `di/WorkerModule.kt`

**Provides**:
- `WorkManager` instance

**Usage**:
```kotlin
@Inject
lateinit var workManager: WorkManager
```

---

## Injectable Components

### Activities

**Annotation**: `@AndroidEntryPoint`

```kotlin
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    
    @Inject
    lateinit var preferencesManager: PreferencesManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // preferencesManager is injected automatically
    }
}
```

---

### Fragments

**Annotation**: `@AndroidEntryPoint`

```kotlin
@AndroidEntryPoint
class ExpenseListFragment : Fragment() {
    
    private val viewModel: ExpenseListViewModel by viewModels()
    
    @Inject
    lateinit var repository: ExpenseRepository
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Dependencies injected automatically
    }
}
```

---

### ViewModels

**Annotation**: `@HiltViewModel`

```kotlin
@HiltViewModel
class ExpenseListViewModel @Inject constructor(
    private val getExpensesUseCase: GetExpensesUseCase,
    private val deleteExpenseUseCase: DeleteExpenseUseCase,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {
    // Dependencies injected via constructor
}
```

**Fragment Usage**:
```kotlin
private val viewModel: ExpenseListViewModel by viewModels()
```

---

### Workers

**Annotation**: `@HiltWorker`

```kotlin
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val syncRepository: SyncRepository
) : CoroutineWorker(context, workerParams) {
    
    override suspend fun doWork(): Result {
        return try {
            syncRepository.syncPendingChanges()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
```

---

## Custom Qualifiers

### Dispatcher Qualifiers

Used to distinguish between different CoroutineDispatchers:

```kotlin
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DefaultDispatcher

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MainDispatcher
```

**Usage**:
```kotlin
@HiltViewModel
class MyViewModel @Inject constructor(
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {
    
    fun loadData() {
        viewModelScope.launch(ioDispatcher) {
            // Background operation
        }
    }
}
```

---

## Scopes

### @Singleton

Lives for the entire application lifetime:

```kotlin
@Provides
@Singleton
fun provideRetrofit(): Retrofit { ... }
```

Used for:
- Database
- Retrofit
- API services
- Repositories
- Managers

### @ViewModelScoped

Lives as long as the ViewModel:

```kotlin
@Provides
@ViewModelScoped
fun provideUseCaseInstance(): UseCase { ... }
```

### @ActivityScoped / @FragmentScoped

Lives as long as the Activity/Fragment:

```kotlin
@Provides
@ActivityScoped
fun provideSomeHelper(): Helper { ... }
```

---

## Testing with Hilt

### Unit Tests

Use `@HiltAndroidTest` and `HiltTestApplication`:

```kotlin
@HiltAndroidTest
class ExpenseRepositoryTest {
    
    @get:Rule
    var hiltRule = HiltAndroidRule(this)
    
    @Inject
    lateinit var repository: ExpenseRepository
    
    @Before
    fun init() {
        hiltRule.inject()
    }
    
    @Test
    fun testGetExpenses() {
        // Test with injected repository
    }
}
```

### Replacing Modules

Create test modules:

```kotlin
@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [NetworkModule::class]
)
object TestNetworkModule {
    
    @Provides
    @Singleton
    fun provideTestExpensesApi(): ExpensesApi {
        return FakeExpensesApi()
    }
}
```

---

## Common Patterns

### Constructor Injection (Preferred)

```kotlin
class MyRepository @Inject constructor(
    private val api: MyApi,
    private val dao: MyDao
) {
    // Use api and dao
}
```

### Field Injection

```kotlin
@AndroidEntryPoint
class MyActivity : AppCompatActivity() {
    
    @Inject
    lateinit var repository: MyRepository
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // repository is injected
    }
}
```

### Provider Injection

When you need to create instances manually:

```kotlin
class MyClass @Inject constructor(
    private val repositoryProvider: Provider<MyRepository>
) {
    
    fun doSomething() {
        val repository = repositoryProvider.get()
        // Use repository
    }
}
```

---

## Dependency Graph

```
SmartBudgetApplication
    ↓
SingletonComponent
    ├── Retrofit
    │   └── API Services (AuthApi, ExpensesApi, etc.)
    ├── Room Database
    │   └── DAOs (ExpenseDao, IncomeDao, etc.)
    ├── Repositories
    │   ├── AuthRepository
    │   ├── ExpenseRepository
    │   └── ...
    ├── Managers
    │   ├── PreferencesManager
    │   └── SecureStorage
    └── Dispatchers
        ├── IO
        ├── Default
        └── Main
    ↓
ViewModelComponent
    └── ViewModels (ExpenseListViewModel, etc.)
    ↓
ActivityComponent / FragmentComponent
    └── UI Components
```

---

## Best Practices

### 1. Constructor Injection First
- Prefer constructor injection over field injection
- Makes dependencies explicit
- Easier to test

### 2. Use Interfaces
- Inject interfaces, not implementations
- Allows easy mocking in tests
- Follows Dependency Inversion Principle

### 3. Scope Appropriately
- Don't make everything @Singleton
- Use appropriate scopes to avoid memory leaks
- Let Hilt manage lifecycles

### 4. Avoid Circular Dependencies
- If A depends on B and B depends on A, refactor
- Use Provider<T> as last resort
- Usually indicates design issue

### 5. Keep Modules Focused
- One module per layer (network, database, etc.)
- Don't mix concerns
- Makes code easier to understand

### 6. Document Complex Bindings
- Add comments for non-obvious dependencies
- Explain why certain configurations exist

---

## Troubleshooting

### "Cannot be provided without an @Inject constructor"

**Problem**: Hilt doesn't know how to create the class

**Solution**: Add `@Inject constructor` or provide it in a module

```kotlin
// Option 1: Add @Inject
class MyClass @Inject constructor(...)

// Option 2: Provide in module
@Provides
fun provideMyClass(): MyClass = MyClass(...)
```

### "Binding already exists"

**Problem**: Multiple modules provide the same type

**Solution**: Use qualifiers to distinguish them

```kotlin
@Qualifier
annotation class ApiBaseUrl

@Provides
@ApiBaseUrl
fun provideBaseUrl(): String = "https://api.example.com"
```

### "MissingBinding"

**Problem**: Required dependency not provided

**Solution**: Add the dependency to a module or add @Inject constructor

### "Activity/Fragment must be attached to an @AndroidEntryPoint"

**Problem**: Parent Activity not annotated

**Solution**: Add @AndroidEntryPoint to Activity

```kotlin
@AndroidEntryPoint
class MainActivity : AppCompatActivity()
```

---

## Migration from Manual DI

### Before (Manual DI)

```kotlin
class ExpenseRepository(
    private val api: ExpensesApi,
    private val dao: ExpenseDao
) {
    companion object {
        @Volatile
        private var INSTANCE: ExpenseRepository? = null
        
        fun getInstance(api: ExpensesApi, dao: ExpenseDao): ExpenseRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ExpenseRepository(api, dao).also { INSTANCE = it }
            }
        }
    }
}
```

### After (Hilt)

```kotlin
class ExpenseRepository @Inject constructor(
    private val api: ExpensesApi,
    private val dao: ExpenseDao
) {
    // No singleton boilerplate needed
}
```

---

## Performance Considerations

### Build Time
- Hilt generates code at compile time
- First build is slower
- Incremental builds are fast

### Runtime
- No reflection (unlike manual dependency injection)
- Minimal overhead
- Optimized by ProGuard/R8

### APK Size
- Hilt adds ~100KB to APK
- Generated code is optimized
- ProGuard removes unused code

---

## Resources

- [Hilt Official Documentation](https://dagger.dev/hilt/)
- [Android Dependency Injection](https://developer.android.com/training/dependency-injection/hilt-android)
- [Hilt Codelab](https://developer.android.com/codelabs/android-hilt)
- [Hilt Testing Guide](https://developer.android.com/training/dependency-injection/hilt-testing)

---

## Summary

Hilt provides:
- ✅ Standardized DI for Android
- ✅ Compile-time safety
- ✅ Reduced boilerplate
- ✅ Lifecycle integration
- ✅ Easy testing
- ✅ Generated code documentation

All dependencies in Smart Budget are managed through Hilt, ensuring:
- Testable code
- Clear dependency graphs
- Proper lifecycle management
- Easy mocking for tests
- Maintainable architecture
