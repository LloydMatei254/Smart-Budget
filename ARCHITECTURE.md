# Smart Budget - Android Application Architecture

## Overview

Smart Budget follows a **Clean Architecture** approach with **MVVM (Model-View-ViewModel)** pattern, ensuring clear separation of concerns, testability, and maintainability.

## Architecture Layers

```
┌─────────────────────────────────────────────────────┐
│                 PRESENTATION LAYER                  │
│  (Activities, Fragments, ViewModels, UI State)     │
└────────────────┬────────────────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────────────────┐
│                  DOMAIN LAYER                       │
│      (Models, Use Cases, Repository Interfaces)     │
└────────────────┬────────────────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────────────────┐
│                   DATA LAYER                        │
│  (Repository Impl, API, Database, DTOs, Entities)  │
└─────────────────────────────────────────────────────┘
```

## Project Structure

```
app/src/main/java/com/example/smartbudget/
│
├── data/                          # DATA LAYER
│   ├── local/                    # Local data sources (Room)
│   │   ├── dao/                  # Data Access Objects
│   │   │   ├── ExpenseDao.kt
│   │   │   ├── IncomeDao.kt
│   │   │   ├── CategoryDao.kt
│   │   │   ├── PaymentMethodDao.kt
│   │   │   └── UserDao.kt
│   │   ├── database/             # Database configuration
│   │   │   ├── SmartBudgetDatabase.kt
│   │   │   └── DatabaseMigrations.kt
│   │   └── entities/             # Room entities
│   │       ├── ExpenseEntity.kt
│   │       ├── IncomeEntity.kt
│   │       ├── CategoryEntity.kt
│   │       ├── PaymentMethodEntity.kt
│   │       └── UserEntity.kt
│   │
│   ├── remote/                   # Remote data sources (API)
│   │   ├── api/                  # Retrofit interfaces
│   │   │   ├── AuthApi.kt
│   │   │   ├── ExpensesApi.kt
│   │   │   ├── IncomeApi.kt
│   │   │   ├── CategoriesApi.kt
│   │   │   ├── PaymentMethodsApi.kt
│   │   │   ├── ReportsApi.kt
│   │   │   └── SyncApi.kt
│   │   ├── dto/                  # Data Transfer Objects
│   │   │   ├── request/
│   │   │   │   ├── LoginRequest.kt
│   │   │   │   ├── RegisterRequest.kt
│   │   │   │   ├── CreateExpenseRequest.kt
│   │   │   │   └── ...
│   │   │   └── response/
│   │   │       ├── AuthResponse.kt
│   │   │       ├── ExpenseResponse.kt
│   │   │       ├── ApiResponse.kt
│   │   │       └── ...
│   │   └── interceptors/         # HTTP interceptors
│   │       ├── AuthInterceptor.kt
│   │       └── ErrorInterceptor.kt
│   │
│   └── repository/               # Repository implementations
│       ├── AuthRepositoryImpl.kt
│       ├── ExpenseRepositoryImpl.kt
│       ├── IncomeRepositoryImpl.kt
│       ├── CategoryRepositoryImpl.kt
│       └── SyncRepositoryImpl.kt
│
├── domain/                        # DOMAIN LAYER
│   ├── model/                    # Domain models (business objects)
│   │   ├── User.kt
│   │   ├── Expense.kt
│   │   ├── Income.kt
│   │   ├── Category.kt
│   │   ├── PaymentMethod.kt
│   │   ├── FinancialSummary.kt
│   │   └── SyncStatus.kt
│   │
│   ├── repository/               # Repository interfaces
│   │   ├── AuthRepository.kt
│   │   ├── ExpenseRepository.kt
│   │   ├── IncomeRepository.kt
│   │   ├── CategoryRepository.kt
│   │   └── SyncRepository.kt
│   │
│   └── usecase/                  # Business logic use cases
│       ├── auth/
│       │   ├── LoginUseCase.kt
│       │   ├── RegisterUseCase.kt
│       │   ├── LogoutUseCase.kt
│       │   └── GetCurrentUserUseCase.kt
│       ├── expense/
│       │   ├── GetExpensesUseCase.kt
│       │   ├── CreateExpenseUseCase.kt
│       │   ├── UpdateExpenseUseCase.kt
│       │   └── DeleteExpenseUseCase.kt
│       ├── income/
│       │   └── ...
│       ├── report/
│       │   ├── GetFinancialSummaryUseCase.kt
│       │   ├── GetSpendingByCategoryUseCase.kt
│       │   └── GetMonthlyTrendUseCase.kt
│       └── sync/
│           └── SyncDataUseCase.kt
│
├── presentation/                  # PRESENTATION LAYER
│   ├── auth/                     # Authentication screens
│   │   ├── login/
│   │   │   ├── LoginFragment.kt
│   │   │   ├── LoginViewModel.kt
│   │   │   └── LoginUiState.kt
│   │   └── register/
│   │       ├── RegisterFragment.kt
│   │       ├── RegisterViewModel.kt
│   │       └── RegisterUiState.kt
│   │
│   ├── dashboard/                # Dashboard screen
│   │   ├── DashboardFragment.kt
│   │   ├── DashboardViewModel.kt
│   │   ├── DashboardUiState.kt
│   │   └── adapter/
│   │       └── RecentTransactionsAdapter.kt
│   │
│   ├── expenses/                 # Expense management
│   │   ├── list/
│   │   │   ├── ExpenseListFragment.kt
│   │   │   ├── ExpenseListViewModel.kt
│   │   │   ├── ExpenseListUiState.kt
│   │   │   └── adapter/
│   │   │       └── ExpenseAdapter.kt
│   │   ├── add/
│   │   │   ├── AddExpenseFragment.kt
│   │   │   ├── AddExpenseViewModel.kt
│   │   │   └── AddExpenseUiState.kt
│   │   └── detail/
│   │       ├── ExpenseDetailFragment.kt
│   │       ├── ExpenseDetailViewModel.kt
│   │       └── ExpenseDetailUiState.kt
│   │
│   ├── income/                   # Income management
│   │   ├── list/
│   │   ├── add/
│   │   └── detail/
│   │
│   ├── transactions/             # Transaction history
│   │   ├── TransactionsFragment.kt
│   │   ├── TransactionsViewModel.kt
│   │   ├── TransactionsUiState.kt
│   │   └── adapter/
│   │       └── TransactionAdapter.kt
│   │
│   ├── categories/               # Category management
│   │   ├── list/
│   │   │   ├── CategoryListFragment.kt
│   │   │   └── CategoryListViewModel.kt
│   │   └── manage/
│   │       ├── ManageCategoryFragment.kt
│   │       └── ManageCategoryViewModel.kt
│   │
│   ├── reports/                  # Reports and analytics
│   │   ├── ReportsFragment.kt
│   │   ├── ReportsViewModel.kt
│   │   └── ReportsUiState.kt
│   │
│   ├── settings/                 # Settings screen
│   │   ├── SettingsFragment.kt
│   │   └── SettingsViewModel.kt
│   │
│   └── common/                   # Shared UI components
│       ├── BaseFragment.kt
│       ├── BaseViewModel.kt
│       ├── UiState.kt
│       └── ViewBindingExtensions.kt
│
├── di/                           # DEPENDENCY INJECTION
│   ├── AppModule.kt             # Application-level dependencies
│   ├── NetworkModule.kt         # Retrofit, OkHttp
│   ├── DatabaseModule.kt        # Room database
│   ├── RepositoryModule.kt      # Repository bindings
│   └── UseCaseModule.kt         # Use case bindings
│
└── utils/                        # UTILITIES
    ├── Constants.kt             # App constants
    ├── DateUtils.kt             # Date formatting
    ├── CurrencyUtils.kt         # Currency formatting
    ├── ValidationUtils.kt       # Input validation
    ├── NetworkUtils.kt          # Network state checking
    ├── PreferencesManager.kt    # SharedPreferences wrapper
    ├── SecureStorage.kt         # Android Keystore wrapper
    └── Extensions.kt            # Kotlin extensions
```

