package co.ke.snilloc.uberclone

import co.ke.snilloc.uberclone.data.model.Driver
import co.ke.snilloc.uberclone.data.model.Location
import co.ke.snilloc.uberclone.data.model.Ride
import co.ke.snilloc.uberclone.data.model.RideStatus
import co.ke.snilloc.uberclone.data.model.RideType
import co.ke.snilloc.uberclone.data.model.User
import org.junit.Assert.*
import org.junit.Test

class RideRequestTest {

    @Test
    fun `test ride creation with valid data`() {
        // Given
        val user = User(
            id = "user123",
            name = "Test User",
            email = "test@example.com",
            phoneNumber = "+254700000000"
        )
        
        val pickupLocation = Location(
            latitude = -1.2921,
            longitude = 36.8219,
            address = "Nairobi CBD"
        )
        
        val destinationLocation = Location(
            latitude = -1.3032,
            longitude = 36.8856,
            address = "Westlands"
        )
        
        // When
        val ride = Ride(
            id = "ride123",
            userId = user.id,
            pickupLocation = pickupLocation,
            destinationLocation = destinationLocation,
            rideType = RideType.UBER_X,
            status = RideStatus.REQUESTED,
            estimatedPrice = 250.0,
            requestTime = System.currentTimeMillis()
        )
        
        // Then
        assertEquals("ride123", ride.id)
        assertEquals(user.id, ride.userId)
        assertEquals(pickupLocation, ride.pickupLocation)
        assertEquals(destinationLocation, ride.destinationLocation)
        assertEquals(RideType.UBER_X, ride.rideType)
        assertEquals(RideStatus.REQUESTED, ride.status)
        assertEquals(250.0, ride.estimatedPrice, 0.01)
        assertNull(ride.driverId)
        assertNull(ride.actualPrice)
    }

    @Test
    fun `test ride status progression`() {
        // Given
        val ride = Ride(
            id = "ride123",
            userId = "user123",
            status = RideStatus.REQUESTED
        )
        
        // When & Then - Test status progression
        assertEquals(RideStatus.REQUESTED, ride.status)
        
        val driverAssigned = ride.copy(
            status = RideStatus.DRIVER_ASSIGNED,
            driverId = "driver123"
        )
        assertEquals(RideStatus.DRIVER_ASSIGNED, driverAssigned.status)
        assertEquals("driver123", driverAssigned.driverId)
        
        val driverArriving = driverAssigned.copy(status = RideStatus.DRIVER_ARRIVING)
        assertEquals(RideStatus.DRIVER_ARRIVING, driverArriving.status)
        
        val inProgress = driverArriving.copy(
            status = RideStatus.IN_PROGRESS,
            startTime = System.currentTimeMillis()
        )
        assertEquals(RideStatus.IN_PROGRESS, inProgress.status)
        assertNotNull(inProgress.startTime)
        
        val completed = inProgress.copy(
            status = RideStatus.COMPLETED,
            actualPrice = 275.0,
            endTime = System.currentTimeMillis()
        )
        assertEquals(RideStatus.COMPLETED, completed.status)
        assertEquals(275.0, completed.actualPrice!!, 0.01)
        assertNotNull(completed.endTime)
    }

    @Test
    fun `test driver model creation`() {
        // Given & When
        val driver = Driver(
            id = "driver123",
            name = "John Doe",
            rating = 4.8,
            vehicleInfo = "Toyota Camry - KCA 123A",
            phoneNumber = "+254700111111",
            currentLocation = Location(
                latitude = -1.2900,
                longitude = 36.8200,
                address = "Near pickup"
            )
        )
        
        // Then
        assertEquals("driver123", driver.id)
        assertEquals("John Doe", driver.name)
        assertEquals(4.8, driver.rating, 0.01)
        assertEquals("Toyota Camry - KCA 123A", driver.vehicleInfo)
        assertEquals("+254700111111", driver.phoneNumber)
        assertNotNull(driver.currentLocation)
    }

    @Test
    fun `test location model creation`() {
        // Given & When
        val location = Location(
            latitude = -1.2921,
            longitude = 36.8219,
            address = "Nairobi CBD",
            placeId = "place123"
        )
        
        // Then
        assertEquals(-1.2921, location.latitude, 0.0001)
        assertEquals(36.8219, location.longitude, 0.0001)
        assertEquals("Nairobi CBD", location.address)
        assertEquals("place123", location.placeId)
    }

    @Test
    fun `test ride type enum values`() {
        // Test that all ride types are available
        val rideTypes = RideType.values()
        
        assertTrue(rideTypes.contains(RideType.UBER_X))
        assertTrue(rideTypes.contains(RideType.UBER_XL))
        assertTrue(rideTypes.contains(RideType.UBER_COMFORT))
        assertEquals(3, rideTypes.size)
    }

    @Test
    fun `test ride status enum values`() {
        // Test that all ride statuses are available
        val statuses = RideStatus.values()
        
        assertTrue(statuses.contains(RideStatus.REQUESTED))
        assertTrue(statuses.contains(RideStatus.DRIVER_ASSIGNED))
        assertTrue(statuses.contains(RideStatus.DRIVER_ARRIVING))
        assertTrue(statuses.contains(RideStatus.IN_PROGRESS))
        assertTrue(statuses.contains(RideStatus.COMPLETED))
        assertTrue(statuses.contains(RideStatus.CANCELLED))
        assertEquals(6, statuses.size)
    }

    @Test
    fun `test user model creation`() {
        // Given & When
        val user = User(
            id = "user123",
            name = "Test User",
            email = "test@example.com",
            phoneNumber = "+254700000000",
            rating = 4.5,
            totalTrips = 25
        )
        
        // Then
        assertEquals("user123", user.id)
        assertEquals("Test User", user.name)
        assertEquals("test@example.com", user.email)
        assertEquals("+254700000000", user.phoneNumber)
        assertEquals(4.5, user.rating, 0.01)
        assertEquals(25, user.totalTrips)
    }

    @Test
    fun `test ride with rating and feedback`() {
        // Given
        val ride = Ride(
            id = "ride123",
            userId = "user123",
            status = RideStatus.COMPLETED,
            estimatedPrice = 250.0,
            actualPrice = 275.0
        )
        
        // When
        val ratedRide = ride.copy(
            rating = 5,
            feedback = "Great driver and smooth ride!"
        )
        
        // Then
        assertEquals(5, ratedRide.rating)
        assertEquals("Great driver and smooth ride!", ratedRide.feedback)
        assertEquals(RideStatus.COMPLETED, ratedRide.status)
    }

    @Test
    fun `test ride cancellation`() {
        // Given
        val ride = Ride(
            id = "ride123",
            userId = "user123",
            status = RideStatus.REQUESTED,
            estimatedPrice = 250.0
        )
        
        // When
        val cancelledRide = ride.copy(status = RideStatus.CANCELLED)
        
        // Then
        assertEquals(RideStatus.CANCELLED, cancelledRide.status)
        assertNull(cancelledRide.actualPrice)
        assertNull(cancelledRide.driverId)
    }
}