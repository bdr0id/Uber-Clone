package co.ke.snilloc.uberclone.data.model

data class Location(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val address: String = "",
    val placeId: String? = null
)