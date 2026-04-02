package co.ke.snilloc.uberclone

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import co.ke.snilloc.uberclone.data.model.Ride
import co.ke.snilloc.uberclone.ui.viewmodel.TripCompletionViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TripCompletionActivity : AppCompatActivity() {

    private val viewModel: TripCompletionViewModel by viewModels()
    
    // UI Components
    private lateinit var tripSummaryCard: androidx.cardview.widget.CardView
    private lateinit var pickupAddressText: TextView
    private lateinit var destinationAddressText: TextView
    private lateinit var tripDateText: TextView
    private lateinit var tripDurationText: TextView
    private lateinit var fareAmountText: TextView
    private lateinit var driverNameText: TextView
    private lateinit var vehicleInfoText: TextView
    
    private lateinit var ratingCard: androidx.cardview.widget.CardView
    private lateinit var ratingBar: RatingBar
    private lateinit var feedbackInput: EditText
    private lateinit var submitRatingButton: Button
    private lateinit var skipRatingButton: Button
    
    private lateinit var actionButtonsCard: androidx.cardview.widget.CardView
    private lateinit var viewHistoryButton: Button
    private lateinit var bookAnotherRideButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trip_completion)
        
        initializeViews()
        setupClickListeners()
        observeViewModel()
        
        // Get ride ID from intent
        val rideId = intent.getStringExtra("RIDE_ID")
        if (rideId != null) {
            viewModel.loadRideDetails(rideId)
        } else {
            Toast.makeText(this, "Error loading trip details", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun initializeViews() {
        tripSummaryCard = findViewById(R.id.tripSummaryCard)
        pickupAddressText = findViewById(R.id.pickupAddressText)
        destinationAddressText = findViewById(R.id.destinationAddressText)
        tripDateText = findViewById(R.id.tripDateText)
        tripDurationText = findViewById(R.id.tripDurationText)
        fareAmountText = findViewById(R.id.fareAmountText)
        driverNameText = findViewById(R.id.driverNameText)
        vehicleInfoText = findViewById(R.id.vehicleInfoText)
        
        ratingCard = findViewById(R.id.ratingCard)
        ratingBar = findViewById(R.id.ratingBar)
        feedbackInput = findViewById(R.id.feedbackInput)
        submitRatingButton = findViewById(R.id.submitRatingButton)
        skipRatingButton = findViewById(R.id.skipRatingButton)
        
        actionButtonsCard = findViewById(R.id.actionButtonsCard)
        viewHistoryButton = findViewById(R.id.viewHistoryButton)
        bookAnotherRideButton = findViewById(R.id.bookAnotherRideButton)
    }

    private fun setupClickListeners() {
        submitRatingButton.setOnClickListener {
            val rating = ratingBar.rating.toInt()
            val feedback = feedbackInput.text.toString().takeIf { it.isNotBlank() }
            viewModel.submitRating(rating, feedback)
        }
        
        skipRatingButton.setOnClickListener {
            viewModel.skipRating()
        }
        
        viewHistoryButton.setOnClickListener {
            startActivity(Intent(this, RideHistoryActivity::class.java))
        }
        
        bookAnotherRideButton.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.ride.collect { ride ->
                ride?.let { updateTripSummary(it) }
            }
        }
        
        lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                submitRatingButton.isEnabled = !isLoading
                skipRatingButton.isEnabled = !isLoading
            }
        }
        
        lifecycleScope.launch {
            viewModel.error.collect { error ->
                error?.let {
                    Toast.makeText(this@TripCompletionActivity, it, Toast.LENGTH_LONG).show()
                }
            }
        }
        
        lifecycleScope.launch {
            viewModel.ratingSubmitted.collect { submitted ->
                if (submitted) {
                    ratingCard.visibility = android.view.View.GONE
                    actionButtonsCard.visibility = android.view.View.VISIBLE
                    Toast.makeText(this@TripCompletionActivity, "Thank you for your feedback!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateTripSummary(ride: Ride) {
        pickupAddressText.text = ride.pickupLocation.address
        destinationAddressText.text = ride.destinationLocation.address
        
        // Format trip date
        val dateFormat = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
        tripDateText.text = dateFormat.format(Date(ride.requestTime))
        
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
        
        // Driver info will be loaded separately
        viewModel.loadDriverInfo(ride.driverId)
    }

    private fun updateDriverInfo(driverName: String, vehicleInfo: String) {
        driverNameText.text = driverName
        vehicleInfoText.text = vehicleInfo
    }
}