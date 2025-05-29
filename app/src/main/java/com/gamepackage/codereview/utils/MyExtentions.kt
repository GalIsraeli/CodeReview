package com.gamepackage.codereview.utils

import android.content.Context
import android.media.MediaPlayer
import android.util.Log


fun Context.playSoundEffect(res: Int) {
    var mediaPlayer: MediaPlayer? = null

    try {
        mediaPlayer = MediaPlayer.create(this, res)

        if (mediaPlayer == null) {
            Log.e("SoundEffect", "MediaPlayer creation failed: Check audio file.")
            return
        }

        mediaPlayer.setOnCompletionListener {
            it.release()
        }

        mediaPlayer.setOnErrorListener { mp, what, extra ->
            Log.e("SoundEffect", "MediaPlayer error occurred. What: $what Extra: $extra")
            mp.release()
            true
        }

        mediaPlayer.start()

    } catch (ex: Exception) {
        Log.e("SoundEffect", "Exception playing sound effect", ex)
        mediaPlayer?.release()
    }
}
