package playlistMenu.controllers

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import playlistMenu.adapters.TimeAdapter
import playlistMenu.interfaces.IPlaylist
import playlistMenu.interfaces.ISong
import playlistMenu.managers.PlayerSettingsManager
import playlistMenu.managers.PlaylistManager
import playlistMenu.managers.SongManager

@SuppressLint("SetTextI18n")
class SongController(
    private val context:            Context,
    private var currentSong:        ISong?,
    private var currentPlaylist:    IPlaylist,
    private val currentSongTitle:   TextView,
    private val currentTimeText:    TextView,
    private val currentTimeSeekBar: SeekBar,
    private val localVolumeSeekBar: SeekBar,
    private val skipIntroCheckBox:  CheckBox,
    private val skipOutroCheckBox:  CheckBox,
    private val repeatSongCheckBox: CheckBox,
    private val pauseAndPlayButton: ImageButton
) {

    private val handler             = Handler(Looper.getMainLooper())
    private val updateSeekBarTask   = Runnable { updateSongTime() }
    private val checkOutroSkip      = Runnable { outroSkip() }



    init {
        playCurrentSong()
        setupListeners()
    }

    private fun  playCurrentSong() {
        currentSong?.let { playSong(it) }
        startHandler()
    }

    private  fun playSong(song: ISong) {
        PlaylistManager.getSongFromPlaylist(song, currentPlaylist)?.let {
            SongManager.play(context, it)
            currentSong = it
        }
        playNextListenerSetup()

        Handler(Looper.getMainLooper()).postDelayed( {
            currentSongTitle.isSelected = true
            updateUI(true)
        }, 100)

        updateUI()
    }

    private fun setupListeners() {
        currentTimeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                updateSongTimeText(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                SongManager.pause()
                stopHandler()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBar?.let {
                    SongManager.seekTo(it.progress)
                }

                SongManager.unpause()
                updateUI()
                startHandler()
            }
        })

        localVolumeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val volume = progress / 100f
                SongManager.setLocalVolume(volume)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBar?.let {
                    SongManager.saveLocalVolume(context, it.progress)
                }
                updateUI()
            }
        })

        skipIntroCheckBox.setOnCheckedChangeListener { _, isChecked ->
            SongManager.setSkipTheIntro(context, isChecked)
            updateUI()
        }

        skipIntroCheckBox.isChecked = SongManager.skipTheIntro

        skipOutroCheckBox.setOnCheckedChangeListener { _, isChecked ->
            SongManager.setSkipTheOutro(context, isChecked)
            updateUI()
        }

        skipOutroCheckBox.isChecked = SongManager.skipTheOutro

        repeatSongCheckBox.setOnCheckedChangeListener { _, isChecked ->
            SongManager.setIsRepeating(context, isChecked)
            updateUI()
        }

        repeatSongCheckBox.isChecked = SongManager.isRepeating

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
        BroadcastManagerController(context).sendBroadcast("NEXT_SONG")
        playNextListenerSetup()
    }

    private fun playNextListenerSetup() {
        SongManager.setOnCompletionListener(MediaPlayer.OnCompletionListener {
            playNext()
            updateUI(true)
        })
    }

    fun updateUI(updateSongTitle: Boolean = false) {
        if (updateSongTitle) {
            currentSongTitle.text   = "${currentSong?.artist} - ${currentSong?.title}"
        }

        currentTimeSeekBar.max      = currentSong?.duration?.toInt() ?: 0
        currentTimeSeekBar.progress = SongManager.getCurrentPosition()

        currentTimeText.text        = "${TimeAdapter.formatTime(SongManager.getCurrentPosition())} / " +
                "${
                    currentSong?.duration?.let {
                        TimeAdapter.formatTime(it.toInt())
                    }        
                }"

        currentSongTitle.isSelected = true

        if (SongManager.isPlaying) {
            pauseAndPlayButton.setImageResource(android.R.drawable.ic_media_pause)
        } else {
            pauseAndPlayButton.setImageResource(android.R.drawable.ic_media_play)
        }

        localVolumeSeekBar.progress = SongManager.getLocalVolume()
    }



    private fun startHandler() {
        handler.post(updateSeekBarTask)
        handler.post(checkOutroSkip)
    }

    private fun stopHandler() {
        handler.removeCallbacks(updateSeekBarTask)
        handler.removeCallbacks(checkOutroSkip)
    }

    private fun updateSongTime() {
        val currentPosition         = SongManager.getCurrentPosition()
        currentTimeSeekBar.progress = currentPosition

        handler.postDelayed(updateSeekBarTask, 100)

        updateSongTimeText(SongManager.getCurrentPosition())
    }

    private  fun updateSongTimeText(currentTime: Int) {
        currentTimeText.text = "${TimeAdapter.formatTime(currentTime)} / " +
                "${
                    currentSong?.duration?.let {
                        TimeAdapter.formatTime(it.toInt())
                    }
                }"
    }

    private fun outroSkip() {
        SongManager.checkAndSkipOutro()

        handler.postDelayed(checkOutroSkip, 1000)
    }

    fun release() {
        stopHandler()
        SongManager.release()
    }
}
