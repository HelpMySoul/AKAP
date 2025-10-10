package player.managers

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.example.akap.R
import broadcast.BroadcastManagerController
import global.GlobalManager
import player.interfaces.ISong
import settings.player.PlayerSettingsManager
import java.io.IOException

object SongManager {
    private var mediaPlayer: MediaPlayer? = null
    private var currentSong: ISong?       = null

    var isPaused:      Boolean = true
    var canPlay:       Boolean = false
    var isRepeating:   Boolean = false
    var skipTheIntro:  Boolean = false
    var skipTheOutro:  Boolean = false
    var autoOutroSkip: Boolean = false
    var jumpToStart:   Boolean = false

    fun play(context: Context, song: ISong) {
        mediaPlayer?.setOnPreparedListener {
            it.start()
            canPlay  = true
            isPaused = false
            setLocalVolume(song.localVolume.toFloat())
            if (skipTheIntro) {
                it.seekTo(song.introDuration.toInt())
            }
        }
        PlayerSettingsManager.saveSettings(context)
    }

    fun setSong(context: Context, song: ISong) {
        Log.e("SongManager", "Current song: ${song.title}")
        if (mediaPlayer?.isPlaying == true) {
            stop()
        }
        try {
            initializeMediaPlayer(context, song)
            currentSong = song
        }
        catch (e: Exception) {
            Toast.makeText(context, e.localizedMessage, Toast.LENGTH_SHORT).show()
            currentSong = null
        }
    }

    fun unpause(context: Context) {
        if (canPlay) {
            mediaPlayer?.let { player ->
                if (isPaused) {
                    player.start()
                    isPaused = false
                    canPlay = true
                }
            } ?: run {
                Log.e("SongManager", "MediaPlayer is not initialized in unpause")
            }
        }
        else {
            currentSong?.let { play(context, it) }
        }
    }

    fun pause() {
        mediaPlayer?.let { player ->
            if (!isPaused) {
                player.pause()
                isPaused = true
            }
        } ?: run {
            Log.e("SongManager", "MediaPlayer is not initialized in pause")
        }
    }

    fun stop() {
        mediaPlayer?.let { player ->
            if (canPlay) {
                player.pause()
                player.stop()
                isPaused = true
                canPlay = false
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

    fun setCurrentSongIntroTime(context: Context) {
        if (!canPlay) {
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

    fun setSongIntroTime(context: Context, value: Int) {
        if (!canPlay) {
            Toast.makeText(context, context.getString(R.string.no_song_playing), Toast.LENGTH_SHORT).show()
            return
        }

        val song = currentSong ?: run {
            Toast.makeText(context, context.getString(R.string.no_song_playing), Toast.LENGTH_SHORT).show()
            return
        }

        if (value.toLong() >= song.duration - song.outroDuration) {
            Toast.makeText(context, context.getString(R.string.too_late_intro), Toast.LENGTH_SHORT).show()
            return
        }

        song.introDuration = value.toLong()
        SongMetadataManager.saveMetadata(context, song)
        Toast.makeText(context, context.getString(R.string.intro_set_successfully), Toast.LENGTH_SHORT).show()
    }

    fun setCurrentSongOutroTime(context: Context) {
        if (!canPlay) {
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
    }

    fun setSongOutroTime(context: Context, value: Int) {
        if (!canPlay) {
            Toast.makeText(context, context.getString(R.string.no_song_playing), Toast.LENGTH_SHORT).show()
            return
        }

        val song = currentSong ?: run {
            Toast.makeText(context, context.getString(R.string.no_song_playing), Toast.LENGTH_SHORT).show()
            return
        }

        if (value.toLong() <= song.introDuration) {
            Toast.makeText(context, context.getString(R.string.too_early_outro), Toast.LENGTH_SHORT).show()
            return
        }

        song.outroDuration = song.duration - value.toLong()
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
        mediaPlayer = null
        canPlay     = false
        isPaused    = true
    }

    private fun initializeMediaPlayer(context: Context, song: ISong) {
        mediaPlayer = MediaPlayer().apply {
            setDataSource(context, Uri.parse(song.filePath))
            prepare()
        }
    }

    fun setOnCompletionListener(listener: MediaPlayer.OnCompletionListener) {
        mediaPlayer?.setOnCompletionListener(null)
        mediaPlayer?.setOnCompletionListener(listener)
    }

    fun checkAndSkipOutro(context: Context) {
        if (!skipTheOutro) {
            return
        }
        currentSong?.let { song ->
            mediaPlayer?.let { player ->
                val currentPosition = player.currentPosition
                val outroTime       = song.duration - song.outroDuration

                if (outroTime != song.duration && currentPosition > outroTime) {
                    if (isRepeating){
                        BroadcastManagerController(context).sendBroadcast("REPEAT_SONG")
                    } else {
                        BroadcastManagerController(context).sendBroadcast("NEXT_SONG")
                    }
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

    fun setAutoOutroSkip(context: Context, value: Boolean) {
        autoOutroSkip = value
        PlayerSettingsManager.saveSettings(context)
    }

    fun setJumpToStartSwitch(context: Context, value: Boolean) {
        jumpToStart = value
        PlayerSettingsManager.saveSettings(context)
    }

    fun getMaxPosition(): Int {
        return mediaPlayer?.duration ?: 0
    }


}