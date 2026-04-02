package co.ke.snilloc.uberclone.data.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager as AndroidLocationManager
import android.os.Looper
import androidx.core.content.ContextCompat
import co.ke.snilloc.uberclone.data.model.Location
import co.ke.snilloc.uberclone.ui.base.ErrorHandler
import com.google.android.gms.location.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

interface LocationManager {
    suspend fun getCurrentLocation(): Result<Location>
    fun startLocationUpdates(): Flow<Location>
    fun stopLocationUpdates()
    fun hasLocationPermission(): Boolean
    fun isLocationEnabled(): Boolean
    suspend fun requestLocationPermission(): Boolean
}

class LocationManagerImpl(
    private val context: Context,
    private val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
) : LocationManager {

    private var locationCallback: LocationCallback? = null
    private val locationRequest = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY,
        10000L // 10 seconds
    ).apply {
        setMinUpdateIntervalMillis(5000L) // 5 seconds
        setMaxUpdateDelayMillis(15000L) // 15 seconds
    }.build()

    override suspend fun getCurrentLocation(): Result<Location> {
        return try {
            // Check network connectivity first
            if (!ErrorHandler.isNetworkAvailable(context)) {
                return Result.failure(Exception("No internet connection available"))
            }

            if (!hasLocationPermission()) {
                return Result.failure(SecurityException("Location permission not granted"))
            }

            if (!isLocationEnabled()) {
                return Result.failure(Exception("Location services are disabled. Please enable GPS in settings."))
            }

            val location = fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                null
            ).await()

            if (location != null) {
                Result.success(
                    Location(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        address = "Current Location"
                    )
                )
            } else {
                Result.failure(Exception("Unable to determine your current location. Please try again."))
            }
        } catch (e: SecurityException) {
            Result.failure(SecurityException("Location permission is required to get your current location"))
        } catch (e: Exception) {
            Result.failure(Exception("Failed to get location: ${e.message ?: "Unknown error"}"))
        }
    }

    override fun startLocationUpdates(): Flow<Location> = callbackFlow {
        if (!hasLocationPermission()) {
            close(SecurityException("Location permission not granted"))
            return@callbackFlow
        }

        if (!isLocationEnabled()) {
            close(Exception("Location services are disabled. Please enable GPS in settings."))
            return@callbackFlow
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    val locationData = Location(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        address = "Current Location"
                    )
                    trySend(locationData)
                }
            }

            override fun onLocationAvailability(availability: LocationAvailability) {
                if (!availability.isLocationAvailable) {
                    close(Exception("Location is temporarily unavailable. Please check your GPS settings."))
                }
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback!!,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            close(SecurityException("Location permission is required for location tracking"))
        } catch (e: Exception) {
            close(Exception("Failed to start location tracking: ${e.message}"))
        }

        awaitClose {
            stopLocationUpdates()
        }
    }

    override fun stopLocationUpdates() {
        locationCallback?.let { callback ->
            try {
                fusedLocationClient.removeLocationUpdates(callback)
            } catch (e: Exception) {
                // Log error but don't throw - stopping updates should be safe
            } finally {
                locationCallback = null
            }
        }
    }

    override fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun isLocationEnabled(): Boolean {
        return try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as AndroidLocationManager
            locationManager.isProviderEnabled(AndroidLocationManager.GPS_PROVIDER) ||
                   locationManager.isProviderEnabled(AndroidLocationManager.NETWORK_PROVIDER)
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun requestLocationPermission(): Boolean {
        // This method would typically be implemented in the Activity/Fragment
        // where permission requests are handled. For now, we just check current status.
        return hasLocationPermission()
    }
}