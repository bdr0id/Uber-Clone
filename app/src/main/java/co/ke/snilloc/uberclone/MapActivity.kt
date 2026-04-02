package co.ke.snilloc.uberclone

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import co.ke.snilloc.uberclone.data.location.PlacesInitializer
import co.ke.snilloc.uberclone.data.model.Driver
import co.ke.snilloc.uberclone.data.model.Location
import co.ke.snilloc.uberclone.data.model.RideStatus
import co.ke.snilloc.uberclone.data.model.RideType
import co.ke.snilloc.uberclone.ui.adapter.RideTypesAdapter
import co.ke.snilloc.uberclone.ui.adapter.SearchResultsAdapter
import co.ke.snilloc.uberclone.ui.viewmodel.MapViewModel
import co.ke.snilloc.uberclone.ui.viewmodel.MapViewModelFactory
import kotlinx.coroutines.launch

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private lateinit var mapViewModel: MapViewModel
    
    // UI Components
    private lateinit var profileIcon: ImageView
    private lateinit var pickupLocationInput: EditText
    private lateinit var destinationLocationInput: EditText
    private lateinit var searchResultsRecyclerView: RecyclerView
    private lateinit var rideOptionsCard: CardView
    private lateinit var rideTypesRecyclerView: RecyclerView
    private lateinit var requestRideButton: Button
    private lateinit var currentLocationButton: FloatingActionButton
    private lateinit var loadingOverlay: FrameLayout
    private lateinit var loadingText: TextView
    
    // Ride Tracking UI Components
    private lateinit var rideTrackingOverlay: View
    private lateinit var driverPhoto: ImageView
    private lateinit var driverName: TextView
    private lateinit var vehicleInfo: TextView
    private lateinit var driverRating: TextView
    private lateinit var rideStatus: TextView
    private lateinit var estimatedTime: TextView
    private lateinit var pickupAddress: TextView
    private lateinit var destinationAddress: TextView
    private lateinit var callDriverButton: ImageView
    private lateinit var messageDriverButton: ImageView
    private lateinit var cancelRideButton: Button
    private lateinit var shareLocationButton: Button
    
    // Adapters
    private lateinit var searchResultsAdapter: SearchResultsAdapter
    private lateinit var rideTypesAdapter: RideTypesAdapter
    
    // Map markers and polylines
    private var pickupMarker: Marker? = null
    private var destinationMarker: Marker? = null
    private var driverMarker: Marker? = null
    private var routePolyline: Polyline? = null
    
    // Current input field being edited
    private var currentEditingField: EditText? = null

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        
        if (fineLocationGranted || coarseLocationGranted) {
            enableMyLocation()
            mapViewModel.getCurrentLocation()
        } else {
            Toast.makeText(this, "Location permission is required for this feature", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        
        // Initialize Places API before creating ViewModel
        try {
            PlacesInitializer.initialize(this, "AIzaSyD7nM3-Fj8zz-zpsI9jUC8ztyrSSjCyFY8")
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to initialize Places API: ${e.message}", Toast.LENGTH_LONG).show()
        }
        
        // Initialize ViewModel with factory
        val factory = MapViewModelFactory(application)
        mapViewModel = ViewModelProvider(this, factory)[MapViewModel::class.java]
        
        initializeViews()
        setupRecyclerViews()
        setupClickListeners()
        setupTextWatchers()
        observeViewModel()
        
        // Initialize map
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun initializeViews() {
        profileIcon = findViewById(R.id.profileIcon)
        pickupLocationInput = findViewById(R.id.pickupLocationInput)
        destinationLocationInput = findViewById(R.id.destinationLocationInput)
        searchResultsRecyclerView = findViewById(R.id.searchResultsRecyclerView)
        rideOptionsCard = findViewById(R.id.rideOptionsCard)
        rideTypesRecyclerView = findViewById(R.id.rideTypesRecyclerView)
        requestRideButton = findViewById(R.id.requestRideButton)
        currentLocationButton = findViewById(R.id.currentLocationButton)
        loadingOverlay = findViewById(R.id.loadingOverlay)
        loadingText = findViewById(R.id.loadingText)
        
        // Initialize ride tracking overlay components
        rideTrackingOverlay = findViewById(R.id.rideTrackingOverlay)
        driverPhoto = rideTrackingOverlay.findViewById(R.id.driverPhoto)
        driverName = rideTrackingOverlay.findViewById(R.id.driverName)
        vehicleInfo = rideTrackingOverlay.findViewById(R.id.vehicleInfo)
        driverRating = rideTrackingOverlay.findViewById(R.id.driverRating)
        rideStatus = rideTrackingOverlay.findViewById(R.id.rideStatus)
        estimatedTime = rideTrackingOverlay.findViewById(R.id.estimatedTime)
        pickupAddress = rideTrackingOverlay.findViewById(R.id.pickupAddress)
        destinationAddress = rideTrackingOverlay.findViewById(R.id.destinationAddress)
        callDriverButton = rideTrackingOverlay.findViewById(R.id.callDriverButton)
        messageDriverButton = rideTrackingOverlay.findViewById(R.id.messageDriverButton)
        cancelRideButton = rideTrackingOverlay.findViewById(R.id.cancelRideButton)
        shareLocationButton = rideTrackingOverlay.findViewById(R.id.shareLocationButton)
    }

    private fun setupRecyclerViews() {
        // Search results adapter
        searchResultsAdapter = SearchResultsAdapter { location ->
            selectLocation(location)
        }
        searchResultsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MapActivity)
            adapter = searchResultsAdapter
        }
        
        // Ride types adapter
        rideTypesAdapter = RideTypesAdapter { rideType ->
            mapViewModel.selectRideType(rideType)
        }
        rideTypesRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MapActivity)
            adapter = rideTypesAdapter
        }
    }

    private fun setupClickListeners() {
        profileIcon.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
        
        currentLocationButton.setOnClickListener {
            if (hasLocationPermission()) {
                mapViewModel.getCurrentLocation()
            } else {
                requestLocationPermission()
            }
        }
        
        requestRideButton.setOnClickListener {
            mapViewModel.requestRide()
        }
        
        // Ride tracking overlay click listeners
        cancelRideButton.setOnClickListener {
            mapViewModel.cancelRide()
        }
        
        callDriverButton.setOnClickListener {
            val driver = mapViewModel.assignedDriver.value
            driver?.phoneNumber?.let { phoneNumber ->
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    data = android.net.Uri.parse("tel:$phoneNumber")
                }
                startActivity(intent)
            }
        }
        
        messageDriverButton.setOnClickListener {
            // In a real app, this would open a messaging interface
            Toast.makeText(this, "Messaging feature coming soon", Toast.LENGTH_SHORT).show()
        }
        
        shareLocationButton.setOnClickListener {
            val currentLocation = mapViewModel.currentLocation.value
            currentLocation?.let { location ->
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, "My current location: https://maps.google.com/?q=${location.latitude},${location.longitude}")
                }
                startActivity(Intent.createChooser(intent, "Share location"))
            }
        }
        
        // Handle map clicks for pin dropping
        // This will be set up in onMapReady
    }

    private fun setupTextWatchers() {
        pickupLocationInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                currentEditingField = pickupLocationInput
                s?.toString()?.let { query ->
                    if (query.isNotBlank()) {
                        mapViewModel.searchPlaces(query)
                    } else {
                        mapViewModel.clearSearchResults()
                    }
                }
            }
        })
        
        destinationLocationInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                currentEditingField = destinationLocationInput
                s?.toString()?.let { query ->
                    if (query.isNotBlank()) {
                        mapViewModel.searchPlaces(query)
                    } else {
                        mapViewModel.clearSearchResults()
                    }
                }
            }
        })
        
        // Clear search results when focus is lost
        pickupLocationInput.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && destinationLocationInput.hasFocus().not()) {
                hideSearchResults()
            }
        }
        
        destinationLocationInput.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && pickupLocationInput.hasFocus().not()) {
                hideSearchResults()
            }
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            // Observe loading state
            mapViewModel.isLoading.collect { isLoading ->
                loadingOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }
        
        lifecycleScope.launch {
            // Observe errors
            mapViewModel.error.collect { error ->
                error?.let {
                    Toast.makeText(this@MapActivity, it, Toast.LENGTH_LONG).show()
                }
            }
        }
        
        lifecycleScope.launch {
            // Observe current location
            mapViewModel.currentLocation.collect { location ->
                location?.let {
                    updateCurrentLocationOnMap(it)
                }
            }
        }
        
        lifecycleScope.launch {
            // Observe pickup location
            mapViewModel.pickupLocation.collect { location ->
                location?.let {
                    pickupLocationInput.setText(it.address)
                    updatePickupMarker(it)
                    updateRoute()
                }
            }
        }
        
        lifecycleScope.launch {
            // Observe destination location
            mapViewModel.destinationLocation.collect { location ->
                location?.let {
                    destinationLocationInput.setText(it.address)
                    updateDestinationMarker(it)
                    updateRoute()
                    showRideOptions()
                }
            }
        }
        
        lifecycleScope.launch {
            // Observe search results
            mapViewModel.searchResults.collect { results ->
                searchResultsAdapter.updateLocations(results)
                searchResultsRecyclerView.visibility = if (results.isNotEmpty()) View.VISIBLE else View.GONE
            }
        }
        
        lifecycleScope.launch {
            // Observe ride types and selected type
            mapViewModel.rideTypes.collect { rideTypes ->
                lifecycleScope.launch {
                    mapViewModel.selectedRideType.collect { selectedType ->
                        lifecycleScope.launch {
                            mapViewModel.estimatedPrice.collect { price ->
                                rideTypesAdapter.updateRideTypes(rideTypes, selectedType, price)
                            }
                        }
                    }
                }
            }
        }
        
        lifecycleScope.launch {
            // Observe current ride for status updates
            mapViewModel.currentRide.collect { ride ->
                ride?.let {
                    updateRideStatus(it.status)
                } ?: run {
                    // No active ride, hide loading and reset UI
                    loadingOverlay.visibility = View.GONE
                    hideRideTracking()
                }
            }
        }
        
        lifecycleScope.launch {
            // Observe driver location for real-time tracking
            mapViewModel.driverLocation.collect { location ->
                location?.let {
                    updateDriverMarker(it)
                }
            }
        }
        
        lifecycleScope.launch {
            // Observe assigned driver
            mapViewModel.assignedDriver.collect { driver ->
                driver?.let {
                    showDriverInfo(it)
                }
            }
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        
        // Configure map settings
        googleMap.uiSettings.apply {
            isZoomControlsEnabled = true
            isCompassEnabled = true
            isMyLocationButtonEnabled = false // We have our own button
        }
        
        // Set up map click listener for pin dropping
        googleMap.setOnMapClickListener { latLng ->
            val location = Location(
                latitude = latLng.latitude,
                longitude = latLng.longitude,
                address = "Lat: ${String.format("%.4f", latLng.latitude)}, Lng: ${String.format("%.4f", latLng.longitude)}"
            )
            
            when {
                currentEditingField == pickupLocationInput || 
                (currentEditingField == null && mapViewModel.pickupLocation.value == null) -> {
                    mapViewModel.selectPickupLocation(location)
                }
                currentEditingField == destinationLocationInput ||
                (currentEditingField == null && mapViewModel.destinationLocation.value == null) -> {
                    mapViewModel.selectDestinationLocation(location)
                }
            }
        }
        
        // Enable location if permission is granted
        if (hasLocationPermission()) {
            enableMyLocation()
            mapViewModel.getCurrentLocation()
        } else {
            requestLocationPermission()
        }
    }

    private fun selectLocation(location: Location) {
        when (currentEditingField) {
            pickupLocationInput -> {
                mapViewModel.selectPickupLocation(location)
            }
            destinationLocationInput -> {
                mapViewModel.selectDestinationLocation(location)
            }
        }
        hideSearchResults()
        currentEditingField?.clearFocus()
    }

    private fun hideSearchResults() {
        searchResultsRecyclerView.visibility = View.GONE
        mapViewModel.clearSearchResults()
    }

    private fun showRideOptions() {
        rideOptionsCard.visibility = View.VISIBLE
    }

    private fun updateCurrentLocationOnMap(location: Location) {
        val latLng = LatLng(location.latitude, location.longitude)
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
    }

    private fun updatePickupMarker(location: Location) {
        pickupMarker?.remove()
        val latLng = LatLng(location.latitude, location.longitude)
        pickupMarker = googleMap.addMarker(
            MarkerOptions()
                .position(latLng)
                .title("Pickup Location")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
        )
    }

    private fun updateDestinationMarker(location: Location) {
        destinationMarker?.remove()
        val latLng = LatLng(location.latitude, location.longitude)
        destinationMarker = googleMap.addMarker(
            MarkerOptions()
                .position(latLng)
                .title("Destination")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
        )
    }

    private fun updateRoute() {
        val pickup = mapViewModel.pickupLocation.value
        val destination = mapViewModel.destinationLocation.value
        
        if (pickup != null && destination != null) {
            // Remove existing route
            routePolyline?.remove()
            
            // Create a simple straight line route (in a real app, you'd use Directions API)
            val pickupLatLng = LatLng(pickup.latitude, pickup.longitude)
            val destinationLatLng = LatLng(destination.latitude, destination.longitude)
            
            routePolyline = googleMap.addPolyline(
                PolylineOptions()
                    .add(pickupLatLng, destinationLatLng)
                    .width(8f)
                    .color(ContextCompat.getColor(this, android.R.color.holo_blue_dark))
            )
            
            // Adjust camera to show both markers
            val boundsBuilder = LatLngBounds.Builder()
            boundsBuilder.include(pickupLatLng)
            boundsBuilder.include(destinationLatLng)
            val bounds = boundsBuilder.build()
            
            googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
        }
    }

    private fun updateDriverMarker(location: Location) {
        driverMarker?.remove()
        val latLng = LatLng(location.latitude, location.longitude)
        driverMarker = googleMap.addMarker(
            MarkerOptions()
                .position(latLng)
                .title("Driver")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
        )
    }

    private fun updateRideStatus(status: RideStatus) {
        when (status) {
            RideStatus.REQUESTED -> {
                loadingText.text = "Finding drivers..."
                loadingOverlay.visibility = View.VISIBLE
                rideOptionsCard.visibility = View.GONE
                rideTrackingOverlay.visibility = View.GONE
            }
            RideStatus.DRIVER_ASSIGNED -> {
                rideStatus.text = "Driver assigned"
                estimatedTime.text = mapViewModel.getEstimatedArrivalTime() ?: "Arriving soon"
                showRideTracking()
            }
            RideStatus.DRIVER_ARRIVING -> {
                rideStatus.text = "Driver is arriving"
                estimatedTime.text = mapViewModel.getEstimatedArrivalTime() ?: "Arriving in 5 minutes"
                showRideTracking()
            }
            RideStatus.IN_PROGRESS -> {
                rideStatus.text = "Trip in progress"
                estimatedTime.text = mapViewModel.getEstimatedArrivalTime() ?: "Arriving at destination"
                showRideTracking()
            }
            RideStatus.COMPLETED -> {
                hideRideTracking()
                showTripCompleted()
            }
            RideStatus.CANCELLED -> {
                hideRideTracking()
                Toast.makeText(this, "Ride cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showDriverInfo(driver: Driver) {
        // Update driver info in the ride tracking overlay
        driverName.text = driver.name
        vehicleInfo.text = driver.vehicleInfo
        driverRating.text = driver.rating.toString()
        
        // Update trip addresses
        val pickup = mapViewModel.pickupLocation.value
        val destination = mapViewModel.destinationLocation.value
        pickupAddress.text = pickup?.address ?: "Pickup location"
        destinationAddress.text = destination?.address ?: "Destination"
    }

    private fun showRideTracking() {
        loadingOverlay.visibility = View.GONE
        rideOptionsCard.visibility = View.GONE
        rideTrackingOverlay.visibility = View.VISIBLE
        
        // Update trip addresses when showing tracking
        val pickup = mapViewModel.pickupLocation.value
        val destination = mapViewModel.destinationLocation.value
        pickupAddress.text = pickup?.address ?: "Pickup location"
        destinationAddress.text = destination?.address ?: "Destination"
    }

    private fun hideRideTracking() {
        rideTrackingOverlay.visibility = View.GONE
        rideOptionsCard.visibility = View.VISIBLE
        driverMarker?.remove()
        driverMarker = null
    }

    private fun showTripCompleted() {
        loadingOverlay.visibility = View.GONE
        
        // Navigate to trip completion activity
        val currentRide = mapViewModel.currentRide.value
        if (currentRide != null) {
            val intent = Intent(this, TripCompletionActivity::class.java)
            intent.putExtra("RIDE_ID", currentRide.id)
            startActivity(intent)
            
            // Reset the map state
            mapViewModel.resetRideState()
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    private fun enableMyLocation() {
        if (hasLocationPermission()) {
            try {
                googleMap.isMyLocationEnabled = true
            } catch (e: SecurityException) {
                // Handle exception
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (hasLocationPermission() && mapViewModel.hasLocationPermission()) {
            mapViewModel.startLocationTracking()
        }
    }

    override fun onPause() {
        super.onPause()
        mapViewModel.stopLocationTracking()
    }
}