## Architecture Principles

### 1. Separation of Concerns
Each layer has a single, well-defined responsibility:
- **Presentation**: Display data and handle user interactions
- **Domain**: Business logic and business rules
- **Data**: Data fetching and caching

### 2. Dependency Rule
Dependencies flow inward:
- **Presentation** depends on **Domain**
- **Data** implements **Domain** interfaces
- **Domain** has no dependencies on other layers

### 3. Testability
- Each layer can be tested independently
- Use cases contain testable business logic
- Repositories can be mocked for ViewModel tests
- ViewModels can be tested without Android framework

### 4. Scalability
- New features are added by creating new use cases and ViewModels
- Existing code rarely needs modification
- Clean boundaries between modules

## Data Flow

### Reading Data (e.g., Fetching Expenses)

```
User Action (Click)
    ↓
Fragment observes ViewModel
    ↓
ViewModel calls Use Case
    ↓
Use Case calls Repository (Interface)
    ↓
Repository Implementation coordinates:
    ├─ Check Room Cache (Local)
    └─ Fetch from API (Remote)
    ↓
Repository returns Domain Model
    ↓
Use Case processes/validates
    ↓
ViewModel updates UI State
    ↓
Fragment observes state change
    ↓
UI Updates
```

### Writing Data (e.g., Creating Expense)

