package co.ke.snilloc.uberclone

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.ke.snilloc.uberclone.ui.adapter.RideHistoryAdapter
import co.ke.snilloc.uberclone.ui.viewmodel.ProfileViewModel
import co.ke.snilloc.uberclone.data.onboarding.OnboardingManager
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {

    private val viewModel: ProfileViewModel by viewModels()
    private lateinit var onboardingManager: OnboardingManager
    
    // UI Components
    private lateinit var btnBack: ImageButton
    private lateinit var btnEdit: ImageButton
    private lateinit var ivProfileImage: ImageView
    private lateinit var etUserName: EditText
    private lateinit var etUserEmail: EditText
    private lateinit var etUserPhone: EditText
    private lateinit var tvTotalTrips: TextView
    private lateinit var tvUserRating: TextView
    private lateinit var tvTotalSpent: TextView
    private lateinit var llActionButtons: LinearLayout
    private lateinit var btnCancel: Button
    private lateinit var btnSave: Button
    private lateinit var btnViewAllRides: Button
    private lateinit var rvRideHistory: RecyclerView
    private lateinit var progressBar: ProgressBar
    
    // Adapter
    private lateinit var rideHistoryAdapter: RideHistoryAdapter
    
    private var isEditMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        
        onboardingManager = OnboardingManager(this)
        
        initializeViews()
        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
        
        // Load user profile and recent rides
        viewModel.loadUserProfile()
        viewModel.loadRecentRides()
    }

    private fun initializeViews() {
        btnBack = findViewById(R.id.btnBack)
        btnEdit = findViewById(R.id.btnEdit)
        ivProfileImage = findViewById(R.id.ivProfileImage)
        etUserName = findViewById(R.id.etUserName)
        etUserEmail = findViewById(R.id.etUserEmail)
        etUserPhone = findViewById(R.id.etUserPhone)
        tvTotalTrips = findViewById(R.id.tvTotalTrips)
        tvUserRating = findViewById(R.id.tvUserRating)
        tvTotalSpent = findViewById(R.id.tvTotalSpent)
        llActionButtons = findViewById(R.id.llActionButtons)
        btnCancel = findViewById(R.id.btnCancel)
        btnSave = findViewById(R.id.btnSave)
        btnViewAllRides = findViewById(R.id.btnViewAllRides)
        rvRideHistory = findViewById(R.id.rvRideHistory)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun setupRecyclerView() {
        rideHistoryAdapter = RideHistoryAdapter { ride ->
            // Navigate to ride detail
            val intent = Intent(this, RideDetailActivity::class.java)
            intent.putExtra("RIDE_ID", ride.id)
            startActivity(intent)
        }
        
        rvRideHistory.apply {
            layoutManager = LinearLayoutManager(this@ProfileActivity)
            adapter = rideHistoryAdapter
        }
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            finish()
        }
        
        btnEdit.setOnClickListener {
            toggleEditMode()
        }
        
        btnCancel.setOnClickListener {
            cancelEdit()
        }
        
        btnSave.setOnClickListener {
            saveProfile()
        }
        
        btnViewAllRides.setOnClickListener {
            startActivity(Intent(this, RideHistoryActivity::class.java))
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.user.collect { user ->
                user?.let { updateUserInfo(it) }
            }
        }
        
        lifecycleScope.launch {
            viewModel.userStats.collect { stats ->
                stats?.let { updateUserStats(it) }
            }
        }
        
        lifecycleScope.launch {
            viewModel.recentRides.collect { rides ->
                rideHistoryAdapter.updateRides(rides.take(3)) // Show only 3 recent rides
            }
        }
        
        lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }
        
        lifecycleScope.launch {
            viewModel.error.collect { error ->
                error?.let {
                    Toast.makeText(this@ProfileActivity, it, Toast.LENGTH_LONG).show()
                }
            }
        }
        
        lifecycleScope.launch {
            viewModel.profileUpdated.collect { updated ->
                if (updated) {
                    Toast.makeText(this@ProfileActivity, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                    
                    // Mark profile as completed in onboarding flow
                    onboardingManager.markProfileCompleted()
                    
                    toggleEditMode() // Exit edit mode
                    
                    // If this was part of onboarding flow, navigate to map
                    if (onboardingManager.hasCompletedOnboarding()) {
                        val intent = Intent(this@ProfileActivity, MapActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                }
            }
        }
    }

    private fun updateUserInfo(user: co.ke.snilloc.uberclone.data.model.User) {
        etUserName.setText(user.name)
        etUserEmail.setText(user.email)
        etUserPhone.setText(user.phoneNumber ?: "")
        
        // Load profile image if available
        // In a real app, you would use an image loading library like Glide
        // For now, we'll use the default icon
    }

    private fun updateUserStats(stats: ProfileViewModel.UserStats) {
        tvTotalTrips.text = stats.totalTrips.toString()
        tvUserRating.text = String.format("%.1f", stats.averageRating)
        tvTotalSpent.text = "KSh ${String.format("%.2f", stats.totalSpent)}"
    }

    private fun toggleEditMode() {
        isEditMode = !isEditMode
        
        etUserName.isEnabled = isEditMode
        etUserEmail.isEnabled = isEditMode
        etUserPhone.isEnabled = isEditMode
        
        llActionButtons.visibility = if (isEditMode) View.VISIBLE else View.GONE
        
        // Change edit button icon or text if needed
        btnEdit.alpha = if (isEditMode) 0.5f else 1.0f
    }

    private fun cancelEdit() {
        isEditMode = false
        
        etUserName.isEnabled = false
        etUserEmail.isEnabled = false
        etUserPhone.isEnabled = false
        
        llActionButtons.visibility = View.GONE
        btnEdit.alpha = 1.0f
        
        // Reload user data to reset any changes
        viewModel.loadUserProfile()
    }

    private fun saveProfile() {
        val name = etUserName.text.toString().trim()
        val email = etUserEmail.text.toString().trim()
        val phone = etUserPhone.text.toString().trim()
        
        if (name.isBlank()) {
            Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show()
            return
        }
        
        viewModel.updateUserProfile(name, email, phone.takeIf { it.isNotBlank() })
    }
}