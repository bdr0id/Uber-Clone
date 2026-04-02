package co.ke.snilloc.uberclone.data.location

import android.content.Context
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient

object PlacesInitializer {
    private var placesClient: PlacesClient? = null
    
    fun initialize(context: Context, apiKey: String): PlacesClient {
        if (placesClient == null) {
            if (!Places.isInitialized()) {
                Places.initialize(context.applicationContext, apiKey)
            }
            placesClient = Places.createClient(context)
        }
        return placesClient!!
    }
    
    fun getPlacesClient(): PlacesClient? {
        return placesClient
    }
    
    fun isInitialized(): Boolean {
        return Places.isInitialized() && placesClient != null
    }
}