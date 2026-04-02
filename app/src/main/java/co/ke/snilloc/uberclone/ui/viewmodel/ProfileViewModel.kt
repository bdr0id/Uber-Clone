package co.ke.snilloc.uberclone.ui.viewmodel

import co.ke.snilloc.uberclone.data.model.Ride
import co.ke.snilloc.uberclone.data.model.RideStatus
import co.ke.snilloc.uberclone.data.model.User
import co.ke.snilloc.uberclone.data.repository.RideRepository
import co.ke.snilloc.uberclone.data.repository.RideRepositoryImpl
import co.ke.snilloc.uberclone.data.repository.UserRepository
import co.ke.snilloc.uberclone.data.repository.UserRepositoryImpl
import co.ke.snilloc.uberclone.ui.base.BaseViewModel
import co.ke.snilloc.uberclone.ui.base.LoadingStateManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ProfileViewModel(
    private val userRepository: UserRepository = UserRepositoryImpl(),
    private val rideRepository: RideRepository = RideRepositoryImpl()
) : BaseViewModel() {

    data class UserStats(
        val totalTrips: Int,
        val averageRating: Double,
        val totalSpent: Double
    )

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _userStats = MutableStateFlow<UserStats?>(null)
    val userStats: StateFlow<UserStats?> = _userStats.asStateFlow()

    private val _recentRides = MutableStateFlow<List<Ride>>(emptyList())
    val recentRides: StateFlow<List<Ride>> = _recentRides.asStateFlow()

    private val _rideHistory = MutableStateFlow<List<Ride>>(emptyList())
    val rideHistory: StateFlow<List<Ride>> = _rideHistory.asStateFlow()

    private val _profileUpdated = MutableStateFlow(false)
    val profileUpdated: StateFlow<Boolean> = _profileUpdated.asStateFlow()

    private val _isEditMode = MutableStateFlow(false)
    val isEditMode: StateFlow<Boolean> = _isEditMode.asStateFlow()

    fun loadUserProfile() {
        executeAsync(
            loadingType = LoadingStateManager.LoadingType.GENERAL,
            loadingMessage = "Loading profile...",
            maxRetries = 2
        ) {
            userRepository.getCurrentUser().fold(
                onSuccess = { user ->
                    _user.value = user
                    user?.let { loadRideHistory(it.id) }
                },
                onFailure = { throw it }
            )
        }
    }

    fun loadRecentRides() {
        val currentUser = _user.value
        if (currentUser != null) {
            executeAsync(
                loadingType = LoadingStateManager.LoadingType.RIDE_HISTORY,
                loadingMessage = "Loading recent rides...",
                maxRetries = 1
            ) {
                loadRideHistory(currentUser.id)
            }
        }
    }

    private suspend fun loadRideHistory(userId: String) {
        rideRepository.getUserRides(userId).fold(
            onSuccess = { rides ->
                _rideHistory.value = rides
                _recentRides.value = rides.take(3) // Show only 3 recent rides
                calculateUserStats(rides)
            },
            onFailure = {
                throw it
            }
        )
    }

    private fun calculateUserStats(rides: List<Ride>) {
        val completedRides = rides.filter { it.status == RideStatus.COMPLETED }
        val totalTrips = completedRides.size
        val totalSpent = completedRides.sumOf { it.actualPrice ?: it.estimatedPrice }
        val ratedRides = completedRides.filter { it.rating != null && it.rating > 0 }
        val averageRating = if (ratedRides.isNotEmpty()) {
            ratedRides.sumOf { it.rating!! } / ratedRides.size.toDouble()
        } else {
            0.0
        }

        _userStats.value = UserStats(
            totalTrips = totalTrips,
            averageRating = averageRating,
            totalSpent = totalSpent
        )
    }

    fun updateUserProfile(name: String, email: String, phoneNumber: String?) {
        val currentUser = _user.value
        if (currentUser == null) {
            setError("No user data available")
            return
        }

        val updatedUser = currentUser.copy(
            name = name,
            email = email,
            phoneNumber = phoneNumber
        )

        executeAsync(
            loadingType = LoadingStateManager.LoadingType.PROFILE_UPDATE,
            loadingMessage = "Updating profile...",
            maxRetries = 2
        ) {
            userRepository.updateUser(updatedUser).fold(
                onSuccess = {
                    _user.value = updatedUser
                    _profileUpdated.value = true
                    // Reset the flag after a short delay
                    kotlinx.coroutines.delay(100)
                    _profileUpdated.value = false
                },
                onFailure = { throw it }
            )
        }
    }

    fun updateProfile(updatedUser: User) {
        executeAsync(
            loadingType = LoadingStateManager.LoadingType.PROFILE_UPDATE,
            loadingMessage = "Updating profile...",
            maxRetries = 2
        ) {
            userRepository.updateUser(updatedUser).fold(
                onSuccess = {
                    _user.value = updatedUser
                    _isEditMode.value = false
                },
                onFailure = { throw it }
            )
        }
    }

    fun enableEditMode() {
        _isEditMode.value = true
    }

    fun cancelEdit() {
        _isEditMode.value = false
    }

    fun rateRide(rideId: String, rating: Int, feedback: String?) {
        executeAsync(
            loadingType = LoadingStateManager.LoadingType.GENERAL,
            loadingMessage = "Submitting rating...",
            maxRetries = 2
        ) {
            rideRepository.rateRide(rideId, rating, feedback).fold(
                onSuccess = {
                    // Update the ride in the history list
                    val updatedHistory = _rideHistory.value.map { ride ->
                        if (ride.id == rideId) {
                            ride.copy(rating = rating, feedback = feedback)
                        } else {
                            ride
                        }
                    }
                    _rideHistory.value = updatedHistory
                    _recentRides.value = updatedHistory.take(3)
                    calculateUserStats(updatedHistory)
                },
                onFailure = { throw it }
            )
        }
    }

    fun refreshRideHistory() {
        val currentUser = _user.value
        if (currentUser != null) {
            executeAsync(
                loadingType = LoadingStateManager.LoadingType.RIDE_HISTORY,
                loadingMessage = "Refreshing ride history...",
                maxRetries = 1
            ) {
                loadRideHistory(currentUser.id)
            }
        }
    }

    fun getTotalSpent(): Double {
        return _rideHistory.value.sumOf { ride ->
            ride.actualPrice ?: ride.estimatedPrice
        }
    }

    fun getCompletedTripsCount(): Int {
        return _rideHistory.value.count { ride ->
            ride.status == RideStatus.COMPLETED
        }
    }

    fun getAverageRating(): Double {
        val ratedRides = _rideHistory.value.filter { it.rating != null }
        return if (ratedRides.isNotEmpty()) {
            ratedRides.sumOf { it.rating!! } / ratedRides.size.toDouble()
        } else {
            0.0
        }
    }
}