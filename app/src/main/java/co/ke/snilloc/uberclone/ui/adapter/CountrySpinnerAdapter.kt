package co.ke.snilloc.uberclone.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import co.ke.snilloc.uberclone.R
import co.ke.snilloc.uberclone.data.model.Country

class CountrySpinnerAdapter(
    private val context: Context,
    private val countries: List<Country>
) : BaseAdapter() {

    override fun getCount(): Int = countries.size

    override fun getItem(position: Int): Country = countries[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        return createView(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
        return createView(position, convertView, parent)
    }

    private fun createView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(
            android.R.layout.simple_spinner_item, parent, false
        )
        
        val textView = view.findViewById<TextView>(android.R.id.text1)
        val country = countries[position]
        textView.text = country.flag
        textView.textSize = 28f // Make flag bigger
        textView.setPadding(16, 16, 16, 16) // Add some padding
        
        return view
    }
}