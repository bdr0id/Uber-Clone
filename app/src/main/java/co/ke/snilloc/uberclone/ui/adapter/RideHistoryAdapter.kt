package co.ke.snilloc.uberclone.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import co.ke.snilloc.uberclone.R
import co.ke.snilloc.uberclone.data.model.Ride
import co.ke.snilloc.uberclone.data.model.RideStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RideHistoryAdapter(
    private val onRideClick: (Ride) -> Unit
) : RecyclerView.Adapter<RideHistoryAdapter.RideViewHolder>() {

    private var rides = listOf<Ride>()

    fun updateRides(newRides: List<Ride>) {
        rides = newRides
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RideViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ride_history, parent, false)
        return RideViewHolder(view)
    }

    override fun onBindViewHolder(holder: RideViewHolder, position: Int) {
        holder.bind(rides[position])
    }

    override fun getItemCount(): Int = rides.size

    inner class RideViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dateText: TextView = itemView.findViewById(R.id.dateText)
        private val timeText: TextView = itemView.findViewById(R.id.timeText)
        private val pickupAddressText: TextView = itemView.findViewById(R.id.pickupAddressText)
        private val destinationAddressText: TextView = itemView.findViewById(R.id.destinationAddressText)
        private val fareText: TextView = itemView.findViewById(R.id.fareText)
        private val statusText: TextView = itemView.findViewById(R.id.statusText)
        private val ratingText: TextView = itemView.findViewById(R.id.ratingText)

        fun bind(ride: Ride) {
            // Format date and time
            val date = Date(ride.requestTime)
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
            
            dateText.text = dateFormat.format(date)
            timeText.text = timeFormat.format(date)
            
            // Set addresses
            pickupAddressText.text = ride.pickupLocation.address
            destinationAddressText.text = ride.destinationLocation.address
            
            // Set fare
            val fare = ride.actualPrice ?: ride.estimatedPrice
            fareText.text = "KSh ${String.format("%.2f", fare)}"
            
            // Set status
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
            statusText.setTextColor(itemView.context.getColor(statusColor))
            
            // Set rating if available
            if (ride.rating != null && ride.rating > 0) {
                ratingText.text = "${ride.rating}⭐"
                ratingText.visibility = View.VISIBLE
            } else {
                ratingText.visibility = View.GONE
            }
            
            // Set click listener
            itemView.setOnClickListener {
                onRideClick(ride)
            }
        }
    }
}