package co.ke.snilloc.uberclone

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import co.ke.snilloc.uberclone.data.onboarding.OnboardingManager

class MainActivity : AppCompatActivity() {
    
    private lateinit var onboardingManager: OnboardingManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //hide the status bar
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        onboardingManager = OnboardingManager(this)

        // Show splash screen and then determine navigation
        showSplashScreen()
    }

    private fun showSplashScreen() {
        Handler(Looper.getMainLooper()).postDelayed({
            determineNavigationFlow()
        }, 3500) //delayed for 3.5 seconds
    }
    
    private fun determineNavigationFlow() {
        val currentUser = Firebase.auth.currentUser
        
        when {
            // User is signed in and has completed onboarding
            currentUser != null && onboardingManager.hasCompletedOnboarding() -> {
                navigateToMapActivity()
            }
            // User is signed in but hasn't completed onboarding
            currentUser != null && !onboardingManager.hasCompletedOnboarding() -> {
                navigateToAppropriateOnboardingStep()
            }
            // User is not signed in and hasn't seen get started
            !onboardingManager.hasSeenGetStarted() -> {
                navigateToStartedActivity()
            }
            // User is not signed in but has seen get started
            else -> {
                navigateToMobileActivity()
            }
        }
    }
    
    private fun navigateToAppropriateOnboardingStep() {
        when {
            !onboardingManager.isPhoneVerified() -> navigateToMobileActivity()
            !onboardingManager.hasCompletedProfile() -> navigateToProfileActivity()
            else -> navigateToMapActivity()
        }
    }

    private fun navigateToStartedActivity() {
        val intent = Intent(this, StartedActivity::class.java)
        startActivity(intent)
        finish()
    }
    
    private fun navigateToMobileActivity() {
        val intent = Intent(this, MobileActivity::class.java)
        startActivity(intent)
        finish()
    }
    
    private fun navigateToProfileActivity() {
        val intent = Intent(this, ProfileActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToMapActivity() {
        val intent = Intent(this, MapActivity::class.java)
        startActivity(intent)
        finish()
    }
}