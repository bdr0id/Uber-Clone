package co.ke.snilloc.uberclone.data.location

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

class LocationPermissionHelper(
    private val context: Context
) {
    companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 1001
        
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }

    fun hasLocationPermission(): Boolean {
        return REQUIRED_PERMISSIONS.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun shouldShowRationale(activity: Activity): Boolean {
        return REQUIRED_PERMISSIONS.any { permission ->
            activity.shouldShowRequestPermissionRationale(permission)
        }
    }

    fun shouldShowRationale(fragment: Fragment): Boolean {
        return REQUIRED_PERMISSIONS.any { permission ->
            fragment.shouldShowRequestPermissionRationale(permission)
        }
    }

    fun requestPermissions(activity: Activity) {
        activity.requestPermissions(REQUIRED_PERMISSIONS, LOCATION_PERMISSION_REQUEST_CODE)
    }

    fun requestPermissions(fragment: Fragment) {
        fragment.requestPermissions(REQUIRED_PERMISSIONS, LOCATION_PERMISSION_REQUEST_CODE)
    }

    fun handlePermissionResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
        onPermissionGranted: () -> Unit,
        onPermissionDenied: () -> Unit,
        onPermissionPermanentlyDenied: () -> Unit
    ) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                onPermissionGranted()
            } else {
                val activity = context as? Activity
                if (activity != null && shouldShowRationale(activity)) {
                    onPermissionDenied()
                } else {
                    onPermissionPermanentlyDenied()
                }
            }
        }
    }

    fun showPermissionRationaleDialog(
        activity: Activity,
        onPositive: () -> Unit,
        onNegative: () -> Unit = {}
    ) {
        AlertDialog.Builder(activity)
            .setTitle("Location Permission Required")
            .setMessage("This app needs location permission to show your current location and help you book rides. Please grant location permission to continue.")
            .setPositiveButton("Grant Permission") { _, _ -> onPositive() }
            .setNegativeButton("Cancel") { _, _ -> onNegative() }
            .setCancelable(false)
            .show()
    }

    fun showPermissionDeniedDialog(
        activity: Activity,
        onPositive: () -> Unit = {},
        onNegative: () -> Unit = {}
    ) {
        AlertDialog.Builder(activity)
            .setTitle("Location Permission Denied")
            .setMessage("Location permission is required for this app to function properly. You can enable it in the app settings.")
            .setPositiveButton("Open Settings") { _, _ -> 
                openAppSettings(activity)
                onPositive()
            }
            .setNegativeButton("Cancel") { _, _ -> onNegative() }
            .show()
    }

    private fun openAppSettings(activity: Activity) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", activity.packageName, null)
        }
        activity.startActivity(intent)
    }

    // For use with ActivityResultLauncher (modern approach)
    fun createPermissionLauncher(
        activity: Activity,
        onPermissionGranted: () -> Unit,
        onPermissionDenied: () -> Unit
    ): ActivityResultLauncher<Array<String>> {
        return (activity as androidx.activity.ComponentActivity).registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val allGranted = permissions.values.all { it }
            if (allGranted) {
                onPermissionGranted()
            } else {
                onPermissionDenied()
            }
        }
    }

    fun createPermissionLauncher(
        fragment: Fragment,
        onPermissionGranted: () -> Unit,
        onPermissionDenied: () -> Unit
    ): ActivityResultLauncher<Array<String>> {
        return fragment.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val allGranted = permissions.values.all { it }
            if (allGranted) {
                onPermissionGranted()
            } else {
                onPermissionDenied()
            }
        }
    }
}