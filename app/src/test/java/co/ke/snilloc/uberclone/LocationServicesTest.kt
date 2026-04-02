package co.ke.snilloc.uberclone

import co.ke.snilloc.uberclone.data.model.Location
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for location services implementation
 */
class LocationServicesTest {

    @Test
    fun testLocationModelCreation() {
        val location = Location(
            latitude = 37.7749,
            longitude = -122.4194,
            address = "San Francisco, CA",
            placeId = "test_place_id"
        )
        
        assertEquals(37.7749, location.latitude, 0.0001)
        assertEquals(-122.4194, location.longitude, 0.0001)
        assertEquals("San Francisco, CA", location.address)
        assertEquals("test_place_id", location.placeId)
    }

    @Test
    fun testLocationModelDefaults() {
        val location = Location()
        
        assertEquals(0.0, location.latitude, 0.0001)
        assertEquals(0.0, location.longitude, 0.0001)
        assertEquals("", location.address)
        assertNull(location.placeId)
    }

    @Test
    fun testLocationModelWithPartialData() {
        val location = Location(
            latitude = 40.7128,
            longitude = -74.0060,
            address = "New York, NY"
        )
        
        assertEquals(40.7128, location.latitude, 0.0001)
        assertEquals(-74.0060, location.longitude, 0.0001)
        assertEquals("New York, NY", location.address)
        assertNull(location.placeId)
    }
}