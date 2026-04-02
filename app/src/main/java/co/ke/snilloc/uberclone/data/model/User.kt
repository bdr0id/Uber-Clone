package co.ke.snilloc.uberclone.data.model

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val phoneNumber: String? = null,
    val profileImageUrl: String? = null,
    val rating: Double = 0.0,
    val totalTrips: Int = 0
)