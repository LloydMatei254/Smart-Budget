# Smart Budget - UI State Management Architecture

## Overview
Smart Budget implements a robust UI state management system based on MVVM pattern with reactive data flows using Kotlin Flow and ViewBinding.

## Architecture Components

### 1. UiState<T>
**Location**: `presentation/common/UiState.kt`

Sealed class representing the state of any UI operation:

```kotlin
sealed class UiState<out T> {
    object Idle : UiState<Nothing>()
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String, val throwable: Throwable?) : UiState<Nothing>()
}
```

**Usage in ViewModel:**
```kotlin
private val _expenses = MutableStateFlow<UiState<List<Expense>>>(UiState.Idle)
val expenses: StateFlow<UiState<List<Expense>>> = _expenses.asStateFlow()

fun loadExpenses() {
    executeWithState(_expenses) {
        expenseRepository.getExpenses()
    }
}
```

**Usage in Fragment:**
```kotlin
viewModel.expenses.collectInLifecycle(viewLifecycleOwner) { state ->
    state.onState(
        onLoading = { showLoading() },
        onSuccess = { expenses -> displayExpenses(expenses) },
        onError = { message -> showError(message) }
    )
}
```

### 2. UiEvent
**Location**: `presentation/common/UiEvent.kt`

Sealed class for one-time UI events (navigation, toasts, dialogs):

```kotlin
sealed class UiEvent {
    data class ShowToast(val message: String) : UiEvent()
    data class ShowSnackbar(...) : UiEvent()
    data class Navigate(val route: String) : UiEvent()
    object NavigateBack : UiEvent()
    data class ShowError(...) : UiEvent()
    data class ShowConfirmation(...) : UiEvent()
    data class ShowLoading(...) : UiEvent()
    object HideLoading : UiEvent()
}
```

**Usage:**
```kotlin
// In ViewModel
protected fun sendEvent(event: UiEvent) {
    viewModelScope.launch {
        _uiEvent.emit(event)
    }
}

showToast("Expense saved successfully")
navigate("expense_detail/${expenseId}")

// In Fragment - automatically handled by BaseFragment
```

### 3. BaseViewModel
**Location**: `presentation/common/BaseViewModel.kt`

Abstract ViewModel providing common functionality:

**Features:**
- ✅ Automatic loading state management
- ✅ Error handling with retry logic
- ✅ Event emission for one-time actions
- ✅ Lifecycle-aware coroutine scopes
- ✅ Resource access via ResourceProvider

**Key Methods:**
```kotlin
// Execute with automatic loading state
protected fun <T> launchWithLoading(
    onError: ((Throwable) -> Unit)? = null,
    block: suspend () -> T
)

// Execute without loading indicator
protected fun <T> launchSilent(
    onError: ((Throwable) -> Unit)? = null,
    block: suspend () -> T
)

// Execute with UiState management
protected fun <T> executeWithState(
    stateFlow: MutableStateFlow<UiState<T>>,
    block: suspend () -> Result<T>
)

// Event helpers
protected fun showToast(message: String)
protected fun showSnackbar(message: String, ...)
protected fun showError(message: String, title: String)
protected fun navigate(route: String)
protected fun navigateBack()
```

**Example ViewModel:**
```kotlin
class ExpenseViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository
) : BaseViewModel() {
    
    private val _expenses = MutableStateFlow<UiState<List<Expense>>>(UiState.Idle)
    val expenses: StateFlow<UiState<List<Expense>>> = _expenses.asStateFlow()
    
    fun loadExpenses() {
        executeWithState(_expenses) {
            expenseRepository.getExpenses()
        }
    }
    
    fun deleteExpense(expenseId: String) {
        launchWithLoading {
            expenseRepository.deleteExpense(expenseId)
                .onSuccess {
                    showToast("Expense deleted")
                    loadExpenses() // Refresh list
                }
                .onFailure { error ->
                    showError(error.message ?: "Delete failed")
                }
        }
    }
}
```

### 4. BaseFragment<VB, VM>
**Location**: `presentation/common/BaseFragment.kt`

Abstract Fragment with ViewBinding and ViewModel support:

**Features:**
- ✅ Automatic ViewBinding setup/cleanup
- ✅ Automatic event collection
- ✅ Automatic loading state observation
- ✅ Toast/Snackbar helpers
- ✅ Dialog helpers
- ✅ Navigation helpers

