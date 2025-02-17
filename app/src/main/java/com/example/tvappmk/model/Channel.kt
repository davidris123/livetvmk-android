package com.example.tvappmk.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Channel(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val name: String,
    val logoUrl: String,
    val category: String,
    var favorite: Boolean,
    var streamUrl: String
)
