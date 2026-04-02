package co.ke.snilloc.uberclone.data.repository

import co.ke.snilloc.uberclone.data.model.Driver
import co.ke.snilloc.uberclone.data.model.Location
import co.ke.snilloc.uberclone.data.model.Ride
import co.ke.snilloc.uberclone.data.model.RideStatus
import co.ke.snilloc.uberclone.data.model.RideType
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import kotlin.random.Random

interface RideRepository {
    suspend fun createRide(ride: Ride): Result<String>
    suspend fun updateRideStatus(rideId: String, status: RideStatus): Result<Unit>
    suspend fun getRideById(rideId: String): Result<Ride?>
    suspend fun getUserRides(userId: String): Result<List<Ride>>
    suspend fun assignDriver(rideId: String): Result<Driver>
    suspend fun calculateEstimatedPrice(pickup: Location, destination: Location, rideType: RideType): Result<Double>
    suspend fun completeRide(rideId: String, actualPrice: Double): Result<Unit>
    suspend fun rateRide(rideId: String, rating: Int, feedback: String?): Result<Unit>
    suspend fun updateDriverLocation(driverId: String, location: Location): Result<Unit>
    suspend fun getDriverLocation(driverId: String): Result<Location?>
    suspend fun startTrip(rideId: String): Result<Unit>
    suspend fun simulateDriverMovement(rideId: String, pickupLocation: Location, destinationLocation: Location): Result<Unit>
}

class RideRepositoryImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : RideRepository {

    override suspend fun createRide(ride: Ride): Result<String> {
        return try {
            val rideRef = firestore.collection("rides").document()
            val rideWithId = ride.copy(id = rideRef.id)
            rideRef.set(rideWithId).await()
            Result.success(rideRef.id)
        } catch (e: Exception) {
            when {
                e.message?.contains("PERMISSION_DENIED", ignoreCase = true) == true ->
                    Result.failure(Exception("Access denied. Please check your account permissions."))
                e.message?.contains("UNAVAILABLE", ignoreCase = true) == true ->
                    Result.failure(Exception("Service temporarily unavailable. Please try again."))
                e.message?.contains("DEADLINE_EXCEEDED", ignoreCase = true) == true ->
                    Result.failure(Exception("Request timeout. Please check your connection and try again."))
                else ->
                    Result.failure(Exception("Failed to create ride request: ${e.message ?: "Unknown error"}"))
            }
        }
    }

    override suspend fun updateRideStatus(rideId: String, status: RideStatus): Result<Unit> {
        return try {
            firestore.collection("rides")
                .document(rideId)
                .update("status", status)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            when {
                e.message?.contains("NOT_FOUND", ignoreCase = true) == true ->
                    Result.failure(Exception("Ride not found. It may have been cancelled."))
                e.message?.contains("PERMISSION_DENIED", ignoreCase = true) == true ->
                    Result.failure(Exception("Access denied. Cannot update ride status."))
                e.message?.contains("UNAVAILABLE", ignoreCase = true) == true ->
                    Result.failure(Exception("Service temporarily unavailable. Please try again."))
                else ->
                    Result.failure(Exception("Failed to update ride status: ${e.message ?: "Unknown error"}"))
            }
        }
    }

    override suspend fun getRideById(rideId: String): Result<Ride?> {
        return try {
            val document = firestore.collection("rides")
                .document(rideId)
                .get()
                .await()
            
            val ride = if (document.exists()) {
                document.toObject(Ride::class.java)
            } else {
                null
            }
            Result.success(ride)
        } catch (e: Exception) {
            when {
                e.message?.contains("NOT_FOUND", ignoreCase = true) == true ->
                    Result.failure(Exception("Ride not found."))
                e.message?.contains("PERMISSION_DENIED", ignoreCase = true) == true ->
                    Result.failure(Exception("Access denied. Cannot access ride details."))
                e.message?.contains("UNAVAILABLE", ignoreCase = true) == true ->
                    Result.failure(Exception("Service temporarily unavailable. Please try again."))
                else ->
                    Result.failure(Exception("Failed to load ride details: ${e.message ?: "Unknown error"}"))
            }
        }
    }

