package co.ke.snilloc.uberclone

import android.content.Intent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SocialActivity : AppCompatActivity() {
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth

    // Register for activity result
    private val signInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            account?.let { firebaseAuthWithGoogle(it.idToken!!) }
        } catch (e: ApiException) {
            when (e.statusCode) {
                7 -> showError("Network error. Please check your internet connection.")
                12500 -> showError("Google Sign-In configuration error. Please contact support.")
                else -> showError("Google sign in failed: ${e.message}")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_social)

        // Set status bar color
        window.statusBarColor = ContextCompat.getColor(this, R.color.blue)

        // Initialize Firebase Auth
        auth = Firebase.auth

        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Check if user is already signed in
        val currentUser = auth.currentUser
        if (currentUser != null) {
            navigateToMapActivity()
            return
        }

        // Set up Google Sign In click listener
        findViewById<ImageView>(R.id.SocialGoogleImageView).setOnClickListener {
            signIn()
        }
        findViewById<TextView>(R.id.SocialGoogleTextView).setOnClickListener {
            signIn()
        }
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        signInLauncher.launch(signInIntent)
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Check if this is a new user
                    val isNewUser = task.result.additionalUserInfo?.isNewUser ?: false
                    if (isNewUser) {
                        // Handle new user sign up
                        val user = auth.currentUser
                        user?.let {
                            // You can store additional user data here if needed
                            showSuccess("Welcome! Your account has been created.")
                        }
                    } else {
                        // Handle existing user sign in
                        showSuccess("Welcome back!")
                    }
                    navigateToMapActivity()
                } else {
                    showError("Authentication failed: ${task.exception?.message}")
                }
            }
    }

    private fun navigateToMapActivity() {
        startActivity(Intent(this, MapActivity::class.java))
        finish()
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun showSuccess(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}