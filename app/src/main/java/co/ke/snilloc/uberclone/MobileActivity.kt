package co.ke.snilloc.uberclone

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import co.ke.snilloc.uberclone.data.model.Country
import co.ke.snilloc.uberclone.data.onboarding.OnboardingManager
import co.ke.snilloc.uberclone.ui.adapter.CountrySpinnerAdapter
import co.ke.snilloc.uberclone.utils.PhoneNumberValidator

class MobileActivity : AppCompatActivity() {
    
    private lateinit var onboardingManager: OnboardingManager
    private lateinit var countrySpinner: Spinner
    private lateinit var countryCodeTextView: TextView
    private lateinit var phoneNumberEditText: EditText
    private lateinit var errorTextView: TextView
    private lateinit var nextButton: Button
    
    private val countries = Country.getEastAfricanCountries()
    private var selectedCountry = countries.first { it.code == "KE" } // Default to Kenya
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mobile)

        onboardingManager = OnboardingManager(this)
        
        initializeViews()
        setupCountrySpinner()
        setupPhoneNumberInput()
        setupNextButton()
        setupSocialMediaLink()
    }
    
    private fun initializeViews() {
        countrySpinner = findViewById(R.id.MobileSpinner)
        countryCodeTextView = findViewById(R.id.MobileCountryCodeTextView)
        phoneNumberEditText = findViewById(R.id.MobileNumberEditText)
        errorTextView = findViewById(R.id.MobileErrorTextView)
        nextButton = findViewById(R.id.MobileNextButton)
    }
    
    private fun setupCountrySpinner() {
        val adapter = CountrySpinnerAdapter(this, countries)
        countrySpinner.adapter = adapter
        
        // Set Kenya as default selection
        val kenyaIndex = countries.indexOfFirst { it.code == "KE" }
        countrySpinner.setSelection(kenyaIndex)
        updateCountryCode(selectedCountry)
        
        countrySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedCountry = countries[position]
                updateCountryCode(selectedCountry)
                updatePhoneNumberHint(selectedCountry)
                validateCurrentInput()
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
    }
    
    private fun updateCountryCode(country: Country) {
        countryCodeTextView.text = country.phoneCode
    }
    
    private fun updatePhoneNumberHint(country: Country) {
        phoneNumberEditText.hint = country.phoneFormat.replace("X", "7")
    }
    
    private fun setupPhoneNumberInput() {
        updatePhoneNumberHint(selectedCountry)
        
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            
            override fun afterTextChanged(s: Editable?) {
                validateCurrentInput()
            }
        }
        
        phoneNumberEditText.addTextChangedListener(textWatcher)
        phoneNumberEditText.tag = textWatcher
    }
    
    private fun validateCurrentInput() {
        val phoneNumber = phoneNumberEditText.text.toString().trim()
        
        if (phoneNumber.isEmpty()) {
            hideError()
            return
        }
        
        val validationResult = PhoneNumberValidator.validatePhoneNumber(phoneNumber, selectedCountry)
        
        if (validationResult.isValid) {
            hideError()
            // Format the phone number as user types
            val formattedNumber = PhoneNumberValidator.formatPhoneNumber(phoneNumber, selectedCountry)
            if (formattedNumber != phoneNumber) {
                phoneNumberEditText.removeTextChangedListener(phoneNumberEditText.tag as? TextWatcher)
                phoneNumberEditText.setText(formattedNumber)
                phoneNumberEditText.setSelection(formattedNumber.length)
                setupPhoneNumberInput() // Re-add the text watcher
            }
        } else {
            showError(validationResult.errorMessage)
        }
    }
    
    private fun setupNextButton() {
        nextButton.setOnClickListener {
            val phoneNumber = phoneNumberEditText.text.toString().trim()
            
            if (phoneNumber.isEmpty()) {
                phoneNumberEditText.requestFocus()
                showError("Please enter your mobile number")
                return@setOnClickListener
            }
            
            val validationResult = PhoneNumberValidator.validatePhoneNumber(phoneNumber, selectedCountry)
            
            if (validationResult.isValid) {
                // Store the selected country and phone number for verification
                val fullPhoneNumber = "${selectedCountry.phoneCode}${phoneNumber.removePrefix("0")}"
                
                val intent = Intent(this, MobileVerifyActivity::class.java).apply {
                    putExtra("phone_number", fullPhoneNumber)
                    putExtra("country_code", selectedCountry.code)
                    putExtra("country_name", selectedCountry.name)
                }
                startActivity(intent)
            } else {
                showError(validationResult.errorMessage)
                phoneNumberEditText.requestFocus()
            }
        }
    }
    
    private fun setupSocialMediaLink() {
        val socialTextView: TextView = findViewById(R.id.MobileSocialTextView)
        socialTextView.setTextColor(Color.parseColor("#2d71e2"))
        socialTextView.setOnClickListener {
            val intent = Intent(this, SocialActivity::class.java)
            startActivity(intent)
        }
    }
    
    private fun showError(message: String) {
        errorTextView.text = message
        errorTextView.visibility = View.VISIBLE
    }
    
    private fun hideError() {
        errorTextView.visibility = View.GONE
    }
}