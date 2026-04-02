package co.ke.snilloc.uberclone.ui.viewmodel

import co.ke.snilloc.uberclone.data.model.Ride
import co.ke.snilloc.uberclone.data.repository.RideRepository
import co.ke.snilloc.uberclone.data.repository.RideRepositoryImpl
import co.ke.snilloc.uberclone.data.repository.UserRepository
import co.ke.snilloc.uberclone.data.repository.UserRepositoryImpl
import co.ke.snilloc.uberclone.ui.base.BaseViewModel
import co.ke.snilloc.uberclone.ui.base.LoadingStateManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class RideHistoryViewModel(
    private val rideRepository: RideRepository = RideRepositoryImpl(),
    private val userRepository: UserRepository = UserRepositoryImpl()
) : BaseViewModel() {

    private val _rides = MutableStateFlow<List<Ride>>(emptyList())
    val rides: StateFlow<List<Ride>> = _rides.asStateFlow()

    fun loadRideHistory() {
        executeAsync(
            loadingType = LoadingStateManager.LoadingType.RIDE_HISTORY,
            loadingMessage = "Loading ride history...",
            maxRetries = 2
        ) {
            userRepository.getCurrentUser().fold(
                onSuccess = { currentUser ->
                    if (currentUser != null) {
                        rideRepository.getUserRides(currentUser.id).fold(
                            onSuccess = { ridesList ->
                                _rides.value = ridesList
                            },
                            onFailure = { exception ->
                                throw exception
                            }
                        )
                    } else {
                        throw Exception("User not authenticated")
                    }
                },
                onFailure = { exception ->
                    throw exception
                }
            )
        }
    }

    fun refreshRideHistory() {
        loadRideHistory()
    }
}