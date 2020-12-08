package com.example.myproject.city

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myproject.R

class CitiesRecyclerAdapter (context: Context,
                             private val cities: List<City>,
                             private val citiesClickListener: CitiesClickListener
)
    : RecyclerView.Adapter<CitiesRecyclerAdapter.ViewHolder>() {
    private val inflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = inflater.inflate(R.layout.city_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.nameView.text = cities[position].name
        holder.itemView.setOnClickListener {
           citiesClickListener.onCitiesClickListener(position)
        }
    }

    override fun getItemCount(): Int {
        return cities.size
    }

     class ViewHolder constructor(view: View): RecyclerView.ViewHolder(view) {
        val nameView = view.findViewById<TextView>(R.id.item_city_name)

    }
}