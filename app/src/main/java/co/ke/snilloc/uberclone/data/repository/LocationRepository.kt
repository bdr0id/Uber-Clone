package co.ke.snilloc.uberclone.data.repository

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.core.content.ContextCompat
import co.ke.snilloc.uberclone.data.location.LocationManager
import co.ke.snilloc.uberclone.data.model.Location
import co.ke.snilloc.uberclone.ui.base.ErrorHandler
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import java.util.*

interface LocationRepository {
    suspend fun getCurrentLocation(): Result<Location>
    suspend fun searchPlaces(query: String): Result<List<Location>>
    suspend fun searchPlacesWithBounds(query: String, bounds: RectangularBounds?): Result<List<Location>>
    suspend fun getPlaceDetails(placeId: String): Result<Location>
    suspend fun reverseGeocode(latitude: Double, longitude: Double): Result<String>
    fun startLocationTracking(): Flow<Location>
    fun stopLocationTracking()
    fun hasLocationPermission(): Boolean
    fun isLocationEnabled(): Boolean
}

class LocationRepositoryImpl(
    private val context: Context,
    private val placesClient: PlacesClient,
    private val locationManager: LocationManager,
    private val geocoder: Geocoder = Geocoder(context, Locale.getDefault())
) : LocationRepository {

    override suspend fun getCurrentLocation(): Result<Location> {
        return try {
            // Check network connectivity first
            if (!ErrorHandler.isNetworkAvailable(context)) {
                return Result.failure(Exception("No internet connection. Please check your network and try again."))
            }

            val locationResult = locationManager.getCurrentLocation()
            if (locationResult.isSuccess) {
                val location = locationResult.getOrThrow()
                val address = reverseGeocode(location.latitude, location.longitude)
                    .getOrElse { "Current Location" }
                
                Result.success(
                    Location(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        address = address
                    )
                )
            } else {
                locationResult
            }
        } catch (e: SecurityException) {
            Result.failure(SecurityException("Location permission is required to get your current location"))
        } catch (e: Exception) {
            Result.failure(Exception("Failed to get current location: ${e.message ?: "Unknown error"}"))
        }
    }

    override suspend fun searchPlaces(query: String): Result<List<Location>> {
        return searchPlacesWithBounds(query, null)
    }

    override suspend fun searchPlacesWithBounds(query: String, bounds: RectangularBounds?): Result<List<Location>> {
        return try {
            // Check network connectivity first
            if (!ErrorHandler.isNetworkAvailable(context)) {
                return Result.failure(Exception("No internet connection. Please check your network to search places."))
            }

            if (query.isBlank()) {
                return Result.success(emptyList())
            }

            val token = AutocompleteSessionToken.newInstance()
            val requestBuilder = FindAutocompletePredictionsRequest.builder()
                .setSessionToken(token)
                .setQuery(query)
            
            // Add location bias if bounds are provided
            bounds?.let { requestBuilder.setLocationBias(it) }
            
            val request = requestBuilder.build()
            val response = placesClient.findAutocompletePredictions(request).await()
            
            val locations = response.autocompletePredictions.map { prediction ->
                Location(
                    latitude = 0.0, // Will be filled when place details are fetched
                    longitude = 0.0,
                    address = prediction.getFullText(null).toString(),
                    placeId = prediction.placeId
                )
            }
            Result.success(locations)
        } catch (e: Exception) {
            when {
                e.message?.contains("OVER_QUERY_LIMIT", ignoreCase = true) == true ->
                    Result.failure(Exception("Search limit exceeded. Please try again later."))
                e.message?.contains("REQUEST_DENIED", ignoreCase = true) == true ->
                    Result.failure(Exception("Search service unavailable. Please try again."))
                e.message?.contains("INVALID_REQUEST", ignoreCase = true) == true ->
                    Result.failure(Exception("Invalid search query. Please try different keywords."))
                !ErrorHandler.isNetworkAvailable(context) ->
                    Result.failure(Exception("No internet connection. Please check your network."))
                else ->
                    Result.failure(Exception("Failed to search places: ${e.message ?: "Unknown error"}"))
            }
        }
    }

    override suspend fun getPlaceDetails(placeId: String): Result<Location> {
        return try {
            // Check network connectivity first
            if (!ErrorHandler.isNetworkAvailable(context)) {
                return Result.failure(Exception("No internet connection. Please check your network."))
            }

            val placeFields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS)
            val request = FetchPlaceRequest.newInstance(placeId, placeFields)
            
            val response = placesClient.fetchPlace(request).await()
            val place = response.place
            
            Result.success(
                Location(
                    latitude = place.latLng?.latitude ?: 0.0,
                    longitude = place.latLng?.longitude ?: 0.0,
                    address = place.address ?: place.name ?: "Unknown location",
                    placeId = place.id
                )
            )
        } catch (e: Exception) {
            when {
                e.message?.contains("NOT_FOUND", ignoreCase = true) == true ->
                    Result.failure(Exception("Location not found. Please try a different search."))
                e.message?.contains("OVER_QUERY_LIMIT", ignoreCase = true) == true ->
                    Result.failure(Exception("Service limit exceeded. Please try again later."))
                !ErrorHandler.isNetworkAvailable(context) ->
                    Result.failure(Exception("No internet connection. Please check your network."))
                else ->
                    Result.failure(Exception("Failed to get location details: ${e.message ?: "Unknown error"}"))
            }
        }
    }

    override suspend fun reverseGeocode(latitude: Double, longitude: Double): Result<String> {
        return try {
            if (!Geocoder.isPresent()) {
                return Result.failure(Exception("Geocoding service not available"))
            }

            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            val address = addresses?.firstOrNull()?.getAddressLine(0) ?: "Unknown location"
            Result.success(address)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to get address: ${e.message ?: "Unknown error"}"))
        }
    }

    override fun startLocationTracking(): Flow<Location> {
        return locationManager.startLocationUpdates()
    }

    override fun stopLocationTracking() {
        locationManager.stopLocationUpdates()
    }

    override fun hasLocationPermission(): Boolean {
        return locationManager.hasLocationPermission()
    }

    override fun isLocationEnabled(): Boolean {
        return locationManager.isLocationEnabled()
    }
}