```
User Action (Submit Form)
    ↓
Fragment calls ViewModel method
    ↓
ViewModel validates input
    ↓
ViewModel calls Use Case
    ↓
Use Case applies business rules
    ↓
Repository saves to:
    ├─ Local Database (Room) - immediately
    └─ Remote API - with sync status
    ↓
Repository returns Result
    ↓
ViewModel updates UI State
    ↓
Fragment shows success/error
    ↓
Background sync worker handles API sync
```

## Key Components

### 1. UI State Pattern

All ViewModels expose sealed UI state classes:

```kotlin
sealed interface ExpenseListUiState {
    data object Loading : ExpenseListUiState
    data class Success(val expenses: List<Expense>) : ExpenseListUiState
    data class Error(val message: String) : ExpenseListUiState
    data object Empty : ExpenseListUiState
}
```

### 2. Repository Pattern

Repositories coordinate between local and remote data sources:

```kotlin
interface ExpenseRepository {
    suspend fun getExpenses(filters: ExpenseFilters): Result<List<Expense>>
    suspend fun createExpense(expense: Expense): Result<Expense>
    suspend fun updateExpense(expense: Expense): Result<Expense>
    suspend fun deleteExpense(id: String): Result<Unit>
}

class ExpenseRepositoryImpl(
    private val remoteApi: ExpensesApi,
    private val localDao: ExpenseDao,
    private val mapper: ExpenseMapper
) : ExpenseRepository {
    // Offline-first implementation
    override suspend fun getExpenses(filters: ExpenseFilters): Result<List<Expense>> {
        return try {
            // Try API first
            val response = remoteApi.getExpenses(filters)
            // Cache to Room
            localDao.insertAll(response.map { mapper.toEntity(it) })
            // Return domain models
            Result.success(response.map { mapper.toDomain(it) })
        } catch (e: Exception) {
            // Fallback to cache
            Result.success(localDao.getAll().map { mapper.toDomain(it) })
        }
    }
}
```

### 3. Use Case Pattern

Use cases encapsulate single business operations:

```kotlin
class CreateExpenseUseCase(
    private val repository: ExpenseRepository,
    private val validator: ExpenseValidator
) {
    suspend operator fun invoke(expense: Expense): Result<Expense> {
        // Validate
        val validationResult = validator.validate(expense)
        if (validationResult.hasErrors()) {
            return Result.failure(ValidationException(validationResult.errors))
        }
        
        // Business logic
        val processedExpense = expense.copy(
            syncStatus = SyncStatus.PENDING
        )
        
        // Execute
        return repository.createExpense(processedExpense)
    }
}
```

### 4. ViewModel Pattern

ViewModels manage UI state and coordinate use cases:

```kotlin
class ExpenseListViewModel(
    private val getExpensesUseCase: GetExpensesUseCase,
    private val deleteExpenseUseCase: DeleteExpenseUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<ExpenseListUiState>(ExpenseListUiState.Loading)
    val uiState: StateFlow<ExpenseListUiState> = _uiState.asStateFlow()
    
    fun loadExpenses(filters: ExpenseFilters = ExpenseFilters()) {
        viewModelScope.launch {
            _uiState.value = ExpenseListUiState.Loading
            
            getExpensesUseCase(filters)
                .onSuccess { expenses ->
                    _uiState.value = if (expenses.isEmpty()) {
                        ExpenseListUiState.Empty
                    } else {
                        ExpenseListUiState.Success(expenses)
                    }
                }
                .onFailure { error ->
                    _uiState.value = ExpenseListUiState.Error(error.message ?: "Unknown error")
                }
        }
    }
    
    fun deleteExpense(id: String) {
        viewModelScope.launch {
            deleteExpenseUseCase(id)
                .onSuccess { loadExpenses() }
                .onFailure { /* handle error */ }
        }
    }
}
```

### 5. Fragment/Activity Pattern

UI components observe state and forward user actions:

