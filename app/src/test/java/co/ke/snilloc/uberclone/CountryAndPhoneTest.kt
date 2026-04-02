package co.ke.snilloc.uberclone

import co.ke.snilloc.uberclone.data.model.Country
import co.ke.snilloc.uberclone.utils.PhoneNumberValidator
import org.junit.Test
import org.junit.Assert.*

class CountryAndPhoneTest {

    @Test
    fun `test Kenya country data`() {
        val countries = Country.getEastAfricanCountries()
        val kenya = countries.first { it.code == "KE" }
        
        assertEquals("Kenya", kenya.name)
        assertEquals("KE", kenya.code)
        assertEquals("+254", kenya.phoneCode)
        assertEquals("🇰🇪", kenya.flag)
        assertEquals("0XXX XXX XXX", kenya.phoneFormat)
    }

    @Test
    fun `test valid Kenya phone number`() {
        val kenya = Country.getEastAfricanCountries().first { it.code == "KE" }
        val result = PhoneNumberValidator.validatePhoneNumber("0712345678", kenya)
        
        assertTrue("Valid Kenya number should pass validation", result.isValid)
        assertEquals("", result.errorMessage)
    }

    @Test
    fun `test invalid Kenya phone number`() {
        val kenya = Country.getEastAfricanCountries().first { it.code == "KE" }
        val result = PhoneNumberValidator.validatePhoneNumber("712345678", kenya)
        
        assertFalse("Invalid Kenya number should fail validation", result.isValid)
        assertTrue("Should have error message", result.errorMessage.isNotEmpty())
    }

    @Test
    fun `test phone number formatting`() {
        val kenya = Country.getEastAfricanCountries().first { it.code == "KE" }
        val formatted = PhoneNumberValidator.formatPhoneNumber("0712345678", kenya)
        
        assertEquals("0712 345 678", formatted)
    }

    @Test
    fun `test all East African countries exist`() {
        val countries = Country.getEastAfricanCountries()
        val countryCodes = countries.map { it.code }
        
        assertTrue("Should contain Kenya", countryCodes.contains("KE"))
        assertTrue("Should contain Uganda", countryCodes.contains("UG"))
        assertTrue("Should contain Tanzania", countryCodes.contains("TZ"))
        assertTrue("Should contain Rwanda", countryCodes.contains("RW"))
        assertTrue("Should contain Burundi", countryCodes.contains("BI"))
        assertTrue("Should contain Ethiopia", countryCodes.contains("ET"))
        assertTrue("Should contain South Sudan", countryCodes.contains("SS"))
        assertTrue("Should contain Djibouti", countryCodes.contains("DJ"))
        assertTrue("Should contain Eritrea", countryCodes.contains("ER"))
        assertTrue("Should contain Somalia", countryCodes.contains("SO"))
        
        assertEquals("Should have 10 countries", 10, countries.size)
    }
}