package co.ke.snilloc.uberclone

import co.ke.snilloc.uberclone.ui.base.ErrorHandler
import co.ke.snilloc.uberclone.ui.base.LoadingStateManager
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.firestore.FirebaseFirestoreException
import org.junit.Test
import org.junit.Assert.*
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Test class for error handling and loading state management
 */
class ErrorHandlingTest {

    @Test
    fun testErrorHandler_NetworkErrors() {
        // Test UnknownHostException
        val networkError = UnknownHostException("Network unreachable")
        val networkMessage = ErrorHandler.getErrorMessage(networkError)
        assertEquals("No internet connection. Please check your network.", networkMessage)
        assertTrue(ErrorHandler.isRecoverableError(networkError))

        // Test SocketTimeoutException
        val timeoutError = SocketTimeoutException("Connection timeout")
        val timeoutMessage = ErrorHandler.getErrorMessage(timeoutError)
        assertEquals("Connection timeout. Please try again.", timeoutMessage)
        assertTrue(ErrorHandler.isRecoverableError(timeoutError))

        // Test IOException
        val ioError = IOException("Network error")
        val ioMessage = ErrorHandler.getErrorMessage(ioError)
        assertEquals("Network error. Please check your connection.", ioMessage)
        assertTrue(ErrorHandler.isRecoverableError(ioError))
    }

    @Test
    fun testErrorHandler_FirebaseErrors() {
        // Test FirebaseNetworkException
        val firebaseNetworkError = FirebaseNetworkException("Network error")
        val firebaseNetworkMessage = ErrorHandler.getErrorMessage(firebaseNetworkError)
        assertEquals("Network error. Please check your internet connection.", firebaseNetworkMessage)
        assertTrue(ErrorHandler.isRecoverableError(firebaseNetworkError))

        // Test FirebaseTooManyRequestsException
        val tooManyRequestsError = FirebaseTooManyRequestsException("Too many requests")
        val tooManyRequestsMessage = ErrorHandler.getErrorMessage(tooManyRequestsError)
        assertEquals("Too many requests. Please try again later.", tooManyRequestsMessage)
        assertTrue(ErrorHandler.isRecoverableError(tooManyRequestsError))
    }

    @Test
    fun testErrorHandler_FirestoreErrors() {
        // Test UNAVAILABLE error
        val unavailableError = FirebaseFirestoreException(
            "Service unavailable", 
            FirebaseFirestoreException.Code.UNAVAILABLE
        )
        val unavailableMessage = ErrorHandler.getErrorMessage(unavailableError)
        assertEquals("Service temporarily unavailable. Please try again.", unavailableMessage)
        assertTrue(ErrorHandler.isRecoverableError(unavailableError))

        // Test PERMISSION_DENIED error
        val permissionError = FirebaseFirestoreException(
            "Permission denied", 
            FirebaseFirestoreException.Code.PERMISSION_DENIED
        )
        val permissionMessage = ErrorHandler.getErrorMessage(permissionError)
        assertEquals("Access denied. Please check your permissions.", permissionMessage)
        assertFalse(ErrorHandler.isRecoverableError(permissionError))

        // Test NOT_FOUND error
        val notFoundError = FirebaseFirestoreException(
            "Document not found", 
            FirebaseFirestoreException.Code.NOT_FOUND
        )
        val notFoundMessage = ErrorHandler.getErrorMessage(notFoundError)
        assertEquals("Requested data not found.", notFoundMessage)
        assertFalse(ErrorHandler.isRecoverableError(notFoundError))
    }

    @Test
    fun testErrorHandler_SecurityErrors() {
        // Test SecurityException with permission message
        val permissionSecurityError = SecurityException("Location permission denied")
        val permissionSecurityMessage = ErrorHandler.getErrorMessage(permissionSecurityError)
        assertEquals("Location permission is required for this feature.", permissionSecurityMessage)

        // Test general SecurityException
        val generalSecurityError = SecurityException("Security error")
        val generalSecurityMessage = ErrorHandler.getErrorMessage(generalSecurityError)
        assertEquals("Security error occurred. Please try again.", generalSecurityMessage)
    }

