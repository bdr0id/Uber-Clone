package co.ke.snilloc.uberclone.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import co.ke.snilloc.uberclone.data.model.RideType

class RideTypesAdapter(
    private val onRideTypeClick: (RideType) -> Unit
) : RecyclerView.Adapter<RideTypesAdapter.ViewHolder>() {

    private var rideTypes = listOf<RideType>()
    private var selectedRideType: RideType = RideType.UBER_X
    private var estimatedPrice: Double? = null

    fun updateRideTypes(newRideTypes: List<RideType>, selected: RideType, price: Double?) {
        rideTypes = newRideTypes
        selectedRideType = selected
        estimatedPrice = price
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_2, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(rideTypes[position])
    }

    override fun getItemCount(): Int = rideTypes.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleText: TextView = itemView.findViewById(android.R.id.text1)
        private val subtitleText: TextView = itemView.findViewById(android.R.id.text2)

        fun bind(rideType: RideType) {
            titleText.text = when (rideType) {
                RideType.UBER_X -> "UberX"
                RideType.UBER_XL -> "UberXL"
                RideType.UBER_COMFORT -> "Uber Comfort"
            }

            val description = when (rideType) {
                RideType.UBER_X -> "Affordable, everyday rides"
                RideType.UBER_XL -> "Larger vehicles for groups"
                RideType.UBER_COMFORT -> "Premium comfort rides"
            }

            val priceText = estimatedPrice?.let { price ->
                val adjustedPrice = when (rideType) {
                    RideType.UBER_X -> price
                    RideType.UBER_XL -> price * 1.3
                    RideType.UBER_COMFORT -> price * 1.2
                }
                " • $${String.format("%.2f", adjustedPrice)}"
            } ?: ""

            subtitleText.text = "$description$priceText"

            // Highlight selected ride type
            if (rideType == selectedRideType) {
                itemView.setBackgroundColor(0xFFE3F2FD.toInt())
                titleText.setTextColor(0xFF1976D2.toInt())
            } else {
                itemView.setBackgroundColor(0xFFFFFFFF.toInt())
                titleText.setTextColor(0xFF000000.toInt())
            }

            itemView.setOnClickListener {
                onRideTypeClick(rideType)
            }
        }
    }
}