    override suspend fun getUserRides(userId: String): Result<List<Ride>> {
        return try {
            val querySnapshot = firestore.collection("rides")
                .whereEqualTo("userId", userId)
                .orderBy("requestTime", Query.Direction.DESCENDING)
                .get()
                .await()
            
            val rides = querySnapshot.documents.mapNotNull { document ->
                document.toObject(Ride::class.java)
            }
            Result.success(rides)
        } catch (e: Exception) {
            when {
                e.message?.contains("PERMISSION_DENIED", ignoreCase = true) == true ->
                    Result.failure(Exception("Access denied. Cannot access ride history."))
                e.message?.contains("UNAVAILABLE", ignoreCase = true) == true ->
                    Result.failure(Exception("Service temporarily unavailable. Please try again."))
                e.message?.contains("DEADLINE_EXCEEDED", ignoreCase = true) == true ->
                    Result.failure(Exception("Request timeout. Please check your connection."))
                else ->
                    Result.failure(Exception("Failed to load ride history: ${e.message ?: "Unknown error"}"))
            }
        }
    }

    override suspend fun assignDriver(rideId: String): Result<Driver> {
        return try {
            // Get ride details to determine pickup location for driver positioning
            val rideDoc = firestore.collection("rides").document(rideId).get().await()
            val ride = rideDoc.toObject(Ride::class.java)
            
            if (ride == null) {
                return Result.failure(Exception("Ride not found. Please try requesting again."))
            }
            
            // Simulate driver assignment with mock data and nearby locations
            val mockDrivers = listOf(
                Driver(
                    id = "driver_1",
                    name = "John Doe",
                    rating = 4.8,
                    vehicleInfo = "Toyota Camry - ABC 123",
                    phoneNumber = "+254700123456",
                    currentLocation = generateNearbyLocation(ride.pickupLocation)
                ),
                Driver(
                    id = "driver_2", 
                    name = "Jane Smith",
                    rating = 4.9,
                    vehicleInfo = "Honda Civic - XYZ 789",
                    phoneNumber = "+254700654321",
                    currentLocation = generateNearbyLocation(ride.pickupLocation)
                ),
                Driver(
                    id = "driver_3",
                    name = "Mike Johnson",
                    rating = 4.7,
                    vehicleInfo = "Nissan Sentra - DEF 456",
                    phoneNumber = "+254700987654",
                    currentLocation = generateNearbyLocation(ride.pickupLocation)
                )
            )
            
            val assignedDriver = mockDrivers.random()
            
            // Store driver location in Firebase
            firestore.collection("drivers")
                .document(assignedDriver.id)
                .set(assignedDriver)
                .await()
            
            // Update ride with driver assignment
            firestore.collection("rides")
                .document(rideId)
                .update(
                    mapOf(
                        "driverId" to assignedDriver.id,
                        "status" to RideStatus.DRIVER_ASSIGNED
                    )
                )
                .await()
            
            Result.success(assignedDriver)
        } catch (e: Exception) {
            when {
                e.message?.contains("NOT_FOUND", ignoreCase = true) == true ->
                    Result.failure(Exception("No drivers available in your area. Please try again."))
                e.message?.contains("UNAVAILABLE", ignoreCase = true) == true ->
                    Result.failure(Exception("Driver assignment service unavailable. Please try again."))
                e.message?.contains("DEADLINE_EXCEEDED", ignoreCase = true) == true ->
                    Result.failure(Exception("Driver search timeout. Please try again."))
                else ->
                    Result.failure(Exception("Failed to find driver: ${e.message ?: "No drivers available"}"))
            }
        }
    }

    override suspend fun calculateEstimatedPrice(
        pickup: Location,
        destination: Location,
        rideType: RideType
    ): Result<Double> {
        return try {
            // Simple distance calculation (Haversine formula approximation)
            val distance = calculateDistance(pickup, destination)
            
            val basePrice = when (rideType) {
                RideType.UBER_X -> 50.0
                RideType.UBER_XL -> 80.0
                RideType.UBER_COMFORT -> 70.0
            }
            
            val pricePerKm = when (rideType) {
                RideType.UBER_X -> 25.0
                RideType.UBER_XL -> 35.0
                RideType.UBER_COMFORT -> 30.0
            }
            
            val estimatedPrice = basePrice + (distance * pricePerKm)
            Result.success(estimatedPrice)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to calculate price: ${e.message ?: "Unknown error"}"))
        }
    }

