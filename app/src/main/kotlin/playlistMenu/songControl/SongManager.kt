package playlistMenu.songControl

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import android.widget.Toast
import playlistMenu.interfaces.ISong
import java.io.IOException

object SongManager {

    private var currentSong: ISong? = null
    private var isPlaying: Boolean = false
    var skipTheIntro: Boolean = false
    var skipTheOutro: Boolean = false

    private var mediaPlayer: MediaPlayer? = null

    fun play(context: Context, song: ISong) {
        if (currentSong != song) {
            if (mediaPlayer?.isPlaying == true) {
                stop()
            }
            currentSong = song
            initializeMediaPlayer(context, song)

            mediaPlayer?.setOnPreparedListener {
                it.start()
                isPlaying = true
                if (skipTheIntro) {
                    it.seekTo(song.introDuration.toInt())
                }
            }
        } else {
            mediaPlayer?.let {
                if (!it.isPlaying) {
                    it.start()
                    isPlaying = true
                }
            }
        }
    }



    fun pause() {
        mediaPlayer?.let { player ->
            if (isPlaying) {
                player.pause()
                isPlaying = false
            }
        } ?: run {
            Log.e("SongManager", "MediaPlayer is not initialized in pause")
        }
    }

    fun stop() {
        mediaPlayer?.let { player ->
            if (isPlaying) {
                player.stop()
                player.reset()
                isPlaying = false
            }
        } ?: run {
            Log.e("SongManager", "MediaPlayer is not initialized in stop")
        }
        currentSong = null
    }

    fun seekTo(position: Int) {
        mediaPlayer?.let {
            if (position in 0..it.duration) {
                it.seekTo(position)
            } else {
                Log.e("SongManager", "$position : ${it.duration}")
            }
        }
    }




    fun getCurrentPosition(): Int {
        return mediaPlayer?.currentPosition ?: 0
    }


    fun isPlaying(): Boolean {
        return isPlaying
    }

    fun setGlobalVolume(context: Context, volume: Int) {
        if (volume in 0..100) {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val newVolume = (volume / 100.0 * maxVolume).toInt()
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, 0)
        }
    }

    fun setLocalVolume(song: ISong, volume: Int) {
        if (volume in 0..100) {
            song.localVolume = volume
            if (currentSong == song) {
                applyLocalVolume(song)
            }
        }
    }

    private fun applyLocalVolume(song: ISong) {
        val volumeLevel = song.localVolume / 100.0f
        mediaPlayer?.setVolume(volumeLevel, volumeLevel)
    }

    private fun skipIntro(song: ISong) {
        if (skipTheIntro) {
            mediaPlayer?.seekTo((song.introDuration * 1000).toInt())
        }
    }

    private fun skipOutro(song: ISong) {
        if (skipTheOutro) {
            val outroStartTime = song.duration - song.outroDuration
            if (mediaPlayer?.currentPosition!! >= outroStartTime) {
                stop()
            }
        }
    }

    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
        isPlaying = false
        currentSong = null
    }

    private fun initializeMediaPlayer(context: Context, song: ISong) {
        mediaPlayer = MediaPlayer().apply {
            try {
                setDataSource(context, Uri.parse(song.filePath))
                setOnPreparedListener {
                    if (skipTheIntro) {
                        seekTo((song.introDuration * 1000).toInt())
                    }
                }
                prepareAsync()
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(context, e.localizedMessage ?: "Unknown error", Toast.LENGTH_SHORT).show()
                release()
            }
        }
    }

}