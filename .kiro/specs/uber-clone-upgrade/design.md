# Design Document

## Overview

This design document outlines the technical approach for upgrading the existing Uber clone Android application to provide a complete ride-hailing experience. The upgrade will transform the current basic authentication flow into a fully functional ride-hailing app with real-time location tracking, ride booking, driver matching, and trip management using MVVM architecture, Firebase backend services, and Google Maps integration.

## Architecture

### MVVM Architecture Pattern

The application will follow the Model-View-ViewModel (MVVM) architectural pattern:

- **View Layer**: Activities and Fragments responsible for UI rendering and user interactions
- **ViewModel Layer**: Business logic, state management, and data transformation
- **Model Layer**: Data sources including Firebase, local storage, and API services through Repository pattern

### Key Architectural Components

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   View Layer    │    │ ViewModel Layer │    │  Model Layer    │
│                 │    │                 │    │                 │
│ • MapActivity   │◄──►│ • MapViewModel  │◄──►│ • RideRepository│
│ • ProfileActivity│    │ • ProfileVM     │    │ • UserRepository│
│ • Fragments     │    │ • TripViewModel │    │ • LocationRepo  │
│                 │    │                 │    │ • Firebase      │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

## Components and Interfaces

### 1. Map Component
**Primary Activity**: `MapActivity`
**ViewModel**: `MapViewModel`
**Key Features**:
- Google Maps integration with current location
- Location search with Google Places API
- Route display and navigation
- Ride booking interface
- Real-time driver tracking

### 2. Profile Component
**Primary Activity**: `ProfileActivity`
**ViewModel**: `ProfileViewModel`
**Key Features**:
- User profile display and editing
- Ride history access
- Account settings management

### 3. Location Services
**Repository**: `LocationRepository`
**Key Features**:
- GPS location tracking
- Location permission handling
- Address geocoding and reverse geocoding
- Google Places API integration

### 4. Ride Management
**Repository**: `RideRepository`
**ViewModel**: `RideViewModel`
**Key Features**:
- Ride request creation and management
- Driver matching simulation
- Trip status tracking
- Fare calculation

### 5. User Management
**Repository**: `UserRepository`
**Key Features**:
- Firebase Authentication integration
- User profile data management
- Trip history storage
- Social media authentication (Google, Facebook)
- Onboarding state management

### 6. Onboarding Component
**Activities**: `StartedActivity`, `MobileActivity`, `MobileVerifyActivity`
**Key Features**:
- One-time onboarding flow management
- East African country selection with phone validation
- Social media authentication integration
- Default verification code handling for development

## Data Models

### User Model
```kotlin
data class User(
    val id: String,
    val name: String,
    val email: String,
    val phoneNumber: String?,
    val profileImageUrl: String?,
    val rating: Double = 0.0,
    val totalTrips: Int = 0
)
```

### Location Model
```kotlin
data class Location(
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val placeId: String? = null
)
```

### Ride Model
```kotlin
data class Ride(
    val id: String,
    val userId: String,
    val pickupLocation: Location,
    val destinationLocation: Location,
    val rideType: RideType,
    val status: RideStatus,
    val estimatedPrice: Double,
    val actualPrice: Double? = null,
    val driverId: String? = null,
    val requestTime: Long,
    val startTime: Long? = null,
    val endTime: Long? = null,
    val rating: Int? = null,
    val feedback: String? = null
)

enum class RideType { UBER_X, UBER_XL, UBER_COMFORT }
enum class RideStatus { REQUESTED, DRIVER_ASSIGNED, DRIVER_ARRIVING, IN_PROGRESS, COMPLETED, CANCELLED }
```

### Driver Model (for simulation)
```kotlin
data class Driver(
    val id: String,
    val name: String,
    val rating: Double,
    val vehicleInfo: String,
    val currentLocation: Location
)
```

### Country Model
```kotlin
data class Country(
    val name: String,
    val code: String,
    val phoneCode: String,
    val flag: String,
    val phoneFormat: String
)
```

### Onboarding State Model
```kotlin
data class OnboardingState(
    val hasSeenGetStarted: Boolean = false,
    val isPhoneVerified: Boolean = false,
    val hasCompletedProfile: Boolean = false,
    val selectedCountry: Country? = null
)
```

