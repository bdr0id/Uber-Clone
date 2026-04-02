package co.ke.snilloc.uberclone.ui.viewmodel

import android.app.Application
import androidx.lifecycle.viewModelScope
import co.ke.snilloc.uberclone.data.location.LocationManager
import co.ke.snilloc.uberclone.data.location.LocationManagerImpl
import co.ke.snilloc.uberclone.data.location.PlacesInitializer
import co.ke.snilloc.uberclone.data.model.Driver
import co.ke.snilloc.uberclone.data.model.Location
import co.ke.snilloc.uberclone.data.model.Ride
import co.ke.snilloc.uberclone.data.model.RideStatus
import co.ke.snilloc.uberclone.data.model.RideType
import co.ke.snilloc.uberclone.data.repository.LocationRepository
import co.ke.snilloc.uberclone.data.repository.LocationRepositoryImpl
import co.ke.snilloc.uberclone.data.repository.RideRepository
import co.ke.snilloc.uberclone.data.repository.RideRepositoryImpl
import co.ke.snilloc.uberclone.data.repository.UserRepository
import co.ke.snilloc.uberclone.data.repository.UserRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import co.ke.snilloc.uberclone.ui.base.BaseViewModel
import co.ke.snilloc.uberclone.ui.base.LoadingStateManager
import kotlinx.coroutines.launch

