package co.ke.snilloc.uberclone

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.ke.snilloc.uberclone.ui.adapter.RideHistoryAdapter
import co.ke.snilloc.uberclone.ui.viewmodel.RideHistoryViewModel
import kotlinx.coroutines.launch

class RideHistoryActivity : AppCompatActivity() {

    private val viewModel: RideHistoryViewModel by viewModels()
    
    // UI Components
    private lateinit var backButton: ImageView
    private lateinit var titleText: TextView
    private lateinit var ridesRecyclerView: RecyclerView
    private lateinit var emptyStateView: View
    private lateinit var emptyStateText: TextView
    private lateinit var loadingProgressBar: ProgressBar
    
    // Adapter
    private lateinit var rideHistoryAdapter: RideHistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ride_history)
        
        initializeViews()
        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
        
        // Load ride history
        viewModel.loadRideHistory()
    }

    private fun initializeViews() {
        backButton = findViewById(R.id.backButton)
        titleText = findViewById(R.id.titleText)
        ridesRecyclerView = findViewById(R.id.ridesRecyclerView)
        emptyStateView = findViewById(R.id.emptyStateView)
        emptyStateText = findViewById(R.id.emptyStateText)
        loadingProgressBar = findViewById(R.id.loadingProgressBar)
    }

    private fun setupRecyclerView() {
        rideHistoryAdapter = RideHistoryAdapter { ride ->
            // Navigate to ride detail or trip completion screen
            val intent = Intent(this, RideDetailActivity::class.java)
            intent.putExtra("RIDE_ID", ride.id)
            startActivity(intent)
        }
        
        ridesRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@RideHistoryActivity)
            adapter = rideHistoryAdapter
        }
    }

    private fun setupClickListeners() {
        backButton.setOnClickListener {
            finish()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.rides.collect { rides ->
                rideHistoryAdapter.updateRides(rides)
                
                if (rides.isEmpty()) {
                    ridesRecyclerView.visibility = View.GONE
                    emptyStateView.visibility = View.VISIBLE
                } else {
                    ridesRecyclerView.visibility = View.VISIBLE
                    emptyStateView.visibility = View.GONE
                }
            }
        }
        
        lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                loadingProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }
        
        lifecycleScope.launch {
            viewModel.error.collect { error ->
                error?.let {
                    Toast.makeText(this@RideHistoryActivity, it, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}