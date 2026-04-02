package co.ke.snilloc.uberclone

import android.content.Context
import android.content.SharedPreferences
import co.ke.snilloc.uberclone.data.onboarding.OnboardingManager
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.*

class OnboardingManagerTest {

    @Mock
    private lateinit var mockContext: Context

    @Mock
    private lateinit var mockSharedPreferences: SharedPreferences

    @Mock
    private lateinit var mockEditor: SharedPreferences.Editor

    private lateinit var onboardingManager: OnboardingManager

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        
        whenever(mockContext.getSharedPreferences(any(), any()))
            .thenReturn(mockSharedPreferences)
        whenever(mockSharedPreferences.edit()).thenReturn(mockEditor)
        whenever(mockEditor.putBoolean(any(), any())).thenReturn(mockEditor)
        whenever(mockEditor.putString(any(), any())).thenReturn(mockEditor)
        
        onboardingManager = OnboardingManager(mockContext)
    }

    @Test
    fun `hasSeenGetStarted returns false by default`() {
        whenever(mockSharedPreferences.getBoolean("has_seen_get_started", false))
            .thenReturn(false)

        assertFalse(onboardingManager.hasSeenGetStarted())
    }

    @Test
    fun `markGetStartedSeen saves true value`() {
        onboardingManager.markGetStartedSeen()

        verify(mockEditor).putBoolean("has_seen_get_started", true)
        verify(mockEditor).apply()
    }

    @Test
    fun `hasCompletedOnboarding returns true when all steps completed`() {
        whenever(mockSharedPreferences.getBoolean("has_seen_get_started", false))
            .thenReturn(true)
        whenever(mockSharedPreferences.getBoolean("is_phone_verified", false))
            .thenReturn(true)
        whenever(mockSharedPreferences.getBoolean("has_completed_profile", false))
            .thenReturn(true)

        assertTrue(onboardingManager.hasCompletedOnboarding())
    }

    @Test
    fun `hasCompletedOnboarding returns false when any step incomplete`() {
        whenever(mockSharedPreferences.getBoolean("has_seen_get_started", false))
            .thenReturn(true)
        whenever(mockSharedPreferences.getBoolean("is_phone_verified", false))
            .thenReturn(false)
        whenever(mockSharedPreferences.getBoolean("has_completed_profile", false))
            .thenReturn(true)

        assertFalse(onboardingManager.hasCompletedOnboarding())
    }

    @Test
    fun `clearOnboardingData clears all preferences`() {
        onboardingManager.clearOnboardingData()

        verify(mockEditor).clear()
        verify(mockEditor).apply()
    }
}