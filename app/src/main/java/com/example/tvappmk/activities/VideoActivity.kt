package com.example.tvappmk.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.tvappmk.R

class VideoActivity : AppCompatActivity() {

    private lateinit var playerView: PlayerView
    private var player: ExoPlayer? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video)


        playerView = findViewById(R.id.playerView)

        val streamUrl = intent.getStringExtra("STREAM_URL")
        streamUrl?.let {
            if (player == null) {
                // Create a player instance if the activity is first started
                player = ExoPlayer.Builder(this).build()
            }

            player?.setMediaItem(MediaItem.fromUri(streamUrl))
            player?.prepare()
            player?.playWhenReady = true
            playerView.player = player
        }
    }

    override fun onDestroy() {
        //In situations where there is a configuration change
        //(i.e Screen orientation)
        super.onDestroy()
        player?.release();
    }
}
