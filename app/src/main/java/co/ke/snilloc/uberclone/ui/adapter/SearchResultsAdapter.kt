package co.ke.snilloc.uberclone.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import co.ke.snilloc.uberclone.R
import co.ke.snilloc.uberclone.data.model.Location

class SearchResultsAdapter(
    private val onLocationClick: (Location) -> Unit
) : RecyclerView.Adapter<SearchResultsAdapter.ViewHolder>() {

    private var locations = listOf<Location>()

    fun updateLocations(newLocations: List<Location>) {
        locations = newLocations
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_2, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(locations[position])
    }

    override fun getItemCount(): Int = locations.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleText: TextView = itemView.findViewById(android.R.id.text1)
        private val subtitleText: TextView = itemView.findViewById(android.R.id.text2)

        fun bind(location: Location) {
            // Extract main address and secondary info
            val addressParts = location.address.split(",")
            titleText.text = addressParts.firstOrNull()?.trim() ?: location.address
            subtitleText.text = if (addressParts.size > 1) {
                addressParts.drop(1).joinToString(",").trim()
            } else {
                "Lat: ${String.format("%.4f", location.latitude)}, Lng: ${String.format("%.4f", location.longitude)}"
            }

            itemView.setOnClickListener {
                onLocationClick(location)
            }
        }
    }
}