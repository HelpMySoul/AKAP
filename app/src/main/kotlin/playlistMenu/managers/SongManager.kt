package playlistMenu.managers

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.example.akap.R
import playlistMenu.controllers.BroadcastManagerController
import playlistMenu.interfaces.ISong
import java.io.IOException

object SongManager {

    private var mediaPlayer:    MediaPlayer?    = null
    private var currentSong:    ISong?          = null

    var isPlaying:              Boolean         = false
    var isRepeating:            Boolean         = false
    var skipTheIntro:           Boolean         = false
    var skipTheOutro:           Boolean         = false

    fun play(context: Context, song: ISong) {
        Log.e("SongManager", "Current song: ${song.title}")

        if (mediaPlayer?.isPlaying == true) {
            stop()
        }

        initializeMediaPlayer(context, song)

        currentSong = song

        mediaPlayer?.setOnPreparedListener {
            it.start()
            isPlaying = true
            setLocalVolume(song.localVolume / 100f)
            if (skipTheIntro) {
                it.seekTo(song.introDuration.toInt())
            }
        }

        PlayerSettingsManager.saveSettings(context)

        BroadcastManagerController(context).sendBroadcast("SHOW_PLAYER")
    }

    fun unpause() {
        mediaPlayer?.let { player ->
            if (!isPlaying) {
                player.start()
                isPlaying = true
            }
        } ?: run {
            Log.e("SongManager", "MediaPlayer is not initialized in unpause")
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
    }

    fun seekTo(position: Int) {
        mediaPlayer?.let {
            if (position in 0..it.duration) {
                it.seekTo(position)
            } else {
                Log.e("SongManager", "Invalid seek position: $position")
            }
        }
    }

    fun setSongIntroTime(context: Context) {
        if (!isPlaying) {
            Toast.makeText(context, context.getString(R.string.no_song_playing), Toast.LENGTH_SHORT).show()
            return
        }

        val currentPosition = getCurrentPosition()
        val song = currentSong ?: run {
            Toast.makeText(context, context.getString(R.string.no_song_playing), Toast.LENGTH_SHORT).show()
            return
        }

        if (currentPosition >= song.duration - song.outroDuration) {
            Toast.makeText(context, context.getString(R.string.too_late_intro), Toast.LENGTH_SHORT).show()
            return
        }

        song.introDuration = currentPosition.toLong()
        SongMetadataManager.saveMetadata(context, song)
        Toast.makeText(context, context.getString(R.string.intro_set_successfully), Toast.LENGTH_SHORT).show()
    }

    fun setSongOutroTime(context: Context) {
        if (!isPlaying) {
            Toast.makeText(context, context.getString(R.string.no_song_playing), Toast.LENGTH_SHORT).show()
            return
        }

        val currentPosition = getCurrentPosition()
        val song = currentSong ?: run {
            Toast.makeText(context, context.getString(R.string.no_song_playing), Toast.LENGTH_SHORT).show()
            return
        }

        if (currentPosition <= song.introDuration) {
            Toast.makeText(context, context.getString(R.string.too_early_outro), Toast.LENGTH_SHORT).show()
            return
        }

        song.outroDuration = song.duration - currentPosition
        SongMetadataManager.saveMetadata(context, song)
        Toast.makeText(context, context.getString(R.string.outro_set_successfully), Toast.LENGTH_SHORT).show()
    }

    fun getCurrentPosition(): Int {
        return mediaPlayer?.currentPosition ?: 0
    }


    fun setLocalVolume(volume: Float) {
        mediaPlayer?.setVolume(volume, volume)
    }

    fun saveLocalVolume(context: Context, volume: Int) {
        currentSong?.let { song ->
            song.localVolume = (volume)
            SongMetadataManager.saveMetadata(context, song)
        }
    }

    fun getLocalVolume(): Int {
        return currentSong?.localVolume ?: 100
    }


    fun release() {
        mediaPlayer?.release()
        mediaPlayer     = null
        isPlaying       = false
    }

    private fun initializeMediaPlayer(context: Context, song: ISong) {
        mediaPlayer = MediaPlayer().apply {
            try {
                setDataSource(context, Uri.parse(song.filePath))
                prepareAsync()
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(context, e.localizedMessage ?: "Unknown error", Toast.LENGTH_SHORT).show()
                release()
            }
        }
    }

    fun setOnCompletionListener(listener: MediaPlayer.OnCompletionListener) {
        mediaPlayer?.setOnCompletionListener(listener)
    }

    fun checkAndSkipOutro() {
        if (!skipTheOutro) {
            return
        }
        currentSong?.let { song ->
            mediaPlayer?.let { player ->
                val currentPosition = player.currentPosition
                val outroTime  = song.duration - song.outroDuration

                if (currentPosition >= outroTime) {
                    player.seekTo(song.duration.toInt())
                }
            }
        }
    }

    fun setSkipTheIntro(context: Context, value: Boolean){
        skipTheIntro = value
        PlayerSettingsManager.saveSettings(context)
    }
    fun setSkipTheOutro(context: Context, value: Boolean){
        skipTheOutro = value
        PlayerSettingsManager.saveSettings(context)
    }
    fun setIsRepeating(context: Context, value: Boolean) {
        isRepeating = value
        PlayerSettingsManager.saveSettings(context)
    }
}