package playlistMenu.controllers

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import playlistMenu.adapters.SongAdapter
import playlistMenu.interfaces.IPlaylist
import playlistMenu.interfaces.ISong
import playlistMenu.managers.PlaylistManager
import playlistMenu.managers.SongManager

@SuppressLint("SetTextI18n")
class SongController(
    private val context: Context,
    private var currentSong: ISong,
    private var currentPlaylist: IPlaylist,
    private val currentSongTitle: TextView,
    private val currentTimeSeekBar: SeekBar,
    private val localVolumeSeekBar: SeekBar,
    private val skipIntroCheckBox: CheckBox,
    private val skipOutroCheckBox: CheckBox,
    private val pauseAndPlayButton: ImageButton,
    private val currentTimeText: TextView
) {

    private val handler = Handler(Looper.getMainLooper())
    private val updateSeekBarTask = Runnable { updateSongTime() }

    init {
        PlaylistManager.getSongFromPlaylist(currentSong, currentPlaylist)?.let {
            SongManager.play(context, it)
            currentSong = it

            playNextListenerSetup()

            Handler(Looper.getMainLooper()).postDelayed({
                updateUI(true)
            }, 100)
        }

        Handler(Looper.getMainLooper()).postDelayed( {
            currentSongTitle.isSelected = true
        }, 100)

        Log.e("SongController", "Index: ${currentPlaylist.getIndex()}")
        updateUI()

        setupListeners()

        startUpdatingSeekBar()
    }

    private fun setupListeners() {
        currentTimeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                updateSongTimeText(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                SongManager.pause()
                stopUpdatingSeekBar()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBar?.let {
                    SongManager.seekTo(it.progress)
                }
                SongManager.unpause()
                updateUI()
                startUpdatingSeekBar()
            }
        })

        localVolumeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val volume = progress / 100f
                SongManager.setLocalVolume(volume)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                updateUI()
            }
        })

        skipIntroCheckBox.setOnCheckedChangeListener { _, isChecked ->
            SongManager.skipTheIntro = isChecked
            updateUI()
        }

        skipOutroCheckBox.setOnCheckedChangeListener { _, isChecked ->
            SongManager.skipTheOutro = isChecked
            updateUI()
        }
        
        pauseAndPlayButton.setOnClickListener {
            if (SongManager.isPlaying) {
                SongManager.pause()
            } else {
                SongManager.unpause()
            }
            updateUI()
        }


        playNextListenerSetup()
    }

    private fun playNext() {
        val nextSong = PlaylistManager.getNextSong(currentPlaylist)
        if (nextSong == null) {
            SongManager.stop()
        } else {

            SongManager.play(context, nextSong)
            currentSong = nextSong

            playNextListenerSetup()
        }
    }

    private fun playNextListenerSetup() {
        SongManager.setOnCompletionListener(MediaPlayer.OnCompletionListener {
            playNext()
            updateUI(true)
        })
    }



    private fun updateUI(updateSongTitle: Boolean = false) {
        if (updateSongTitle) {
            currentSongTitle.text = "${currentSong.artist} - ${currentSong.title}"
        }

        currentTimeSeekBar.max = currentSong.duration.toInt()
        currentTimeSeekBar.progress = SongManager.getCurrentPosition()

        currentTimeText.text = "${formatTime(SongManager.getCurrentPosition())} / ${formatTime(currentSong.duration.toInt())}"

        currentSongTitle.isSelected = true

        if (SongManager.isPlaying) {
            pauseAndPlayButton.setImageResource(android.R.drawable.ic_media_pause)
        } else {
            pauseAndPlayButton.setImageResource(android.R.drawable.ic_media_play)
        }
    }




    private fun formatTime(milliseconds: Int): String {
        val seconds = (milliseconds / 1000) % 60
        val minutes = (milliseconds / 1000) / 60
        return if (minutes > 0) {
            "$minutes:${seconds.toString().padStart(2, '0')}"
        } else {
            "$seconds"
        }
    }

    private fun startUpdatingSeekBar() {
        handler.post(updateSeekBarTask)
    }

    private fun stopUpdatingSeekBar() {
        handler.removeCallbacks(updateSeekBarTask)
    }

    private fun updateSongTime() {
        val currentPosition = SongManager.getCurrentPosition()
        currentTimeSeekBar.progress = currentPosition

        handler.postDelayed(updateSeekBarTask, 100)

        updateSongTimeText(SongManager.getCurrentPosition())
    }

    private  fun updateSongTimeText(currentTime: Int) {
        currentTimeText.text = "${formatTime(currentTime)} / ${formatTime(currentSong.duration.toInt())}"
    }
    fun release() {
        stopUpdatingSeekBar()
        SongManager.release()
    }
}
