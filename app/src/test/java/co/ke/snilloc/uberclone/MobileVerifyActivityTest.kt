package co.ke.snilloc.uberclone

import co.ke.snilloc.uberclone.data.onboarding.OnboardingManager
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class MobileVerifyActivityTest {

    @Mock
    private lateinit var mockOnboardingManager: OnboardingManager

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun `test verification code validation logic`() {
        // Test empty code
        val emptyCode = ""
        assert(!isValidVerificationCode(emptyCode))
        
        // Test code with wrong length
        val shortCode = "123"
        assert(!isValidVerificationCode(shortCode))
        
        val longCode = "12345"
        assert(!isValidVerificationCode(longCode))
        
        // Test valid default code
        val validCode = "0000"
        assert(isValidVerificationCode(validCode))
        
        // Test invalid code with correct length
        val invalidCode = "1234"
        assert(!isValidVerificationCode(invalidCode))
    }

    @Test
    fun `test error message generation`() {
        // Test empty code error
        val emptyCodeError = getErrorMessage("")
        assert(emptyCodeError.contains("Please enter the 4-digit verification code"))
        
        // Test wrong length error
        val wrongLengthError = getErrorMessage("123")
        assert(wrongLengthError.contains("Verification code must be exactly 4 digits"))
        
        // Test invalid code error
        val invalidCodeError = getErrorMessage("1234")
        assert(invalidCodeError.contains("Invalid verification code"))
        assert(invalidCodeError.contains("0000"))
    }

    @Test
    fun `test phone number formatting`() {
        // Test phone number with +254 prefix removal
        val phoneWithPrefix = "+254712123456"
        val formatted = formatPhoneNumberForDisplay(phoneWithPrefix)
        assert(formatted == "712123456")
        
        // Test phone number without prefix
        val phoneWithoutPrefix = "712123456"
        val formattedWithoutPrefix = formatPhoneNumberForDisplay(phoneWithoutPrefix)
        assert(formattedWithoutPrefix == "712123456")
    }

    // Helper methods that simulate the logic from MobileVerifyActivity
    private fun isValidVerificationCode(code: String): Boolean {
        return code.isNotEmpty() && code.length == 4 && code == "0000"
    }

    private fun getErrorMessage(code: String): String {
        return when {
            code.isEmpty() -> "Please enter the 4-digit verification code"
            code.length != 4 -> "Verification code must be exactly 4 digits"
            code != "0000" -> "Invalid verification code. For testing, please use '0000'"
            else -> ""
        }
    }

    private fun formatPhoneNumberForDisplay(phoneNumber: String): String {
        return phoneNumber.removePrefix("+254").removePrefix("+")
    }
}