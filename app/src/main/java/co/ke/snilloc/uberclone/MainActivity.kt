package co.ke.snilloc.uberclone

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //hide the status bar
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        // Check if user is already signed in
        val currentUser = Firebase.auth.currentUser
        if (currentUser != null) {
            // User is signed in, go directly to MapActivity
            navigateToMapActivity()
        } else {
            // User is not signed in, show splash screen and go to StartedActivity
            showSplashScreen()
        }
    }

    private fun showSplashScreen() {
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, StartedActivity::class.java)
            startActivity(intent)
            finish()
        }, 3500) //delayed for 3.5 seconds
    }

    private fun navigateToMapActivity() {
        val intent = Intent(this, MapActivity::class.java)
        startActivity(intent)
        finish()
    }
}