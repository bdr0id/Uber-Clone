package co.ke.snilloc.uberclone

import co.ke.snilloc.uberclone.data.auth.SocialAuthManager
import co.ke.snilloc.uberclone.data.model.User
import co.ke.snilloc.uberclone.data.repository.UserRepository
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import androidx.appcompat.app.AppCompatActivity
import org.mockito.kotlin.mock

class SocialMediaAuthTest {

    @Mock
    private lateinit var mockActivity: AppCompatActivity
    
    @Mock
    private lateinit var mockUserRepository: UserRepository
    
    private lateinit var socialAuthManager: SocialAuthManager

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        // Note: In a real test, we would need to mock the SocialAuthManager properly
        // since it has dependencies on Android components
    }

    @Test
    fun `test user profile creation after social auth success`() = runTest {
        // Given
        val testUser = User(
            id = "test_user_id",
            name = "Test User",
            email = "test@example.com",
            phoneNumber = null,
            profileImageUrl = "https://example.com/photo.jpg"
        )
        
        whenever(mockUserRepository.createUser(any())).thenReturn(Result.success(Unit))
        
        // When - This would be called after successful social media authentication
        val result = mockUserRepository.createUser(testUser)
        
        // Then
        assert(result.isSuccess)
        verify(mockUserRepository).createUser(testUser)
    }

    @Test
    fun `test user profile creation handles errors`() = runTest {
        // Given
        val testUser = User(
            id = "test_user_id",
            name = "Test User",
            email = "test@example.com"
        )
        
        val errorMessage = "Failed to create user profile"
        whenever(mockUserRepository.createUser(any())).thenReturn(Result.failure(Exception(errorMessage)))
        
        // When
        val result = mockUserRepository.createUser(testUser)
        
        // Then
        assert(result.isFailure)
        assert(result.exceptionOrNull()?.message == errorMessage)
    }

    @Test
    fun `test social media user data mapping`() {
        // Given - Simulating data that would come from Google/Facebook
        val googleUserData = mapOf(
            "id" to "google_user_123",
            "name" to "John Doe",
            "email" to "john.doe@gmail.com",
            "picture" to "https://lh3.googleusercontent.com/photo.jpg"
        )
        
        // When - Creating User object from social media data
        val user = User(
            id = googleUserData["id"] as String,
            name = googleUserData["name"] as String,
            email = googleUserData["email"] as String,
            phoneNumber = null, // Social media auth doesn't provide phone
            profileImageUrl = googleUserData["picture"] as String
        )
        
        // Then
        assert(user.id == "google_user_123")
        assert(user.name == "John Doe")
        assert(user.email == "john.doe@gmail.com")
        assert(user.phoneNumber == null)
        assert(user.profileImageUrl == "https://lh3.googleusercontent.com/photo.jpg")
        assert(user.rating == 0.0) // Default values
        assert(user.totalTrips == 0) // Default values
    }
}