    @Test
    fun testErrorHandler_GeneralErrors() {
        // Test IllegalStateException
        val illegalStateError = IllegalStateException("Invalid state")
        val illegalStateMessage = ErrorHandler.getErrorMessage(illegalStateError)
        assertEquals("App is in an invalid state. Please restart the app.", illegalStateMessage)

        // Test IllegalArgumentException
        val illegalArgError = IllegalArgumentException("Invalid argument")
        val illegalArgMessage = ErrorHandler.getErrorMessage(illegalArgError)
        assertEquals("Invalid input provided. Please check your data.", illegalArgMessage)

        // Test generic Exception
        val genericError = Exception("Something went wrong")
        val genericMessage = ErrorHandler.getErrorMessage(genericError)
        assertEquals("Something went wrong", genericMessage)

        // Test Exception with null message
        val nullMessageError = Exception(null as String?)
        val nullMessage = ErrorHandler.getErrorMessage(nullMessageError)
        assertEquals("An unexpected error occurred. Please try again.", nullMessage)
    }

    @Test
    fun testLoadingStateManager_BasicFunctionality() {
        val loadingManager = LoadingStateManager()

        // Test initial state
        assertFalse(loadingManager.isLoading.value)
        assertFalse(loadingManager.loadingState.value.isLoading)
        assertEquals(LoadingStateManager.LoadingType.GENERAL, loadingManager.loadingState.value.loadingType)
        assertNull(loadingManager.loadingState.value.message)

        // Test setting loading state
        loadingManager.setLoading(true, LoadingStateManager.LoadingType.LOCATION, "Getting location...")
        assertTrue(loadingManager.isLoading.value)
        assertTrue(loadingManager.loadingState.value.isLoading)
        assertEquals(LoadingStateManager.LoadingType.LOCATION, loadingManager.loadingState.value.loadingType)
        assertEquals("Getting location...", loadingManager.loadingState.value.message)

        // Test clearing loading state
        loadingManager.clearLoading()
        assertFalse(loadingManager.isLoading.value)
        assertFalse(loadingManager.loadingState.value.isLoading)
    }

    @Test
    fun testLoadingStateManager_SpecificLoadingTypes() {
        val loadingManager = LoadingStateManager()

        // Test location loading
        loadingManager.setLocationLoading(true)
        assertTrue(loadingManager.isLoading.value)
        assertEquals(LoadingStateManager.LoadingType.LOCATION, loadingManager.loadingState.value.loadingType)
        assertEquals("Getting your location...", loadingManager.loadingState.value.message)

        // Test network loading
        loadingManager.setNetworkLoading(true, "Connecting to server...")
        assertEquals(LoadingStateManager.LoadingType.NETWORK, loadingManager.loadingState.value.loadingType)
        assertEquals("Connecting to server...", loadingManager.loadingState.value.message)

        // Test search loading
        loadingManager.setSearchLoading(true)
        assertEquals(LoadingStateManager.LoadingType.SEARCH, loadingManager.loadingState.value.loadingType)
        assertEquals("Searching...", loadingManager.loadingState.value.message)

        // Test ride request loading
        loadingManager.setRideRequestLoading(true)
        assertEquals(LoadingStateManager.LoadingType.RIDE_REQUEST, loadingManager.loadingState.value.loadingType)
        assertEquals("Requesting ride...", loadingManager.loadingState.value.message)

        // Test driver search loading
        loadingManager.setDriverSearchLoading(true)
        assertEquals(LoadingStateManager.LoadingType.DRIVER_SEARCH, loadingManager.loadingState.value.loadingType)
        assertEquals("Finding drivers...", loadingManager.loadingState.value.message)

        // Test profile update loading
        loadingManager.setProfileUpdateLoading(true)
        assertEquals(LoadingStateManager.LoadingType.PROFILE_UPDATE, loadingManager.loadingState.value.loadingType)
        assertEquals("Updating profile...", loadingManager.loadingState.value.message)

        // Test ride history loading
        loadingManager.setRideHistoryLoading(true)
        assertEquals(LoadingStateManager.LoadingType.RIDE_HISTORY, loadingManager.loadingState.value.loadingType)
        assertEquals("Loading ride history...", loadingManager.loadingState.value.message)
    }

    @Test
    fun testLoadingStateManager_CustomMessages() {
        val loadingManager = LoadingStateManager()

        // Test custom message for location loading
        loadingManager.setLocationLoading(true, "Finding your exact location...")
        assertEquals("Finding your exact location...", loadingManager.loadingState.value.message)

        // Test custom message for search loading
        loadingManager.setSearchLoading(true, "Searching nearby places...")
        assertEquals("Searching nearby places...", loadingManager.loadingState.value.message)
    }
}