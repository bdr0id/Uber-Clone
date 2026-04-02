package co.ke.snilloc.uberclone.ui.base

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestoreException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Centralized error handling utility for the application
 */
object ErrorHandler {
    
    /**
     * Convert exceptions to user-friendly error messages
     */
    fun getErrorMessage(throwable: Throwable): String {
        return when (throwable) {
            // Network related errors
            is UnknownHostException -> "No internet connection. Please check your network."
            is SocketTimeoutException -> "Connection timeout. Please try again."
            is IOException -> "Network error. Please check your connection."
            
            // Firebase specific errors
            is FirebaseNetworkException -> "Network error. Please check your internet connection."
            is FirebaseTooManyRequestsException -> "Too many requests. Please try again later."
            is FirebaseAuthException -> handleAuthError(throwable)
            is FirebaseFirestoreException -> handleFirestoreError(throwable)
            is FirebaseException -> "Service temporarily unavailable. Please try again."
            
            // Location related errors
            is SecurityException -> when {
                throwable.message?.contains("permission", ignoreCase = true) == true -> 
                    "Location permission is required for this feature."
                else -> "Security error occurred. Please try again."
            }
            
            // General errors
            is IllegalStateException -> "App is in an invalid state. Please restart the app."
            is IllegalArgumentException -> "Invalid input provided. Please check your data."
            
            else -> throwable.message ?: "An unexpected error occurred. Please try again."
        }
    }
    
    private fun handleAuthError(exception: FirebaseAuthException): String {
        return when (exception.errorCode) {
            "ERROR_NETWORK_REQUEST_FAILED" -> "Network error. Please check your connection."
            "ERROR_USER_NOT_FOUND" -> "User account not found."
            "ERROR_INVALID_USER_TOKEN" -> "Session expired. Please sign in again."
            "ERROR_USER_DISABLED" -> "Your account has been disabled."
            "ERROR_TOO_MANY_REQUESTS" -> "Too many requests. Please try again later."
            else -> "Authentication error: ${exception.message}"
        }
    }
    
    private fun handleFirestoreError(exception: FirebaseFirestoreException): String {
        return when (exception.code) {
            FirebaseFirestoreException.Code.UNAVAILABLE -> 
                "Service temporarily unavailable. Please try again."
            FirebaseFirestoreException.Code.DEADLINE_EXCEEDED -> 
                "Request timeout. Please try again."
            FirebaseFirestoreException.Code.PERMISSION_DENIED -> 
                "Access denied. Please check your permissions."
            FirebaseFirestoreException.Code.NOT_FOUND -> 
                "Requested data not found."
            FirebaseFirestoreException.Code.ALREADY_EXISTS -> 
                "Data already exists."
            FirebaseFirestoreException.Code.RESOURCE_EXHAUSTED -> 
                "Service quota exceeded. Please try again later."
            else -> "Database error: ${exception.message}"
        }
    }
    
    /**
     * Check if the error is recoverable (can be retried)
     */
    fun isRecoverableError(throwable: Throwable): Boolean {
        return when (throwable) {
            is UnknownHostException,
            is SocketTimeoutException,
            is IOException,
            is FirebaseNetworkException,
            is FirebaseTooManyRequestsException -> true
            
            is FirebaseFirestoreException -> when (throwable.code) {
                FirebaseFirestoreException.Code.UNAVAILABLE,
                FirebaseFirestoreException.Code.DEADLINE_EXCEEDED,
                FirebaseFirestoreException.Code.RESOURCE_EXHAUSTED -> true
                else -> false
            }
            
            else -> false
        }
    }
    
    /**
     * Check if device has internet connectivity
     */
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
               capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
               capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
    }
}