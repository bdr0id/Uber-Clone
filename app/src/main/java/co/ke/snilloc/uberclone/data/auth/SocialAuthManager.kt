package co.ke.snilloc.uberclone.data.auth

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import co.ke.snilloc.uberclone.R
import co.ke.snilloc.uberclone.data.model.User
import co.ke.snilloc.uberclone.data.repository.UserRepository
import co.ke.snilloc.uberclone.data.repository.UserRepositoryImpl
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SocialAuthManager(
    private val activity: AppCompatActivity,
    private val userRepository: UserRepository = UserRepositoryImpl()
) {
    private val auth = FirebaseAuth.getInstance()
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var callbackManager: CallbackManager
    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>
    
    private var onAuthSuccess: ((User) -> Unit)? = null
    private var onAuthError: ((String) -> Unit)? = null
    
    init {
        setupGoogleSignIn()
        setupFacebookSignIn()
        setupActivityResultLauncher()
    }
    
    private fun setupGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(activity.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        
        googleSignInClient = GoogleSignIn.getClient(activity, gso)
    }
    
    private fun setupFacebookSignIn() {
        callbackManager = CallbackManager.Factory.create()
        
        LoginManager.getInstance().registerCallback(callbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(loginResult: LoginResult) {
                    handleFacebookAccessToken(loginResult.accessToken)
                }
                
                override fun onCancel() {
                    onAuthError?.invoke("Facebook login was cancelled")
                }
                
                override fun onError(exception: FacebookException) {
                    onAuthError?.invoke("Facebook login failed: ${exception.message}")
                }
            })
    }
    
    private fun setupActivityResultLauncher() {
        googleSignInLauncher = activity.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val account = task.getResult(ApiException::class.java)
                    handleGoogleSignInResult(account)
                } catch (e: ApiException) {
                    onAuthError?.invoke("Google sign in failed: ${e.message}")
                }
            } else {
                onAuthError?.invoke("Google sign in was cancelled")
            }
        }
    }
    
    fun signInWithGoogle(
        onSuccess: (User) -> Unit,
        onError: (String) -> Unit
    ) {
        this.onAuthSuccess = onSuccess
        this.onAuthError = onError
        
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }
    
    fun signInWithFacebook(
        onSuccess: (User) -> Unit,
        onError: (String) -> Unit
    ) {
        this.onAuthSuccess = onSuccess
        this.onAuthError = onError
        
        LoginManager.getInstance().logInWithReadPermissions(
            activity,
            listOf("email", "public_profile")
        )
    }
    
    private fun handleGoogleSignInResult(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        
        auth.signInWithCredential(credential)
            .addOnCompleteListener(activity) { task ->
                if (task.isSuccessful) {
                    val firebaseUser = auth.currentUser
                    if (firebaseUser != null) {
                        val user = User(
                            id = firebaseUser.uid,
                            name = firebaseUser.displayName ?: "",
                            email = firebaseUser.email ?: "",
                            phoneNumber = firebaseUser.phoneNumber,
                            profileImageUrl = firebaseUser.photoUrl?.toString()
                        )
                        
                        // Create user profile in Firestore
                        createUserProfile(user)
                    } else {
                        onAuthError?.invoke("Failed to get user information")
                    }
                } else {
                    onAuthError?.invoke("Authentication failed: ${task.exception?.message}")
                }
            }
    }
    
    private fun handleFacebookAccessToken(token: AccessToken) {
        val credential = FacebookAuthProvider.getCredential(token.token)
        
        auth.signInWithCredential(credential)
            .addOnCompleteListener(activity) { task ->
                if (task.isSuccessful) {
                    val firebaseUser = auth.currentUser
                    if (firebaseUser != null) {
                        val user = User(
                            id = firebaseUser.uid,
                            name = firebaseUser.displayName ?: "",
                            email = firebaseUser.email ?: "",
                            phoneNumber = firebaseUser.phoneNumber,
                            profileImageUrl = firebaseUser.photoUrl?.toString()
                        )
                        
                        // Create user profile in Firestore
                        createUserProfile(user)
                    } else {
                        onAuthError?.invoke("Failed to get user information")
                    }
                } else {
                    onAuthError?.invoke("Authentication failed: ${task.exception?.message}")
                }
            }
    }
    
    private fun createUserProfile(user: User) {
        // Use coroutine to handle async operation
        activity.lifecycleScope.launch {
            try {
                val result = userRepository.createUser(user)
                if (result.isSuccess) {
                    onAuthSuccess?.invoke(user)
                } else {
                    onAuthError?.invoke("Failed to create user profile: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                onAuthError?.invoke("Failed to create user profile: ${e.message}")
            }
        }
    }
    
    fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }
    
    fun signOut() {
        auth.signOut()
        googleSignInClient.signOut()
        LoginManager.getInstance().logOut()
    }
}