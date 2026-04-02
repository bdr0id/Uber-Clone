package co.ke.snilloc.uberclone.data.model

data class Driver(
    val id: String = "",
    val name: String = "",
    val rating: Double = 0.0,
    val vehicleInfo: String = "",
    val currentLocation: Location = Location(),
    val phoneNumber: String = "",
    val profileImageUrl: String? = null
)