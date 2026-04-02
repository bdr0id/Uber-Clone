package co.ke.snilloc.uberclone.ui.viewmodel

import co.ke.snilloc.uberclone.data.model.Driver
import co.ke.snilloc.uberclone.data.model.Ride
import co.ke.snilloc.uberclone.data.repository.RideRepository
import co.ke.snilloc.uberclone.data.repository.RideRepositoryImpl
import co.ke.snilloc.uberclone.ui.base.BaseViewModel
import co.ke.snilloc.uberclone.ui.base.LoadingStateManager
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

class TripCompletionViewModel(
    private val rideRepository: RideRepository = RideRepositoryImpl()
) : BaseViewModel() {

    private val _ride = MutableStateFlow<Ride?>(null)
    val ride: StateFlow<Ride?> = _ride.asStateFlow()

    private val _driver = MutableStateFlow<Driver?>(null)
    val driver: StateFlow<Driver?> = _driver.asStateFlow()

    private val _ratingSubmitted = MutableStateFlow(false)
    val ratingSubmitted: StateFlow<Boolean> = _ratingSubmitted.asStateFlow()

    fun loadRideDetails(rideId: String) {
        executeAsync(
            loadingType = LoadingStateManager.LoadingType.GENERAL,
            loadingMessage = "Loading trip details...",
            maxRetries = 2
        ) {
            rideRepository.getRideById(rideId).fold(
                onSuccess = { ride ->
                    _ride.value = ride
                },
                onFailure = { exception ->
                    throw exception
                }
            )
        }
    }

    fun loadDriverInfo(driverId: String?) {
        if (driverId == null) return
        
        executeAsync(
            loadingType = LoadingStateManager.LoadingType.GENERAL,
            loadingMessage = "Loading driver information...",
            maxRetries = 1
        ) {
            val firestore = FirebaseFirestore.getInstance()
            val document = firestore.collection("drivers")
                .document(driverId)
                .get()
                .await()
            
            if (document.exists()) {
                val driver = document.toObject(Driver::class.java)
                _driver.value = driver
            }
        }
    }

    fun submitRating(rating: Int, feedback: String?) {
        val currentRide = _ride.value
        if (currentRide == null) {
            setError("No ride information available")
            return
        }

        executeAsync(
            loadingType = LoadingStateManager.LoadingType.GENERAL,
            loadingMessage = "Submitting rating...",
            maxRetries = 2
        ) {
            rideRepository.rateRide(currentRide.id, rating, feedback).fold(
                onSuccess = {
                    _ratingSubmitted.value = true
                },
                onFailure = { exception ->
                    throw exception
                }
            )
        }
    }

    fun skipRating() {
        _ratingSubmitted.value = true
    }
}