```kotlin
class ExpenseListFragment : Fragment() {
    private var _binding: FragmentExpenseListBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: ExpenseListViewModel by viewModels()
    private val adapter = ExpenseAdapter()
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        observeUiState()
        
        viewModel.loadExpenses()
    }
    
    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is ExpenseListUiState.Loading -> showLoading()
                    is ExpenseListUiState.Success -> showExpenses(state.expenses)
                    is ExpenseListUiState.Error -> showError(state.message)
                    is ExpenseListUiState.Empty -> showEmpty()
                }
            }
        }
    }
}
```

## Offline-First Strategy

### Synchronization Approach

1. **Write Operations**:
   - Save to Room immediately with `syncStatus = PENDING`
   - Update UI immediately (optimistic updates)
   - Enqueue sync worker
   - Worker syncs with API in background
   - Update `syncStatus = SYNCED` on success

2. **Read Operations**:
   - Check Room cache first
   - Display cached data immediately
   - Fetch from API in background
   - Update cache
   - Update UI with fresh data

3. **Conflict Resolution**:
   - Last-write-wins based on `syncVersion`
   - Server version > client version = conflict
   - Present conflict UI to user for manual resolution

### Sync Worker

```kotlin
class SyncWorker(
    context: Context,
    params: WorkerParameters,
    private val syncRepository: SyncRepository
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        return try {
            syncRepository.syncPendingChanges()
            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
}
```

## Dependency Injection with Hilt

### Module Organization

- **AppModule**: Application, Context, Dispatchers
- **NetworkModule**: Retrofit, OkHttp, API services
- **DatabaseModule**: Room database, DAOs
- **RepositoryModule**: Repository implementations
- **UseCaseModule**: Use case instances

### Example Module

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    @Provides
    @Singleton
    fun provideExpensesApi(retrofit: Retrofit): ExpensesApi {
        return retrofit.create(ExpensesApi::class.java)
    }
}
```

## Navigation

Using **Navigation Component**:

```xml
<!-- nav_graph.xml -->
<navigation>
    <fragment id="@+id/loginFragment" />
    <fragment id="@+id/dashboardFragment" />
    <fragment id="@+id/expenseListFragment" />
    <fragment id="@+id/addExpenseFragment" />
    <!-- etc -->
</navigation>
```

## Error Handling

### Consistent Error Handling

```kotlin
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Exception) : Result<Nothing>()
}

sealed class AppException : Exception() {
    data class NetworkException(override val message: String) : AppException()
    data class AuthException(override val message: String) : AppException()
    data class ValidationException(val errors: Map<String, String>) : AppException()
    data class ServerException(val code: Int, override val message: String) : AppException()
}
```

## Testing Strategy

### Unit Tests
- **Use Cases**: Test business logic
- **ViewModels**: Test state management
- **Repositories**: Test data coordination (with mocked APIs and DAOs)

### Integration Tests
- **Database**: Test Room DAOs
- **API**: Test Retrofit services (with MockWebServer)

### UI Tests
- **Fragments**: Test user interactions (with Espresso)
- **Navigation**: Test screen flows

## Performance Considerations

1. **Pagination**: Implement paging for large lists
2. **Caching**: Cache API responses in Room
3. **Image Loading**: Use Coil/Glide for efficient image loading
4. **Background Work**: Use WorkManager for deferrable tasks
5. **Memory Leaks**: Use ViewBinding and proper lifecycle management

## Security Considerations

1. **Token Storage**: Store JWT tokens in Android Keystore
2. **Network**: Use HTTPS only, certificate pinning for production
3. **Sensitive Data**: Never log passwords or tokens
4. **Input Validation**: Validate on both client and server
5. **ProGuard**: Obfuscate code in release builds

## Build Variants

```
- debug: Development build with logging
- release: Production build with ProGuard
```

## Next Steps

1. ✅ Project structure created
2. ⏳ Add Gradle dependencies
3. ⏳ Implement domain models
4. ⏳ Set up dependency injection
5. ⏳ Implement data layer (Room + Retrofit)
6. ⏳ Implement use cases
7. ⏳ Implement ViewModels
8. ⏳ Implement UI screens
9. ⏳ Add tests
10. ⏳ Deploy backend API

---

## References

- [Android Architecture Guide](https://developer.android.com/topic/architecture)
- [Guide to app architecture](https://developer.android.com/topic/architecture)
- [MVVM Pattern](https://en.wikipedia.org/wiki/Model%E2%80%93view%E2%80%93viewmodel)
- [Clean Architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
