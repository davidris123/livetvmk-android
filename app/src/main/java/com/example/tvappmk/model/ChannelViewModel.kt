package com.example.tvappmk.model

import android.app.Application
import androidx.lifecycle.*
import com.example.tvappmk.data.AppDatabase
import com.example.tvappmk.data.ChannelDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.io.IOException

class ChannelViewModel(application: Application) : AndroidViewModel(application) {

    private val channelDao: ChannelDao = AppDatabase.getDatabase(application).channelDao()

    // Private mutable LiveData
    private val _channels = MutableLiveData<List<Channel>>()

    // Public immutable LiveData
    val channels: LiveData<List<Channel>> = _channels

    init {
        loadChannels()
        insertPredefinedChannels()
    }

    private fun loadChannels() {
        // Load channels from the database off the IO thread
        viewModelScope.launch(Dispatchers.IO) {
            val loadedChannels = channelDao.getAll()
            // Post the result back to the LiveData on the main thread
            withContext(Dispatchers.Main) {
                _channels.value = loadedChannels
            }
        }
    }

    private fun insertPredefinedChannels() {
        // Insert predefined channel data if the database is empty (off the main thread)
        viewModelScope.launch(Dispatchers.IO) {
            // Check if the database is empty
            if (channelDao.getAll().isEmpty()) {
                val predefinedChannels = listOf(
                    Channel(0, "Sitel", "https://upload.wikimedia.org/wikipedia/commons/thumb/1/1f/Logo_of_Sitel.svg/2560px-Logo_of_Sitel.svg.png", "News", false, ""),
                    Channel(0, "Kanal 5", "https://upload.wikimedia.org/wikipedia/commons/thumb/8/83/Logo_of_Kanal_5.svg/1200px-Logo_of_Kanal_5.svg.png", "News", false, ""),
                    Channel(0, "Telma", "https://upload.wikimedia.org/wikipedia/commons/e/e5/Telma_logo_%28official%29.png", "News", false, ""),
                    Channel(0, "Alfa", "https://upload.wikimedia.org/wikipedia/commons/thumb/e/e7/Logo_of_Alfa_TV_%282020-%29.svg/1200px-Logo_of_Alfa_TV_%282020-%29.svg.png", "News", false, "")
                )

                // Insert channels into the database
                predefinedChannels.forEach { channel ->
                    channelDao.insert(channel)
                }
                // After inserting, load channels again to update the LiveData
                loadChannels()
            }
        }
    }

    fun searchChannels(query: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = channelDao.searchChannels("%$query%")
            withContext(Dispatchers.Main) {
                _channels.value = result
            }
        }
    }

    fun updateFavorite(channelId: Int, favorite: Boolean) {
        viewModelScope.launch {
            // Update the favorite status of the channel
            channelDao.updateFavorite(channelId, favorite)
        }
    }

    fun fetchAndSetStreamUrl(channel: Channel) {
        // Launch a coroutine to fetch the m3u8 URL
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Fetch the webpage and extract the m3u8 URL
                val m3u8Url = fetchM3U8Url(channel)

                // Update the channel with the fetched m3u8 URL
                if (m3u8Url.isNotEmpty()) {
                    channel.streamUrl = m3u8Url

                    // Save the updated channel in the database (off the main thread)
                    channelDao.updateStreamUrl(channel.id, m3u8Url)

                    // Notify observers on the main thread
                    withContext(Dispatchers.Main) {
                        loadChannels() // Reload the updated list of channels
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun fetchM3U8Url(channel: Channel): String {
        val channelUrls = mapOf(
            "Sitel" to "https://tvstanici.net/sitel-vo-zivo/",
            "Kanal 5" to "https://tvstanici.net/kanal5-vo-zivo/",
            "Telma" to "https://tvstanici.net/telma-vo-zivo/",
            "Alfa" to "https://tvstanici.net/alfa-vo-zivo/"
        )

        val url = channelUrls[channel.name]
        if (url == null) {
            println("No URL found for channel: ${channel.name}")
            return ""
        }

        return try {
            val document: Document = Jsoup.connect(url).get()
            val scriptTag: Element? = document.selectFirst("script:containsData(Clappr.Player)")
            scriptTag?.let {
                val scriptContent = it.data()
                val regex = Regex("""source: "([^"]+\.m3u8\?[^"]+)""")
                val matchResult = regex.find(scriptContent)
                matchResult?.groupValues?.get(1) ?: ""
            } ?: ""
        } catch (e: IOException) {
            e.printStackTrace()
            ""
        }
    }
}
