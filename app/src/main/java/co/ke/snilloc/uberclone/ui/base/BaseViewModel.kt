package co.ke.snilloc.uberclone.ui.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

abstract class BaseViewModel : ViewModel() {
    
    protected val loadingStateManager = LoadingStateManager()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _isOffline = MutableStateFlow(false)
    val isOffline: StateFlow<Boolean> = _isOffline.asStateFlow()
    
    private val _retryAction = MutableStateFlow<(() -> Unit)?>(null)
    val retryAction: StateFlow<(() -> Unit)?> = _retryAction.asStateFlow()
    
    // Expose loading states
    val isLoading: StateFlow<Boolean> = loadingStateManager.isLoading
    val loadingState: StateFlow<LoadingStateManager.LoadingState> = loadingStateManager.loadingState
    
    protected fun setLoading(
        loading: Boolean, 
        type: LoadingStateManager.LoadingType = LoadingStateManager.LoadingType.GENERAL,
        message: String? = null
    ) {
        loadingStateManager.setLoading(loading, type, message)
    }
    
    protected fun setError(error: String?, retryAction: (() -> Unit)? = null) {
        _error.value = error
        _retryAction.value = retryAction
    }
    
    protected fun setError(throwable: Throwable, retryAction: (() -> Unit)? = null) {
        val errorMessage = ErrorHandler.getErrorMessage(throwable)
        _error.value = errorMessage
        _retryAction.value = if (ErrorHandler.isRecoverableError(throwable)) retryAction else null
    }
    
    protected fun clearError() {
        _error.value = null
        _retryAction.value = null
    }
    
    protected fun setOfflineState(isOffline: Boolean) {
        _isOffline.value = isOffline
    }
    
    /**
     * Execute async operation with comprehensive error handling and retry mechanism
     */
    protected fun <T> executeAsync(
        loadingType: LoadingStateManager.LoadingType = LoadingStateManager.LoadingType.GENERAL,
        loadingMessage: String? = null,
        maxRetries: Int = 0,
        retryDelayMs: Long = 1000L,
        onStart: () -> Unit = {},
        onComplete: () -> Unit = {},
        onError: (Throwable) -> Unit = { setError(it) },
        action: suspend () -> T
    ) {
        viewModelScope.launch {
            var attempt = 0
            var lastException: Throwable? = null
            
            while (attempt <= maxRetries) {
                try {
                    if (attempt == 0) {
                        onStart()
                        setLoading(true, loadingType, loadingMessage)
                        clearError()
                    }
                    
                    val result = action()
                    
                    // Success - clear any previous errors and loading
                    clearError()
                    loadingStateManager.clearLoading()
                    onComplete()
                    return@launch
                    
                } catch (e: Exception) {
                    lastException = e
                    attempt++
                    
                    if (attempt <= maxRetries && ErrorHandler.isRecoverableError(e)) {
                        // Wait before retry
                        delay(retryDelayMs * attempt) // Exponential backoff
                        continue
                    } else {
                        // Max retries reached or non-recoverable error
                        break
                    }
                }
            }
            
            // Handle final error
            lastException?.let { exception ->
                loadingStateManager.clearLoading()
                
                // Set retry action for recoverable errors
                val retryAction = if (ErrorHandler.isRecoverableError(exception)) {
                    {
                        executeAsync(
                            loadingType = loadingType,
                            loadingMessage = loadingMessage,
                            maxRetries = maxRetries,
                            retryDelayMs = retryDelayMs,
                            onStart = onStart,
                            onComplete = onComplete,
                            onError = onError,
                            action = action
                        )
                    }
                } else null
                
                setError(exception, retryAction)
                onError(exception)
                onComplete()
            }
        }
    }
    
    /**
     * Execute async operation with simple error handling (backward compatibility)
     */
    protected fun <T> executeAsyncSimple(
        onStart: () -> Unit = { setLoading(true) },
        onComplete: () -> Unit = { setLoading(false) },
        onError: (Exception) -> Unit = { setError(it.message) },
        action: suspend () -> T
    ) {
        executeAsync(
            onStart = onStart,
            onComplete = {
                loadingStateManager.clearLoading()
                onComplete()
            },
            onError = { throwable ->
                if (throwable is Exception) {
                    onError(throwable)
                } else {
                    setError(throwable)
                }
            },
            action = action
        )
    }
    
    /**
     * Retry the last failed operation
     */
    fun retry() {
        _retryAction.value?.invoke()
    }
    
    /**
     * Clear all error and loading states
     */
    fun clearStates() {
        clearError()
        loadingStateManager.clearLoading()
        setOfflineState(false)
    }
}