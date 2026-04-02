package co.ke.snilloc.uberclone.data.onboarding

import android.content.Context
import android.content.SharedPreferences

class OnboardingManager(context: Context) {
    
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME, Context.MODE_PRIVATE
    )
    
    companion object {
        private const val PREFS_NAME = "uber_clone_onboarding"
        private const val KEY_HAS_SEEN_GET_STARTED = "has_seen_get_started"
        private const val KEY_IS_PHONE_VERIFIED = "is_phone_verified"
        private const val KEY_HAS_COMPLETED_PROFILE = "has_completed_profile"
        private const val KEY_SELECTED_COUNTRY_CODE = "selected_country_code"
        private const val KEY_PHONE_NUMBER = "phone_number"
    }
    
    /**
     * Check if user has seen the get started screen
     */
    fun hasSeenGetStarted(): Boolean {
        return sharedPreferences.getBoolean(KEY_HAS_SEEN_GET_STARTED, false)
    }
    
    /**
     * Mark that user has seen the get started screen
     */
    fun markGetStartedSeen() {
        sharedPreferences.edit()
            .putBoolean(KEY_HAS_SEEN_GET_STARTED, true)
            .apply()
    }
    
    /**
     * Check if user has verified their phone number
     */
    fun isPhoneVerified(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_PHONE_VERIFIED, false)
    }
    
    /**
     * Mark phone number as verified
     */
    fun markPhoneVerified() {
        sharedPreferences.edit()
            .putBoolean(KEY_IS_PHONE_VERIFIED, true)
            .apply()
    }
    
    /**
     * Check if user has completed their profile setup
     */
    fun hasCompletedProfile(): Boolean {
        return sharedPreferences.getBoolean(KEY_HAS_COMPLETED_PROFILE, false)
    }
    
    /**
     * Mark profile as completed
     */
    fun markProfileCompleted() {
        sharedPreferences.edit()
            .putBoolean(KEY_HAS_COMPLETED_PROFILE, true)
            .apply()
    }
    
    /**
     * Get selected country code
     */
    fun getSelectedCountryCode(): String? {
        return sharedPreferences.getString(KEY_SELECTED_COUNTRY_CODE, null)
    }
    
    /**
     * Save selected country code
     */
    fun saveSelectedCountryCode(countryCode: String) {
        sharedPreferences.edit()
            .putString(KEY_SELECTED_COUNTRY_CODE, countryCode)
            .apply()
    }
    
    /**
     * Get stored phone number
     */
    fun getPhoneNumber(): String? {
        return sharedPreferences.getString(KEY_PHONE_NUMBER, null)
    }
    
    /**
     * Store phone number and country code
     */
    fun storePhoneNumber(phoneNumber: String, countryCode: String) {
        sharedPreferences.edit()
            .putString(KEY_PHONE_NUMBER, phoneNumber)
            .putString(KEY_SELECTED_COUNTRY_CODE, countryCode)
            .apply()
    }
    
    /**
     * Clear all onboarding data (useful for testing or logout)
     */
    fun clearOnboardingData() {
        sharedPreferences.edit().clear().apply()
    }
    
    /**
     * Check if user has completed the full onboarding flow
     */
    fun hasCompletedOnboarding(): Boolean {
        return hasSeenGetStarted() && isPhoneVerified() && hasCompletedProfile()
    }
}