class MapViewModel(
    application: Application,
    private val locationManager: LocationManager = LocationManagerImpl(application),
    private val locationRepository: LocationRepository = LocationRepositoryImpl(
        context = application,
        placesClient = PlacesInitializer.getPlacesClient() ?: throw IllegalStateException("Places API not initialized"),
        locationManager = locationManager
    ),
    private val rideRepository: RideRepository = RideRepositoryImpl(),
    private val userRepository: UserRepository = UserRepositoryImpl()
) : BaseViewModel() {

    private val context = application.applicationContext

    private val _currentLocation = MutableStateFlow<Location?>(null)
    val currentLocation: StateFlow<Location?> = _currentLocation.asStateFlow()

    private val _pickupLocation = MutableStateFlow<Location?>(null)
    val pickupLocation: StateFlow<Location?> = _pickupLocation.asStateFlow()

    private val _destinationLocation = MutableStateFlow<Location?>(null)
    val destinationLocation: StateFlow<Location?> = _destinationLocation.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Location>>(emptyList())
    val searchResults: StateFlow<List<Location>> = _searchResults.asStateFlow()

    private val _selectedRideType = MutableStateFlow(RideType.UBER_X)
    val selectedRideType: StateFlow<RideType> = _selectedRideType.asStateFlow()

    private val _estimatedPrice = MutableStateFlow<Double?>(null)
    val estimatedPrice: StateFlow<Double?> = _estimatedPrice.asStateFlow()

    private val _currentRide = MutableStateFlow<Ride?>(null)
    val currentRide: StateFlow<Ride?> = _currentRide.asStateFlow()

    private val _assignedDriver = MutableStateFlow<Driver?>(null)
    val assignedDriver: StateFlow<Driver?> = _assignedDriver.asStateFlow()

    private val _driverLocation = MutableStateFlow<Location?>(null)
    val driverLocation: StateFlow<Location?> = _driverLocation.asStateFlow()

    private val _rideTypes = MutableStateFlow(
        listOf(
            RideType.UBER_X,
            RideType.UBER_XL,
            RideType.UBER_COMFORT
        )
    )
    val rideTypes: StateFlow<List<RideType>> = _rideTypes.asStateFlow()

    private val _isLocationTrackingActive = MutableStateFlow(false)
    val isLocationTrackingActive: StateFlow<Boolean> = _isLocationTrackingActive.asStateFlow()

    private val _isDriverTrackingActive = MutableStateFlow(false)
    val isDriverTrackingActive: StateFlow<Boolean> = _isDriverTrackingActive.asStateFlow()

    fun getCurrentLocation() {
        executeAsync(
            loadingType = LoadingStateManager.LoadingType.LOCATION,
            loadingMessage = "Getting your location...",
            maxRetries = 2,
            retryDelayMs = 1000L
        ) {
            locationRepository.getCurrentLocation().fold(
                onSuccess = { location ->
                    _currentLocation.value = location
                    if (_pickupLocation.value == null) {
                        _pickupLocation.value = location
                    }
                },
                onFailure = { throw it }
            )
        }
    }

    fun searchPlaces(query: String) {
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }

        executeAsync(
            loadingType = LoadingStateManager.LoadingType.SEARCH,
            loadingMessage = "Searching places...",
            maxRetries = 1
        ) {
            locationRepository.searchPlaces(query).fold(
                onSuccess = { locations ->
                    _searchResults.value = locations
                },
                onFailure = { throw it }
            )
        }
    }

    fun selectPickupLocation(location: Location) {
        executeAsync(
            loadingType = LoadingStateManager.LoadingType.GENERAL,
            maxRetries = 1
        ) {
            if (location.placeId != null) {
                locationRepository.getPlaceDetails(location.placeId).fold(
                    onSuccess = { detailedLocation ->
                        _pickupLocation.value = detailedLocation
                        calculateEstimatedPrice()
                    },
                    onFailure = {
                        _pickupLocation.value = location
                        calculateEstimatedPrice()
                    }
                )
            } else {
                _pickupLocation.value = location
                calculateEstimatedPrice()
            }
        }
    }

    fun selectDestinationLocation(location: Location) {
        executeAsync(
            loadingType = LoadingStateManager.LoadingType.GENERAL,
            maxRetries = 1
        ) {
            if (location.placeId != null) {
                locationRepository.getPlaceDetails(location.placeId).fold(
                    onSuccess = { detailedLocation ->
                        _destinationLocation.value = detailedLocation
                        calculateEstimatedPrice()
                    },
                    onFailure = {
                        _destinationLocation.value = location
                        calculateEstimatedPrice()
                    }
                )
            } else {
                _destinationLocation.value = location
                calculateEstimatedPrice()
            }
        }
    }

    fun selectRideType(rideType: RideType) {
        _selectedRideType.value = rideType
        calculateEstimatedPrice()
    }

    private fun calculateEstimatedPrice() {
        val pickup = _pickupLocation.value
        val destination = _destinationLocation.value
        
        if (pickup != null && destination != null) {
            executeAsync(
                loadingType = LoadingStateManager.LoadingType.GENERAL,
                maxRetries = 1
            ) {
                rideRepository.calculateEstimatedPrice(pickup, destination, _selectedRideType.value).fold(
                    onSuccess = { price ->
                        _estimatedPrice.value = price
                    },
                    onFailure = { 
                        throw it
                    }
                )
            }
        } else {
            _estimatedPrice.value = null
        }
    }

    fun requestRide() {
        val pickup = _pickupLocation.value
        val destination = _destinationLocation.value
        val price = _estimatedPrice.value

        if (pickup == null || destination == null || price == null) {
            setError("Please select pickup and destination locations")
            return
        }

        executeAsync(
            loadingType = LoadingStateManager.LoadingType.RIDE_REQUEST,
            loadingMessage = "Requesting ride...",
            maxRetries = 2
        ) {
            userRepository.getCurrentUser().fold(
                onSuccess = { user ->
                    if (user != null) {
                        val ride = Ride(
                            userId = user.id,
                            pickupLocation = pickup,
                            destinationLocation = destination,
                            rideType = _selectedRideType.value,
                            status = RideStatus.REQUESTED,
                            estimatedPrice = price,
                            requestTime = System.currentTimeMillis()
                        )

                        rideRepository.createRide(ride).fold(
                            onSuccess = { rideId ->
                                _currentRide.value = ride.copy(id = rideId)
                                findDriver(rideId)
                            },
                            onFailure = { throw it }
                        )
                    } else {
                        throw Exception("User not authenticated")
                    }
                },
                onFailure = { throw it }
            )
        }
    }

    private suspend fun findDriver(rideId: String) {
        // Simulate driver search delay
        kotlinx.coroutines.delay(3000)
        
        rideRepository.assignDriver(rideId).fold(
            onSuccess = { driver ->
                _assignedDriver.value = driver
                _driverLocation.value = driver.currentLocation
                _currentRide.value = _currentRide.value?.copy(
                    status = RideStatus.DRIVER_ASSIGNED,
                    driverId = driver.id
                )
                
                // Start tracking driver location
                startDriverTracking(driver.id)
                
                // Start driver simulation
                val pickup = _pickupLocation.value
                val destination = _destinationLocation.value
                if (pickup != null && destination != null) {
                    simulateDriverJourney(rideId, pickup, destination)
                }
            },
            onFailure = {
                setError("No drivers available at the moment")
            }
        )
    }

    fun cancelRide() {
        val ride = _currentRide.value
        if (ride != null) {
            executeAsync(
                loadingType = LoadingStateManager.LoadingType.GENERAL,
                loadingMessage = "Cancelling ride...",
                maxRetries = 1
            ) {
                rideRepository.updateRideStatus(ride.id, RideStatus.CANCELLED).fold(
                    onSuccess = {
                        _currentRide.value = null
                        _assignedDriver.value = null
                    },
                    onFailure = {
                        throw it
                    }
                )
            }
        }
    }

    fun clearSearchResults() {
        _searchResults.value = emptyList()
    }

    fun hasLocationPermission(): Boolean {
        return locationRepository.hasLocationPermission()
    }

    fun isLocationEnabled(): Boolean {
        return locationRepository.isLocationEnabled()
    }

    fun startLocationTracking() {
        if (_isLocationTrackingActive.value) return
        
        viewModelScope.launch {
            _isLocationTrackingActive.value = true
            locationRepository.startLocationTracking()
                .catch { exception ->
                    setError("Location tracking failed: ${exception.message}")
                    _isLocationTrackingActive.value = false
                }
                .collect { location ->
                    _currentLocation.value = location
                    // Update pickup location if not set
                    if (_pickupLocation.value == null) {
                        _pickupLocation.value = location
                    }
                }
        }
    }

    fun stopLocationTracking() {
        if (!_isLocationTrackingActive.value) return
        
        locationRepository.stopLocationTracking()
        _isLocationTrackingActive.value = false
    }

    fun searchPlacesWithBounds(query: String, currentLocation: Location?) {
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }

        executeAsync(
            loadingType = LoadingStateManager.LoadingType.SEARCH,
            loadingMessage = "Searching places...",
            maxRetries = 1
        ) {
            val bounds = currentLocation?.let { location ->
                // Create a rectangular bounds around current location (approximately 50km radius)
                val latOffset = 0.45 // roughly 50km
                val lngOffset = 0.45
                
                com.google.android.libraries.places.api.model.RectangularBounds.newInstance(
                    com.google.android.gms.maps.model.LatLng(
                        location.latitude - latOffset,
                        location.longitude - lngOffset
                    ),
                    com.google.android.gms.maps.model.LatLng(
                        location.latitude + latOffset,
                        location.longitude + lngOffset
                    )
                )
            }

            locationRepository.searchPlacesWithBounds(query, bounds).fold(
                onSuccess = { locations ->
                    _searchResults.value = locations
                },
                onFailure = { throw it }
            )
        }
    }

    private fun startDriverTracking(driverId: String) {
        if (_isDriverTrackingActive.value) return
        
        viewModelScope.launch {
            _isDriverTrackingActive.value = true
            
            // Poll driver location every 10 seconds
            while (_isDriverTrackingActive.value && _currentRide.value != null) {
                rideRepository.getDriverLocation(driverId).fold(
                    onSuccess = { location ->
                        location?.let { _driverLocation.value = it }
                    },
                    onFailure = {
                        // Continue tracking even if one update fails
                    }
                )
                kotlinx.coroutines.delay(10000) // 10 seconds
            }
        }
    }

    private fun stopDriverTracking() {
        _isDriverTrackingActive.value = false
        _driverLocation.value = null
    }

    private suspend fun simulateDriverJourney(rideId: String, pickup: Location, destination: Location) {
        // Phase 1: Driver moving to pickup (5 minutes)
        kotlinx.coroutines.delay(5000) // Small delay before starting movement
        
        val currentRide = _currentRide.value
        if (currentRide?.status == RideStatus.DRIVER_ASSIGNED) {
            // Update status to driver arriving
            rideRepository.updateRideStatus(rideId, RideStatus.DRIVER_ARRIVING).fold(
                onSuccess = {
                    _currentRide.value = currentRide.copy(status = RideStatus.DRIVER_ARRIVING)
                },
                onFailure = { /* Handle error */ }
            )
            
            // Simulate arrival at pickup after 5 minutes
            kotlinx.coroutines.delay(300000) // 5 minutes
            
            // Driver has arrived at pickup
            _currentRide.value?.let { ride ->
                if (ride.status == RideStatus.DRIVER_ARRIVING) {
                    // Update driver location to pickup
                    ride.driverId?.let { driverId ->
                        rideRepository.updateDriverLocation(driverId, pickup)
                    }
                    
                    // Wait for passenger (simulate 2 minutes)
                    kotlinx.coroutines.delay(120000) // 2 minutes
                    
                    // Start the trip
                    startTrip(rideId, destination)
                }
            }
        }
    }

    private suspend fun startTrip(rideId: String, destination: Location) {
        rideRepository.startTrip(rideId).fold(
            onSuccess = {
                _currentRide.value = _currentRide.value?.copy(
                    status = RideStatus.IN_PROGRESS,
                    startTime = System.currentTimeMillis()
                )
                
                // Simulate trip to destination (10 minutes)
                kotlinx.coroutines.delay(600000) // 10 minutes
                
                // Complete the trip
                completeTrip(rideId, destination)
            },
            onFailure = {
                setError("Failed to start trip: ${it.message}")
            }
        )
    }

    private suspend fun completeTrip(rideId: String, destination: Location) {
        val currentRide = _currentRide.value
        if (currentRide != null) {
            val actualPrice = currentRide.estimatedPrice // In real app, this might be different
            
            rideRepository.completeRide(rideId, actualPrice).fold(
                onSuccess = {
                    _currentRide.value = currentRide.copy(
                        status = RideStatus.COMPLETED,
                        actualPrice = actualPrice,
                        endTime = System.currentTimeMillis()
                    )
                    
                    // Update driver location to destination
                    currentRide.driverId?.let { driverId ->
                        rideRepository.updateDriverLocation(driverId, destination)
                    }
                    
                    // Stop driver tracking
                    stopDriverTracking()
                },
                onFailure = {
                    setError("Failed to complete trip: ${it.message}")
                }
            )
        }
    }

    fun rateRide(rating: Int, feedback: String? = null) {
        val ride = _currentRide.value
        if (ride != null && ride.status == RideStatus.COMPLETED) {
            executeAsync(
                loadingType = LoadingStateManager.LoadingType.GENERAL,
                loadingMessage = "Submitting rating...",
                maxRetries = 2
            ) {
                rideRepository.rateRide(ride.id, rating, feedback).fold(
                    onSuccess = {
                        _currentRide.value = ride.copy(rating = rating, feedback = feedback)
                        // Clear current ride after rating
                        kotlinx.coroutines.delay(2000)
                        _currentRide.value = null
                        _assignedDriver.value = null
                    },
                    onFailure = {
                        throw it
                    }
                )
            }
        }
    }

    fun getCurrentRideStatus(): RideStatus? {
        return _currentRide.value?.status
    }

    fun getEstimatedArrivalTime(): String? {
        val ride = _currentRide.value
        val driver = _assignedDriver.value
        
        return when (ride?.status) {
            RideStatus.DRIVER_ASSIGNED, RideStatus.DRIVER_ARRIVING -> {
                "Driver arriving in 5 minutes"
            }
            RideStatus.IN_PROGRESS -> {
                "Arriving at destination in 10 minutes"
            }
            else -> null
        }
    }

    fun resetRideState() {
        _currentRide.value = null
        _assignedDriver.value = null
        _driverLocation.value = null
        stopDriverTracking()
    }

    override fun onCleared() {
        super.onCleared()
        stopLocationTracking()
        stopDriverTracking()
    }
}