**Required Implementations:**
```kotlin
class ExpenseListFragment : BaseFragment<FragmentExpenseListBinding, ExpenseViewModel>() {
    
    override val viewModel: ExpenseViewModel by viewModels()
    
    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentExpenseListBinding.inflate(inflater, container, false)
    
    override fun setupUI() {
        // Setup click listeners, RecyclerView, etc.
        binding.btnAddExpense.setOnClickListener {
            viewModel.navigateToAddExpense()
        }
    }
    
    override fun observeData() {
        viewModel.expenses.collectInLifecycle(viewLifecycleOwner) { state ->
            state.onState(
                onLoading = { showLoading() },
                onSuccess = { expenses -> updateExpenseList(expenses) },
                onError = { message -> showError(message) }
            )
        }
    }
    
    override fun onLoadingStateChanged(isLoading: Boolean) {
        binding.progressBar.setVisible(isLoading)
    }
}
```

### 5. Extension Functions

#### FlowExtensions.kt
```kotlin
// Lifecycle-aware Flow collection
fun <T> Flow<T>.collectInLifecycle(
    lifecycleOwner: LifecycleOwner,
    state: Lifecycle.State = Lifecycle.State.STARTED,
    collector: suspend (T) -> Unit
)

fun <T> Flow<T>.collectLatestInLifecycle(...)
```

#### ViewExtensions.kt
```kotlin
// View visibility
fun View.show()
fun View.hide()
fun View.setVisible(visible: Boolean)

// EditText helpers
fun EditText.textString(): String
fun EditText.onTextChanged(action: (String) -> Unit)
fun EditText.isEmpty(): Boolean

// View state
fun View.setEnabled(enabled: Boolean, disabledAlpha: Float = 0.5f)
```

#### UiState Extensions
```kotlin
// Transform UiState data
fun <T, R> UiState<T>.map(transform: (T) -> R): UiState<R>

// Handle UiState with callbacks
fun <T> UiState<T>.onState(
    onIdle: () -> Unit,
    onLoading: () -> Unit,
    onSuccess: (T) -> Unit,
    onError: (String) -> Unit
)
```

### 6. ResourceProvider
**Location**: `presentation/common/ResourceProvider.kt`

Singleton for accessing Android resources in ViewModels:

```kotlin
@Singleton
class ResourceProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun getString(@StringRes resId: Int): String
    fun getString(@StringRes resId: Int, vararg formatArgs: Any): String
    fun getColor(@ColorRes resId: Int): Int
}
```

**Usage in ViewModel:**
```kotlin
class ExpenseViewModel @Inject constructor(
    private val resourceProvider: ResourceProvider
) : BaseViewModel() {
    
    fun validateAmount(amount: String) {
        if (amount.isEmpty()) {
            showError(resourceProvider.getString(R.string.error_amount_required))
        }
    }
}
```

### 7. LoadingDialog
**Location**: `presentation/common/LoadingDialog.kt`

Reusable loading dialog for operations:

```kotlin
private val loadingDialog by lazy { LoadingDialog(requireContext()) }

override fun onLoadingStateChanged(isLoading: Boolean) {
    if (isLoading) {
        loadingDialog.show("Saving expense...")
    } else {
        loadingDialog.dismiss()
    }
}
```

## State Flow Patterns

### Pattern 1: Simple Data Loading
```kotlin
// ViewModel
private val _user = MutableStateFlow<UiState<User>>(UiState.Idle)
val user: StateFlow<UiState<User>> = _user.asStateFlow()

fun loadUser() {
    executeWithState(_user) {
        authRepository.getCurrentUser()
    }
}

// Fragment
viewModel.user.collectInLifecycle(viewLifecycleOwner) { state ->
    when (state) {
        is UiState.Idle -> {}
        is UiState.Loading -> showLoading()
        is UiState.Success -> displayUser(state.data)
        is UiState.Error -> showError(state.message)
    }
}
```

### Pattern 2: Action with Feedback
```kotlin
// ViewModel
fun saveExpense(expense: Expense) {
    launchWithLoading(
        onError = { error ->
            showError("Failed to save: ${error.message}")
        }
    ) {
        expenseRepository.createExpense(expense)
            .onSuccess {
                showToast("Expense saved successfully")
                navigateBack()
            }
    }
}
```

