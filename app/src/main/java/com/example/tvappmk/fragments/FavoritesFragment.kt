package com.example.tvappmk.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.tvappmk.activities.VideoActivity
import com.example.tvappmk.adapter.ChannelAdapter
import com.example.tvappmk.databinding.FragmentFavoritesBinding
import com.example.tvappmk.model.ChannelViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FavoritesFragment : Fragment() {

    private lateinit var viewModel: ChannelViewModel
    private lateinit var adapter: ChannelAdapter
    private lateinit var noFavoritesMessage: TextView
    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)

        noFavoritesMessage = binding.noFavoritesMessage

        viewModel = ViewModelProvider(requireActivity())[ChannelViewModel::class.java]

        // Set up RecyclerView with GridLayoutManager
        val layoutManager = GridLayoutManager(requireContext(), 2)
        binding.recyclerview.layoutManager = layoutManager

        // Set up adapter with the appropriate click listener and favorite update listener
        adapter = ChannelAdapter(emptyList(), clickListener = { channel ->
            // Fetch stream URL for the channel and navigate to VideoActivity
            viewModel.fetchAndSetStreamUrl(channel)

            viewModel.channels.observe(viewLifecycleOwner) { updatedChannels ->
                val updatedChannel = updatedChannels.find { it.name == channel.name }
                updatedChannel?.streamUrl?.let { streamUrl ->
                    if (streamUrl.isNotEmpty()) {
                        val intent = Intent(requireContext(), VideoActivity::class.java)
                        intent.putExtra("STREAM_URL", streamUrl)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                    }
                }
            }
        }, updateFavorite = { channelId, favorite ->
            // Update favorite state in the database
            viewModel.updateFavorite(channelId, favorite)
        })

        // Set the adapter for the RecyclerView
        binding.recyclerview.adapter = adapter

        // Observe changes to the channel list
        viewModel.channels.observe(viewLifecycleOwner) { channels ->
            // Filter out only favorite channels
            val favoriteChannels = channels.filter { it.favorite }

            if (favoriteChannels.isEmpty()) {
                noFavoritesMessage.visibility = View.VISIBLE
                binding.recyclerview.visibility = View.GONE
            }

            adapter.submitList(favoriteChannels)
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
