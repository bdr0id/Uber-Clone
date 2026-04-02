package co.ke.snilloc.uberclone

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import co.ke.snilloc.uberclone.data.onboarding.OnboardingManager

class MobileVerifyActivity : AppCompatActivity() {
    
    private lateinit var onboardingManager: OnboardingManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mobile_verify)

        onboardingManager = OnboardingManager(this)

        // Get phone number and country info from intent
        val phoneNumber = intent.getStringExtra("phone_number") ?: ""
        val countryCode = intent.getStringExtra("country_code") ?: ""
        val countryName = intent.getStringExtra("country_name") ?: ""

        val button: Button = findViewById(R.id.VerifyNextButton)
        val userNumber: EditText = findViewById(R.id.VerifyEditTextNumber)
        val codeError: TextView = findViewById(R.id.VerifyCodeErrorTextView)
        val skipVerify: TextView = findViewById(R.id.VerifySkipTextView)
        
        // Display the phone number and country code from intent
        val countryCodeTextView = findViewById<TextView>(R.id.VerifyCountryCodeTextView)
        val phoneDisplayTextView = findViewById<TextView>(R.id.VerifyMobileNumber)
        
        if (phoneNumber.isNotEmpty()) {
            // Display the full phone number without prefix removal for clarity
            phoneDisplayTextView.text = phoneNumber
        }
        
        if (countryCode.isNotEmpty()) {
            countryCodeTextView.text = countryCode
        }

        skipVerify.setOnClickListener {
            val intent = Intent(this, PasswordActivity::class.java)
            startActivity(intent)
        }

        button.setOnClickListener {
            val enteredCode = userNumber.text.toString().trim()
            
            if (enteredCode.isEmpty()) {
                showError("Please enter the 4-digit verification code", userNumber, codeError)
                return@setOnClickListener
            }
            
            if (enteredCode.length != 4) {
                showError("Verification code must be exactly 4 digits", userNumber, codeError)
                return@setOnClickListener
            }
            
            // Development mode: Accept "0000" as valid verification code
            if (enteredCode == "0000") {
                handleSuccessfulVerification(phoneNumber, countryCode)
            } else {
                // In development mode, only "0000" is accepted
                showError("Invalid verification code. For testing, please use '0000'", userNumber, codeError)
            }
        }
    }
    
    /**
     * Display error message and focus on input field
     */
    private fun showError(message: String, editText: EditText, errorTextView: TextView) {
        errorTextView.text = message
        errorTextView.visibility = View.VISIBLE
        editText.requestFocus()
    }
    
    /**
     * Handle successful verification and navigate to MapActivity
     */
    private fun handleSuccessfulVerification(phoneNumber: String, countryCode: String) {
        // Mark phone as verified in onboarding manager
        onboardingManager.markPhoneVerified()
        
        // Store the verified phone number and country
        onboardingManager.storePhoneNumber(phoneNumber, countryCode)
        
        // Always navigate to MapActivity after verification
        val intent = Intent(this, MapActivity::class.java)
        startActivity(intent)
        finish()
    }
}