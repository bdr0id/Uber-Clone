package co.ke.snilloc.uberclone

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.core.content.ContextCompat
import co.ke.snilloc.uberclone.data.onboarding.OnboardingManager

class StartedActivity : AppCompatActivity() {
    
    private lateinit var onboardingManager: OnboardingManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_started)

        //change system ui color to match current activity
        window.statusBarColor = ContextCompat.getColor(this, R.color.blue)

        onboardingManager = OnboardingManager(this)

        //start next activity when button clicked
        val button: Button = findViewById(R.id.StartedUberButton)
        button.setOnClickListener {
            // Mark that user has seen the get started screen
            onboardingManager.markGetStartedSeen()
            
            val intent = Intent(this, MobileActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}