package co.ke.snilloc.uberclone

import co.ke.snilloc.uberclone.data.model.Country
import co.ke.snilloc.uberclone.utils.PhoneNumberValidator
import org.junit.Test
import org.junit.Assert.*

class PhoneNumberValidationTest {

    @Test
    fun testKenyaPhoneNumberValidation() {
        val kenya = Country.getEastAfricanCountries().first { it.code == "KE" }
        
        // Valid Kenya numbers
        assertTrue(PhoneNumberValidator.validatePhoneNumber("0712345678", kenya).isValid)
        assertTrue(PhoneNumberValidator.validatePhoneNumber("0123456789", kenya).isValid)
        
        // Invalid Kenya numbers
        assertFalse(PhoneNumberValidator.validatePhoneNumber("712345678", kenya).isValid) // Missing 0
        assertFalse(PhoneNumberValidator.validatePhoneNumber("071234567", kenya).isValid) // Too short
        assertFalse(PhoneNumberValidator.validatePhoneNumber("07123456789", kenya).isValid) // Too long
        assertFalse(PhoneNumberValidator.validatePhoneNumber("0912345678", kenya).isValid) // Invalid prefix
    }

    @Test
    fun testUgandaPhoneNumberValidation() {
        val uganda = Country.getEastAfricanCountries().first { it.code == "UG" }
        
        // Valid Uganda numbers
        assertTrue(PhoneNumberValidator.validatePhoneNumber("0712345678", uganda).isValid)
        assertTrue(PhoneNumberValidator.validatePhoneNumber("0312345678", uganda).isValid)
        
        // Invalid Uganda numbers
        assertFalse(PhoneNumberValidator.validatePhoneNumber("0912345678", uganda).isValid) // Invalid prefix
        assertFalse(PhoneNumberValidator.validatePhoneNumber("712345678", uganda).isValid) // Missing 0
    }

    @Test
    fun testPhoneNumberFormatting() {
        val kenya = Country.getEastAfricanCountries().first { it.code == "KE" }
        
        // Test formatting
        assertEquals("0712 345 678", PhoneNumberValidator.formatPhoneNumber("0712345678", kenya))
        assertEquals("0123 456 789", PhoneNumberValidator.formatPhoneNumber("0123456789", kenya))
        
        // Test partial numbers (should not format)
        assertEquals("071", PhoneNumberValidator.formatPhoneNumber("071", kenya))
        assertEquals("07123", PhoneNumberValidator.formatPhoneNumber("07123", kenya))
    }

    @Test
    fun testBurundiPhoneNumberValidation() {
        val burundi = Country.getEastAfricanCountries().first { it.code == "BI" }
        
        // Valid Burundi numbers (8 digits)
        assertTrue(PhoneNumberValidator.validatePhoneNumber("12345678", burundi).isValid)
        assertTrue(PhoneNumberValidator.validatePhoneNumber("87654321", burundi).isValid)
        
        // Invalid Burundi numbers
        assertFalse(PhoneNumberValidator.validatePhoneNumber("1234567", burundi).isValid) // Too short
        assertFalse(PhoneNumberValidator.validatePhoneNumber("123456789", burundi).isValid) // Too long
    }

    @Test
    fun testCountryDataIntegrity() {
        val countries = Country.getEastAfricanCountries()
        
        // Verify we have the expected countries
        assertTrue(countries.any { it.code == "KE" })
        assertTrue(countries.any { it.code == "UG" })
        assertTrue(countries.any { it.code == "TZ" })
        assertTrue(countries.any { it.code == "RW" })
        assertTrue(countries.any { it.code == "BI" })
        assertTrue(countries.any { it.code == "ET" })
        assertTrue(countries.any { it.code == "SS" })
        assertTrue(countries.any { it.code == "DJ" })
        assertTrue(countries.any { it.code == "ER" })
        assertTrue(countries.any { it.code == "SO" })
        
        // Verify all countries have required fields
        countries.forEach { country ->
            assertNotNull("Country name should not be null", country.name)
            assertNotNull("Country code should not be null", country.code)
            assertNotNull("Phone code should not be null", country.phoneCode)
            assertNotNull("Flag should not be null", country.flag)
            assertNotNull("Phone format should not be null", country.phoneFormat)
            assertTrue("Phone code should start with +", country.phoneCode.startsWith("+"))
        }
    }

    @Test
    fun testKenyaIsDefaultCountry() {
        val countries = Country.getEastAfricanCountries()
        val kenya = countries.first { it.code == "KE" }
        
        assertEquals("Kenya", kenya.name)
        assertEquals("KE", kenya.code)
        assertEquals("+254", kenya.phoneCode)
        assertEquals("🇰🇪", kenya.flag)
    }
}