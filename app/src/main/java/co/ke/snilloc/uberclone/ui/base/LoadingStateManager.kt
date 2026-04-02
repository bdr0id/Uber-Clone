package co.ke.snilloc.uberclone.ui.base

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages different types of loading states for the application
 */
class LoadingStateManager {
    
    data class LoadingState(
        val isLoading: Boolean = false,
        val loadingType: LoadingType = LoadingType.GENERAL,
        val message: String? = null
    )
    
    enum class LoadingType {
        GENERAL,
        LOCATION,
        NETWORK,
        SEARCH,
        RIDE_REQUEST,
        DRIVER_SEARCH,
        PROFILE_UPDATE,
        RIDE_HISTORY,
        PAYMENT
    }
    
    private val _loadingState = MutableStateFlow(LoadingState())
    val loadingState: StateFlow<LoadingState> = _loadingState.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    fun setLoading(
        isLoading: Boolean, 
        type: LoadingType = LoadingType.GENERAL, 
        message: String? = null
    ) {
        _loadingState.value = LoadingState(isLoading, type, message)
        _isLoading.value = isLoading
    }
    
    fun setLocationLoading(isLoading: Boolean, message: String? = null) {
        setLoading(isLoading, LoadingType.LOCATION, message ?: "Getting your location...")
    }
    
    fun setNetworkLoading(isLoading: Boolean, message: String? = null) {
        setLoading(isLoading, LoadingType.NETWORK, message ?: "Connecting...")
    }
    
    fun setSearchLoading(isLoading: Boolean, message: String? = null) {
        setLoading(isLoading, LoadingType.SEARCH, message ?: "Searching...")
    }
    
    fun setRideRequestLoading(isLoading: Boolean, message: String? = null) {
        setLoading(isLoading, LoadingType.RIDE_REQUEST, message ?: "Requesting ride...")
    }
    
    fun setDriverSearchLoading(isLoading: Boolean, message: String? = null) {
        setLoading(isLoading, LoadingType.DRIVER_SEARCH, message ?: "Finding drivers...")
    }
    
    fun setProfileUpdateLoading(isLoading: Boolean, message: String? = null) {
        setLoading(isLoading, LoadingType.PROFILE_UPDATE, message ?: "Updating profile...")
    }
    
    fun setRideHistoryLoading(isLoading: Boolean, message: String? = null) {
        setLoading(isLoading, LoadingType.RIDE_HISTORY, message ?: "Loading ride history...")
    }
    
    fun clearLoading() {
        _loadingState.value = LoadingState()
        _isLoading.value = false
    }
}