package com.example.tvappmk.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.tvappmk.model.Channel

@Dao
interface ChannelDao {

    @Insert
    suspend fun insert(vararg channel: Channel)

    @Query("SELECT * FROM channel")
    suspend fun getAll() : List<Channel>

    @Query("UPDATE channel SET favorite = :favorite WHERE id = :channelId")
    suspend fun updateFavorite(channelId: Int, favorite: Boolean)

    @Query("SELECT * FROM channel WHERE name LIKE :query")
    suspend fun searchChannels(query: String) : List<Channel>

    @Query("SELECT * FROM channel WHERE name LIKE :query AND favorite = 1")
    fun searchFavoriteChannels(query: String) : List<Channel>

    @Query("UPDATE channel SET streamUrl = :streamUrl WHERE id = :channelId")
    fun updateStreamUrl(channelId: Int, streamUrl: String)

}