package com.example.tvappmk.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tvappmk.R
import com.example.tvappmk.model.Channel
import com.squareup.picasso.Picasso

class ChannelAdapter(
    private var channels: List<Channel>,
    private val clickListener: (Channel) -> Unit, // This is the click listener lambda
    private val updateFavorite: (Int, Boolean) -> Unit // This function is for updating the favorite state in the database
) : RecyclerView.Adapter<ChannelAdapter.ChannelViewHolder>() {

    // ViewHolder for each item in the RecyclerView
    class ChannelViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val logoImageView: ImageView = view.findViewById(R.id.channel_logo)
        val nameTextView: TextView = view.findViewById(R.id.channel_name)
        val categoryTextView: TextView = view.findViewById(R.id.channel_category)
        val favoriteCheckBox: CheckBox = view.findViewById(R.id.icon)
    }

    // Create a new view holder for each item
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChannelViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.grid_item, parent, false)
        return ChannelViewHolder(view)
    }

    // Bind the data to the view holder
    override fun onBindViewHolder(holder: ChannelViewHolder, position: Int) {
        val channel = channels[position]

        // Set the text for the name and category
        holder.nameTextView.text = channel.name
        holder.categoryTextView.text = channel.category

        // Load the logo image using Picasso
        Picasso.get().load(channel.logoUrl)
            .resize(150, 150)
            .into(holder.logoImageView)

        // Set the checkbox state based on the channel's favorite status
        holder.favoriteCheckBox.isChecked = channel.favorite

        // Handle click on the channel (open video activity or show details)
        holder.itemView.setOnClickListener { clickListener(channel) }

        // Handle changes to the favorite state (check/uncheck)
        holder.favoriteCheckBox.setOnCheckedChangeListener { _, isChecked ->
            channel.favorite = isChecked
            // Update the favorite state in the database
            updateFavorite(channel.id, isChecked)
        }
    }

    // Return the number of items in the list
    override fun getItemCount(): Int = channels.size

    // This function is used to update the list of channels in the adapter
    @SuppressLint("NotifyDataSetChanged")
    fun submitList(newChannels: List<Channel>) {
        this.channels = newChannels
        notifyDataSetChanged() // Refresh the RecyclerView when the list changes
    }
}