## Error Handling

### Error Types and Handling Strategy

1. **Network Errors**
   - Display user-friendly messages
   - Implement retry mechanisms
   - Cache essential data for offline access

2. **Location Errors**
   - Handle permission denials gracefully
   - Provide manual location selection fallback
   - Show appropriate error messages

3. **Firebase Errors**
   - Implement exponential backoff for retries
   - Handle authentication failures
   - Manage connection timeouts

4. **Google Maps/Places API Errors**
   - Handle API quota exceeded scenarios
   - Provide fallback for location search
   - Manage service unavailability

### Loading States

- **Map Loading**: Show skeleton map with loading indicator
- **Location Search**: Display search progress indicator
- **Ride Request**: Show "Finding Driver" animation
- **Profile Loading**: Display shimmer effect for profile data

## Testing Strategy

### Unit Testing
- ViewModel business logic testing
- Repository data operations testing
- Utility functions testing
- Error handling scenarios testing

### Integration Testing
- Firebase integration testing
- Google Maps API integration testing
- Location services testing

### UI Testing
- User flow testing (booking a ride)
- Navigation testing
- Error state UI testing
- Accessibility testing

## Implementation Phases

### Phase 1: Core Infrastructure
- MVVM architecture setup
- Firebase integration
- Google Maps basic integration
- Location services implementation

### Phase 2: Map Functionality
- Current location display
- Location search and selection
- Route display
- Basic UI components

### Phase 3: Ride Booking
- Ride type selection
- Price estimation
- Ride request functionality
- Driver simulation

### Phase 4: Real-time Features
- Trip tracking
- Status updates
- Driver communication simulation

### Phase 5: Profile and History
- Profile management
- Trip history
- Rating system

## Dependencies and Configuration

### Required Dependencies
```gradle
// Google Maps and Places
implementation 'com.google.android.gms:play-services-maps:18.2.0'
implementation 'com.google.android.gms:play-services-location:21.0.1'
implementation 'com.google.libraries.places:places:3.3.0'

// Firebase (already included)
implementation platform('com.google.firebase:firebase-bom:33.14.0')
implementation 'com.google.firebase:firebase-auth-ktx'
implementation 'com.google.firebase:firebase-firestore-ktx'
implementation 'com.google.firebase:firebase-storage-ktx'

// MVVM Architecture
implementation 'androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0'
implementation 'androidx.lifecycle:lifecycle-livedata-ktx:2.7.0'
implementation 'androidx.activity:activity-ktx:1.8.2'
implementation 'androidx.fragment:fragment-ktx:1.6.2'

// Navigation
implementation 'androidx.navigation:navigation-fragment-ktx:2.7.6'
implementation 'androidx.navigation:navigation-ui-ktx:2.7.6'

// Image Loading
implementation 'com.github.bumptech.glide:glide:4.16.0'

// Networking
implementation 'com.squareup.retrofit2:retrofit:2.9.0'
implementation 'com.squareup.retrofit2:converter-gson:2.9.0'

// Social Media Authentication
implementation 'com.google.android.gms:play-services-auth:20.7.0'
implementation 'com.facebook.android:facebook-login:16.2.0'

// SharedPreferences for onboarding state
implementation 'androidx.preference:preference-ktx:1.2.1'
```

### Required Permissions
```xml
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.CALL_PHONE" />
```

### API Keys Configuration
- Google Maps API key
- Google Places API key
- Firebase configuration (already set up)

## Security Considerations

1. **Location Privacy**: Only request location when necessary and explain usage
2. **Data Encryption**: Encrypt sensitive user data in Firebase
3. **API Key Security**: Restrict API keys to specific package names
4. **Authentication**: Maintain secure Firebase authentication flow
5. **Input Validation**: Validate all user inputs before processing

## Performance Optimizations

1. **Map Performance**: 
   - Use appropriate zoom levels
   - Limit marker updates
   - Implement map clustering for multiple drivers

2. **Firebase Optimization**:
   - Use pagination for trip history
   - Implement offline persistence
   - Optimize query structures

3. **Memory Management**:
   - Proper lifecycle management for ViewModels
   - Image loading optimization with Glide
   - Location updates optimization