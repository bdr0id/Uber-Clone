package co.ke.snilloc.uberclone.utils

import co.ke.snilloc.uberclone.data.model.Country

object PhoneNumberValidator {
    
    fun validatePhoneNumber(phoneNumber: String, country: Country): ValidationResult {
        val cleanNumber = phoneNumber.replace("\\s".toRegex(), "")
        
        return when (country.code) {
            "KE" -> validateKenyaNumber(cleanNumber)
            "UG" -> validateUgandaNumber(cleanNumber)
            "TZ" -> validateTanzaniaNumber(cleanNumber)
            "RW" -> validateRwandaNumber(cleanNumber)
            "BI" -> validateBurundiNumber(cleanNumber)
            "ET" -> validateEthiopiaNumber(cleanNumber)
            "SS" -> validateSouthSudanNumber(cleanNumber)
            "DJ" -> validateDjiboutiNumber(cleanNumber)
            "ER" -> validateEritreaNumber(cleanNumber)
            "SO" -> validateSomaliaNumber(cleanNumber)
            else -> ValidationResult(false, "Unsupported country")
        }
    }
    
    private fun validateKenyaNumber(number: String): ValidationResult {
        // Kenya: 0XXX XXX XXX (10 digits starting with 0)
        val regex = "^0[17][0-9]{8}$".toRegex()
        return if (regex.matches(number)) {
            ValidationResult(true, "")
        } else {
            ValidationResult(false, "Please enter a valid Kenyan number (0XXX XXX XXX)")
        }
    }
    
    private fun validateUgandaNumber(number: String): ValidationResult {
        // Uganda: 0XXX XXX XXX (10 digits starting with 0)
        val regex = "^0[37][0-9]{8}$".toRegex()
        return if (regex.matches(number)) {
            ValidationResult(true, "")
        } else {
            ValidationResult(false, "Please enter a valid Ugandan number (0XXX XXX XXX)")
        }
    }
    
    private fun validateTanzaniaNumber(number: String): ValidationResult {
        // Tanzania: 0XXX XXX XXX (10 digits starting with 0)
        val regex = "^0[67][0-9]{8}$".toRegex()
        return if (regex.matches(number)) {
            ValidationResult(true, "")
        } else {
            ValidationResult(false, "Please enter a valid Tanzanian number (0XXX XXX XXX)")
        }
    }
    
    private fun validateRwandaNumber(number: String): ValidationResult {
        // Rwanda: 0XXX XXX XXX (10 digits starting with 0)
        val regex = "^0[78][0-9]{8}$".toRegex()
        return if (regex.matches(number)) {
            ValidationResult(true, "")
        } else {
            ValidationResult(false, "Please enter a valid Rwandan number (0XXX XXX XXX)")
        }
    }
    
    private fun validateBurundiNumber(number: String): ValidationResult {
        // Burundi: XX XXX XXX (8 digits)
        val regex = "^[0-9]{8}$".toRegex()
        return if (regex.matches(number)) {
            ValidationResult(true, "")
        } else {
            ValidationResult(false, "Please enter a valid Burundian number (XX XXX XXX)")
        }
    }
    
    private fun validateEthiopiaNumber(number: String): ValidationResult {
        // Ethiopia: 0XX XXX XXXX (10 digits starting with 0)
        val regex = "^0[9][0-9]{8}$".toRegex()
        return if (regex.matches(number)) {
            ValidationResult(true, "")
        } else {
            ValidationResult(false, "Please enter a valid Ethiopian number (0XX XXX XXXX)")
        }
    }
    
    private fun validateSouthSudanNumber(number: String): ValidationResult {
        // South Sudan: 0XX XXX XXX (9 digits starting with 0)
        val regex = "^0[9][0-9]{7}$".toRegex()
        return if (regex.matches(number)) {
            ValidationResult(true, "")
        } else {
            ValidationResult(false, "Please enter a valid South Sudanese number (0XX XXX XXX)")
        }
    }
    
    private fun validateDjiboutiNumber(number: String): ValidationResult {
        // Djibouti: XX XX XX XX (8 digits)
        val regex = "^[0-9]{8}$".toRegex()
        return if (regex.matches(number)) {
            ValidationResult(true, "")
        } else {
            ValidationResult(false, "Please enter a valid Djiboutian number (XX XX XX XX)")
        }
    }
    
    private fun validateEritreaNumber(number: String): ValidationResult {
        // Eritrea: X XXX XXX (7 digits)
        val regex = "^[0-9]{7}$".toRegex()
        return if (regex.matches(number)) {
            ValidationResult(true, "")
        } else {
            ValidationResult(false, "Please enter a valid Eritrean number (X XXX XXX)")
        }
    }
    
    private fun validateSomaliaNumber(number: String): ValidationResult {
        // Somalia: XX XXX XXX (8 digits)
        val regex = "^[0-9]{8}$".toRegex()
        return if (regex.matches(number)) {
            ValidationResult(true, "")
        } else {
            ValidationResult(false, "Please enter a valid Somali number (XX XXX XXX)")
        }
    }
    
    fun formatPhoneNumber(phoneNumber: String, country: Country): String {
        val cleanNumber = phoneNumber.replace("\\s".toRegex(), "")
        
        return when (country.code) {
            "KE", "UG", "TZ", "RW" -> {
                // Format: 0XXX XXX XXX
                if (cleanNumber.length >= 10) {
                    "${cleanNumber.substring(0, 4)} ${cleanNumber.substring(4, 7)} ${cleanNumber.substring(7)}"
                } else {
                    cleanNumber
                }
            }
            "BI", "DJ", "SO" -> {
                // Format: XX XXX XXX
                if (cleanNumber.length >= 8) {
                    "${cleanNumber.substring(0, 2)} ${cleanNumber.substring(2, 5)} ${cleanNumber.substring(5)}"
                } else {
                    cleanNumber
                }
            }
            "ET" -> {
                // Format: 0XX XXX XXXX
                if (cleanNumber.length >= 10) {
                    "${cleanNumber.substring(0, 3)} ${cleanNumber.substring(3, 6)} ${cleanNumber.substring(6)}"
                } else {
                    cleanNumber
                }
            }
            "SS" -> {
                // Format: 0XX XXX XXX
                if (cleanNumber.length >= 9) {
                    "${cleanNumber.substring(0, 3)} ${cleanNumber.substring(3, 6)} ${cleanNumber.substring(6)}"
                } else {
                    cleanNumber
                }
            }
            "ER" -> {
                // Format: X XXX XXX
                if (cleanNumber.length >= 7) {
                    "${cleanNumber.substring(0, 1)} ${cleanNumber.substring(1, 4)} ${cleanNumber.substring(4)}"
                } else {
                    cleanNumber
                }
            }
            else -> cleanNumber
        }
    }
}

data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String
)