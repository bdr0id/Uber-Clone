package co.ke.snilloc.uberclone

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import co.ke.snilloc.uberclone.data.model.Ride
import co.ke.snilloc.uberclone.data.model.RideStatus
import co.ke.snilloc.uberclone.ui.viewmodel.TripCompletionViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RideDetailActivity : AppCompatActivity() {

    private val viewModel: TripCompletionViewModel by viewModels()
    
    // UI Components
    private lateinit var backButton: ImageView
    private lateinit var titleText: TextView
    private lateinit var loadingProgressBar: ProgressBar
    private lateinit var contentView: View
    
    // Trip Details
    private lateinit var pickupAddressText: TextView
    private lateinit var destinationAddressText: TextView
    private lateinit var tripDateText: TextView
    private lateinit var tripTimeText: TextView
    private lateinit var tripDurationText: TextView
    private lateinit var fareAmountText: TextView
    private lateinit var statusText: TextView
    private lateinit var ratingText: TextView
    private lateinit var feedbackText: TextView
    
    // Driver Details
    private lateinit var driverSection: View
    private lateinit var driverNameText: TextView
    private lateinit var vehicleInfoText: TextView
    private lateinit var driverRatingText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ride_detail)
        
        initializeViews()
        setupClickListeners()
        observeViewModel()
        
        // Get ride ID from intent
        val rideId = intent.getStringExtra("RIDE_ID")
        if (rideId != null) {
            viewModel.loadRideDetails(rideId)
        } else {
            Toast.makeText(this, "Error loading ride details", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun initializeViews() {
        backButton = findViewById(R.id.backButton)
        titleText = findViewById(R.id.titleText)
        loadingProgressBar = findViewById(R.id.loadingProgressBar)
        contentView = findViewById(R.id.contentView)
        
        pickupAddressText = findViewById(R.id.pickupAddressText)
        destinationAddressText = findViewById(R.id.destinationAddressText)
        tripDateText = findViewById(R.id.tripDateText)
        tripTimeText = findViewById(R.id.tripTimeText)
        tripDurationText = findViewById(R.id.tripDurationText)
        fareAmountText = findViewById(R.id.fareAmountText)
        statusText = findViewById(R.id.statusText)
        ratingText = findViewById(R.id.ratingText)
        feedbackText = findViewById(R.id.feedbackText)
        
        driverSection = findViewById(R.id.driverSection)
        driverNameText = findViewById(R.id.driverNameText)
        vehicleInfoText = findViewById(R.id.vehicleInfoText)
        driverRatingText = findViewById(R.id.driverRatingText)
    }

    private fun setupClickListeners() {
        backButton.setOnClickListener {
            finish()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.ride.collect { ride ->
                ride?.let { 
                    updateRideDetails(it)
                    if (it.driverId != null) {
                        viewModel.loadDriverInfo(it.driverId)
                    }
                }
            }
        }
        
        lifecycleScope.launch {
            viewModel.driver.collect { driver ->
                driver?.let { updateDriverInfo(it.name, it.vehicleInfo, it.rating) }
            }
        }
        
        lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                loadingProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
                contentView.visibility = if (isLoading) View.GONE else View.VISIBLE
            }
        }
        
        lifecycleScope.launch {
            viewModel.error.collect { error ->
                error?.let {
                    Toast.makeText(this@RideDetailActivity, it, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun updateRideDetails(ride: Ride) {
        pickupAddressText.text = ride.pickupLocation.address
        destinationAddressText.text = ride.destinationLocation.address
        
        // Format date and time
        val date = Date(ride.requestTime)
        val dateFormat = SimpleDateFormat("EEEE, MMM dd, yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        
        tripDateText.text = dateFormat.format(date)
        tripTimeText.text = timeFormat.format(date)
        
        // Calculate and display trip duration
        val duration = if (ride.startTime != null && ride.endTime != null) {
            val durationMinutes = (ride.endTime - ride.startTime) / (1000 * 60)
            "${durationMinutes} minutes"
        } else {
            "N/A"
        }
        tripDurationText.text = duration
        
        // Display fare
        val fare = ride.actualPrice ?: ride.estimatedPrice
        fareAmountText.text = "KSh ${String.format("%.2f", fare)}"
        
        // Display status
        statusText.text = when (ride.status) {
            RideStatus.COMPLETED -> "Completed"
            RideStatus.CANCELLED -> "Cancelled"
            RideStatus.IN_PROGRESS -> "In Progress"
            RideStatus.DRIVER_ASSIGNED -> "Driver Assigned"
            RideStatus.DRIVER_ARRIVING -> "Driver Arriving"
            RideStatus.REQUESTED -> "Requested"
        }
        
        // Set status color
        val statusColor = when (ride.status) {
            RideStatus.COMPLETED -> android.R.color.holo_green_dark
            RideStatus.CANCELLED -> android.R.color.holo_red_dark
            else -> android.R.color.holo_orange_dark
        }
        statusText.setTextColor(getColor(statusColor))
        
        // Display rating and feedback if available
        if (ride.rating != null && ride.rating > 0) {
            ratingText.text = "Your Rating: ${ride.rating}⭐"
            ratingText.visibility = View.VISIBLE
        } else {
            ratingText.visibility = View.GONE
        }
        
        if (!ride.feedback.isNullOrBlank()) {
            feedbackText.text = "\"${ride.feedback}\""
            feedbackText.visibility = View.VISIBLE
        } else {
            feedbackText.visibility = View.GONE
        }
        
        // Show driver section only if driver was assigned
        driverSection.visibility = if (ride.driverId != null) View.VISIBLE else View.GONE
    }

    private fun updateDriverInfo(name: String, vehicleInfo: String, rating: Double) {
        driverNameText.text = name
        vehicleInfoText.text = vehicleInfo
        driverRatingText.text = "${rating}⭐"
    }
}