    override suspend fun completeRide(rideId: String, actualPrice: Double): Result<Unit> {
        return try {
            firestore.collection("rides")
                .document(rideId)
                .update(
                    mapOf(
                        "status" to RideStatus.COMPLETED,
                        "actualPrice" to actualPrice,
                        "endTime" to System.currentTimeMillis()
                    )
                )
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            when {
                e.message?.contains("NOT_FOUND", ignoreCase = true) == true ->
                    Result.failure(Exception("Ride not found. Cannot complete trip."))
                e.message?.contains("PERMISSION_DENIED", ignoreCase = true) == true ->
                    Result.failure(Exception("Access denied. Cannot complete trip."))
                e.message?.contains("UNAVAILABLE", ignoreCase = true) == true ->
                    Result.failure(Exception("Service temporarily unavailable. Please try again."))
                else ->
                    Result.failure(Exception("Failed to complete trip: ${e.message ?: "Unknown error"}"))
            }
        }
    }

    override suspend fun rateRide(rideId: String, rating: Int, feedback: String?): Result<Unit> {
        return try {
            if (rating < 1 || rating > 5) {
                return Result.failure(Exception("Rating must be between 1 and 5 stars"))
            }

            val updates = mutableMapOf<String, Any>(
                "rating" to rating
            )
            feedback?.let { updates["feedback"] = it }
            
            firestore.collection("rides")
                .document(rideId)
                .update(updates)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            when {
                e.message?.contains("NOT_FOUND", ignoreCase = true) == true ->
                    Result.failure(Exception("Ride not found. Cannot submit rating."))
                e.message?.contains("PERMISSION_DENIED", ignoreCase = true) == true ->
                    Result.failure(Exception("Access denied. Cannot submit rating."))
                e.message?.contains("UNAVAILABLE", ignoreCase = true) == true ->
                    Result.failure(Exception("Service temporarily unavailable. Please try again."))
                else ->
                    Result.failure(Exception("Failed to submit rating: ${e.message ?: "Unknown error"}"))
            }
        }
    }

    override suspend fun updateDriverLocation(driverId: String, location: Location): Result<Unit> {
        return try {
            firestore.collection("drivers")
                .document(driverId)
                .update("currentLocation", location)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            when {
                e.message?.contains("NOT_FOUND", ignoreCase = true) == true ->
                    Result.failure(Exception("Driver not found."))
                e.message?.contains("PERMISSION_DENIED", ignoreCase = true) == true ->
                    Result.failure(Exception("Access denied. Cannot update driver location."))
                else ->
                    Result.failure(Exception("Failed to update driver location: ${e.message ?: "Unknown error"}"))
            }
        }
    }

    override suspend fun getDriverLocation(driverId: String): Result<Location?> {
        return try {
            val document = firestore.collection("drivers")
                .document(driverId)
                .get()
                .await()
            
            val driver = if (document.exists()) {
                document.toObject(Driver::class.java)
            } else {
                null
            }
            Result.success(driver?.currentLocation)
        } catch (e: Exception) {
            when {
                e.message?.contains("NOT_FOUND", ignoreCase = true) == true ->
                    Result.failure(Exception("Driver not found."))
                e.message?.contains("PERMISSION_DENIED", ignoreCase = true) == true ->
                    Result.failure(Exception("Access denied. Cannot get driver location."))
                else ->
                    Result.failure(Exception("Failed to get driver location: ${e.message ?: "Unknown error"}"))
            }
        }
    }

    override suspend fun startTrip(rideId: String): Result<Unit> {
        return try {
            firestore.collection("rides")
                .document(rideId)
                .update(
                    mapOf(
                        "status" to RideStatus.IN_PROGRESS,
                        "startTime" to System.currentTimeMillis()
                    )
                )
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            when {
                e.message?.contains("NOT_FOUND", ignoreCase = true) == true ->
                    Result.failure(Exception("Ride not found. Cannot start trip."))
                e.message?.contains("PERMISSION_DENIED", ignoreCase = true) == true ->
                    Result.failure(Exception("Access denied. Cannot start trip."))
                e.message?.contains("UNAVAILABLE", ignoreCase = true) == true ->
                    Result.failure(Exception("Service temporarily unavailable. Please try again."))
                else ->
                    Result.failure(Exception("Failed to start trip: ${e.message ?: "Unknown error"}"))
            }
        }
    }