### Pattern 3: Confirmation Dialog
```kotlin
// ViewModel
fun requestDeleteExpense(expense: Expense) {
    sendEvent(
        UiEvent.ShowConfirmation(
            title = "Delete Expense",
            message = "Are you sure you want to delete ${expense.description}?",
            onConfirm = { deleteExpense(expense.id) }
        )
    )
}

private fun deleteExpense(expenseId: String) {
    launchWithLoading {
        expenseRepository.deleteExpense(expenseId)
            .onSuccess {
                showToast("Expense deleted")
                loadExpenses()
            }
    }
}
```

### Pattern 4: Real-time Updates with Flow
```kotlin
// ViewModel
val expenses: StateFlow<List<Expense>> = expenseRepository
    .getExpensesFlow()
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

// Fragment - automatically updates when data changes
viewModel.expenses.collectInLifecycle(viewLifecycleOwner) { expenses ->
    adapter.submitList(expenses)
}
```

## Error Handling

### Automatic Error Handling
BaseViewModel provides automatic error handling:

```kotlin
protected open fun handleError(throwable: Throwable) {
    val message = when (throwable) {
        is UnknownHostException -> "No internet connection"
        is SocketTimeoutException -> "Connection timeout"
        is SSLException -> "Secure connection failed"
        else -> throwable.message ?: "An error occurred"
    }
    showSnackbar(message)
}
```

### Custom Error Handling
```kotlin
fun saveExpense(expense: Expense) {
    launchWithLoading(
        onError = { error ->
            when (error) {
                is ValidationException -> showError(error.message!!)
                is NetworkException -> showError("Network error")
                else -> showError("Failed to save expense")
            }
        }
    ) {
        expenseRepository.createExpense(expense)
    }
}
```

## Testing

### ViewModel Testing
```kotlin
@Test
fun `loadExpenses should emit loading then success`() = runTest {
    // Given
    val expenses = listOf(mockExpense1, mockExpense2)
    coEvery { repository.getExpenses() } returns Result.success(expenses)
    
    // When
    viewModel.loadExpenses()
    
    // Then
    viewModel.expenses.test {
        assertEquals(UiState.Loading, awaitItem())
        assertEquals(UiState.Success(expenses), awaitItem())
    }
}
```

### Fragment Testing
```kotlin
@Test
fun `should display expenses when loaded`() {
    // Given
    val expenses = listOf(mockExpense1, mockExpense2)
    viewModel.setExpenses(UiState.Success(expenses))
    
    // When
    launchFragment()
    
    // Then
    onView(withId(R.id.recyclerView))
        .check(matches(hasChildCount(2)))
}
```

## Best Practices

### 1. State Management
- ✅ Use `StateFlow` for data that UI observes
- ✅ Use `SharedFlow` for one-time events
- ✅ Always use `asStateFlow()` / `asSharedFlow()` to expose immutable flows
- ✅ Initialize StateFlows with sensible defaults

### 2. Loading States
- ✅ Show loading indicators for operations > 200ms
- ✅ Disable actions during loading to prevent duplicate requests
- ✅ Use skeleton screens for initial loads
- ✅ Use progress indicators for operations

### 3. Error Handling
- ✅ Always handle errors gracefully
- ✅ Provide actionable error messages
- ✅ Offer retry options when appropriate
- ✅ Log errors for debugging

### 4. Event Handling
- ✅ Use events for one-time actions (navigation, toasts)
- ✅ Use state for data that persists across config changes
- ✅ Never store UI events in StateFlow
- ✅ Consume events immediately in Fragment

### 5. ViewBinding
- ✅ Always nullify binding in `onDestroyView()`
- ✅ Use `binding` property only after `onViewCreated()`
- ✅ Never hold references to Views outside Fragment lifecycle

### 6. Lifecycle Awareness
- ✅ Collect Flows with `collectInLifecycle()`
- ✅ Use `viewLifecycleOwner` in Fragments
- ✅ Cancel coroutines automatically with `viewModelScope`
- ✅ Respect lifecycle states (STARTED vs RESUMED)

## Summary

The UI state management system provides:
- ✅ Type-safe state representation with UiState<T>
- ✅ Consistent error handling across the app
- ✅ Lifecycle-aware data observation
- ✅ Automatic loading state management
- ✅ One-time event handling with UiEvent
- ✅ ViewBinding integration
- ✅ Resource access in ViewModels
- ✅ Reusable base classes
- ✅ Extension functions for common operations
- ✅ Testable architecture

This foundation ensures all screens follow consistent patterns for state management, error handling, and user feedback.
