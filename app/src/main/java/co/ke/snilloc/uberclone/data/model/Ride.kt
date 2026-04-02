package co.ke.snilloc.uberclone.data.model

data class Ride(
    val id: String = "",
    val userId: String = "",
    val pickupLocation: Location = Location(),
    val destinationLocation: Location = Location(),
    val rideType: RideType = RideType.UBER_X,
    val status: RideStatus = RideStatus.REQUESTED,
    val estimatedPrice: Double = 0.0,
    val actualPrice: Double? = null,
    val driverId: String? = null,
    val requestTime: Long = 0L,
    val startTime: Long? = null,
    val endTime: Long? = null,
    val rating: Int? = null,
    val feedback: String? = null
)