    override suspend fun simulateDriverMovement(
        rideId: String, 
        pickupLocation: Location, 
        destinationLocation: Location
    ): Result<Unit> {
        return try {
            val ride = getRideById(rideId).getOrNull()
            if (ride?.driverId == null) {
                return Result.failure(Exception("No driver assigned to ride"))
            }

            // Simulate driver movement in phases
            when (ride.status) {
                RideStatus.DRIVER_ASSIGNED -> {
                    // Driver moving to pickup
                    simulateMovementToPickup(ride.driverId, pickupLocation)
                }
                RideStatus.DRIVER_ARRIVING -> {
                    // Driver has arrived at pickup
                    updateRideStatus(rideId, RideStatus.DRIVER_ARRIVING)
                }
                RideStatus.IN_PROGRESS -> {
                    // Driver moving to destination
                    simulateMovementToDestination(ride.driverId, destinationLocation)
                }
                else -> {
                    // No movement needed
                }
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to simulate driver movement: ${e.message ?: "Unknown error"}"))
        }
    }

    private suspend fun simulateMovementToPickup(driverId: String, pickupLocation: Location) {
        val currentLocation = getDriverLocation(driverId).getOrNull() ?: return
        
        // Simulate movement over 5 steps (5 minutes)
        val steps = 5
        for (i in 1..steps) {
            kotlinx.coroutines.delay(60000) // 1 minute delay
            
            val progress = i.toDouble() / steps
            val newLat = currentLocation.latitude + (pickupLocation.latitude - currentLocation.latitude) * progress
            val newLng = currentLocation.longitude + (pickupLocation.longitude - currentLocation.longitude) * progress
            
            val newLocation = Location(
                latitude = newLat,
                longitude = newLng,
                address = "En route to pickup"
            )
            
            updateDriverLocation(driverId, newLocation)
        }
        
        // Mark as arrived at pickup
        updateDriverLocation(driverId, pickupLocation)
    }

    private suspend fun simulateMovementToDestination(driverId: String, destinationLocation: Location) {
        val currentLocation = getDriverLocation(driverId).getOrNull() ?: return
        
        // Simulate movement over 10 steps (10 minutes)
        val steps = 10
        for (i in 1..steps) {
            kotlinx.coroutines.delay(60000) // 1 minute delay
            
            val progress = i.toDouble() / steps
            val newLat = currentLocation.latitude + (destinationLocation.latitude - currentLocation.latitude) * progress
            val newLng = currentLocation.longitude + (destinationLocation.longitude - currentLocation.longitude) * progress
            
            val newLocation = Location(
                latitude = newLat,
                longitude = newLng,
                address = "En route to destination"
            )
            
            updateDriverLocation(driverId, newLocation)
        }
        
        // Mark as arrived at destination
        updateDriverLocation(driverId, destinationLocation)
    }

    private fun generateNearbyLocation(baseLocation: Location): Location {
        // Generate a location within 2km radius of the base location
        val radiusInDegrees = 0.018 // Approximately 2km
        val randomAngle = Random.nextDouble() * 2 * Math.PI
        val randomRadius = Random.nextDouble() * radiusInDegrees
        
        val deltaLat = randomRadius * kotlin.math.cos(randomAngle)
        val deltaLng = randomRadius * kotlin.math.sin(randomAngle)
        
        return Location(
            latitude = baseLocation.latitude + deltaLat,
            longitude = baseLocation.longitude + deltaLng,
            address = "Driver location"
        )
    }

    private fun calculateDistance(pickup: Location, destination: Location): Double {
        val earthRadius = 6371.0 // Earth's radius in kilometers
        
        val lat1Rad = Math.toRadians(pickup.latitude)
        val lat2Rad = Math.toRadians(destination.latitude)
        val deltaLatRad = Math.toRadians(destination.latitude - pickup.latitude)
        val deltaLonRad = Math.toRadians(destination.longitude - pickup.longitude)
        
        val a = kotlin.math.sin(deltaLatRad / 2) * kotlin.math.sin(deltaLatRad / 2) +
                kotlin.math.cos(lat1Rad) * kotlin.math.cos(lat2Rad) *
                kotlin.math.sin(deltaLonRad / 2) * kotlin.math.sin(deltaLonRad / 2)
        
        val c = 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
        
        return earthRadius * c
    }
}