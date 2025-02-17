package com.example.tvappmk.fragments

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.GridLayoutManager
import com.example.tvappmk.activities.VideoActivity
import com.example.tvappmk.adapter.ChannelAdapter
import com.example.tvappmk.data.AppDatabase
import com.example.tvappmk.data.ChannelDao
import com.example.tvappmk.databinding.FragmentHomeBinding
import com.example.tvappmk.model.ChannelViewModel

class HomeFragment : Fragment() {

    private lateinit var viewModel: ChannelViewModel
    private lateinit var adapter: ChannelAdapter
    private lateinit var channelDao: ChannelDao
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var layoutManager: GridLayoutManager

    // Default grid size is 2 columns
    private var numColumns = 2

    // Preference listener to handle grid size change
    private val preferenceListener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPrefs, key ->
        if (key == "grid_size") {
            val gridSize = sharedPrefs.getString(key, "2") ?: "2"
            numColumns = gridSize.toInt()
            setGridLayout(numColumns)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        // Initialize ViewModel
        viewModel = ViewModelProvider(requireActivity())[ChannelViewModel::class.java]

        // Initialize ChannelDao
        channelDao = AppDatabase.getDatabase(requireContext()).channelDao()

        // Retrieve the grid size from preferences and set it
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val gridSize = sharedPreferences.getString("grid_size", "2") ?: "2"
        numColumns = gridSize.toInt()

        // Set the layout manager with the initial grid size
        layoutManager = GridLayoutManager(requireContext(), numColumns)
        binding.recyclerview.layoutManager = layoutManager

        // Initialize the adapter and pass the clickListener and updateFavorite function
        adapter = ChannelAdapter(emptyList(), clickListener = { channel ->
            // Handle the click event here (e.g., opening the stream URL)
            viewModel.fetchAndSetStreamUrl(channel)

            // Observe the updated stream URL and open VideoActivity
            viewModel.channels.observe(viewLifecycleOwner) { updatedChannels ->
                val updatedChannel = updatedChannels.find { it.name == channel.name }
                updatedChannel?.streamUrl?.let { streamUrl ->
                    if (streamUrl.isNotEmpty()) {
                        val intent = Intent(requireContext(), VideoActivity::class.java)
                        intent.putExtra("STREAM_URL", streamUrl)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                        startActivity(intent)
                    }
                }
            }
        }, updateFavorite = { channelId, favorite ->
            // Update the favorite state in the database
            viewModel.updateFavorite(channelId, favorite)
        })

        // Set the adapter on the RecyclerView
        binding.recyclerview.adapter = adapter

        // Observe ViewModel for channel list updates
        viewModel.channels.observe(viewLifecycleOwner) { channels ->
            // Update the adapter with the new list of channels
            adapter.submitList(channels)
        }

        // Register the preference listener
        sharedPreferences.registerOnSharedPreferenceChangeListener(preferenceListener)

        return binding.root
    }

    // Helper function to set the grid layout with the given number of columns
    private fun setGridLayout(columns: Int) {
        layoutManager.spanCount = columns
    }

    override fun onResume() {
        super.onResume()
        // Ensure grid layout is updated when fragment is resumed
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val gridSize = sharedPreferences.getString("grid_size", "2") ?: "2"
        numColumns = gridSize.toInt()
        setGridLayout(numColumns)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Unregister the preference listener when the view is destroyed
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(preferenceListener)
        _binding = null
    }
}
