package playlistMenu.controllers

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import notification.controllers.NotificationController
import notification.services.MediaSessionService
import playlistMenu.adapters.TimeAdapter
import playlistMenu.interfaces.IPlaylist
import playlistMenu.interfaces.ISong
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
        currentSong?.let { setupSong(it) }
        setupListeners()
        updateUI(true)
    }

    fun startPlaying() {
        playCurrentSong()
        val intent = Intent(context, MediaSessionService::class.java).apply {
            putExtra("title", currentSong?.title)
            putExtra("artist", currentSong?.artist)
        }
        context.startService(intent)
    }

    private fun setupSong(song: ISong) {
        PlaylistManager.getSongFromPlaylist(song, currentPlaylist)?.let { playlist ->
            SongManager.setSong(context, playlist)
            currentSong = song
        }
    }

    private fun  playCurrentSong() {
        currentSong?.let { playSong(it) }
        startHandler()
    }

    private fun playSong(song: ISong) {

        SongManager.play(context, song)
        playNextListenerSetup()

        Handler(Looper.getMainLooper()).postDelayed( {
            currentSongTitle.isSelected = true
            updateUI(true)
        }, 33)

        updateUI()
    }

    private fun setupListeners() {


        currentTimeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            var wasPaused: Boolean = false

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                updateSongTimeText(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                wasPaused = SongManager.isPaused
                SongManager.pause()
                stopHandler()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBar?.let {
                    SongManager.seekTo(it.progress)
                }

                if (!wasPaused) {
                    SongManager.unpause()
                }

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
            BroadcastManagerController(context).sendBroadcast("PAUSE_OR_PLAY_SONG")
        }

        playNextListenerSetup()
    }

    private fun playNext() {
        if (SongManager.isRepeating){
            BroadcastManagerController(context).sendBroadcast("REPEAT_SONG")
        } else {
            BroadcastManagerController(context).sendBroadcast("NEXT_SONG")
        }
        playNextListenerSetup()
    }

    private fun playNextListenerSetup() {
        SongManager.setOnCompletionListener(MediaPlayer.OnCompletionListener {
            playNext()
            updateUI(true)
        })
    }

    private fun  getSongName(): String {
        return "${currentSong?.artist} - ${currentSong?.title}"
    }

    fun updateUI(updateSongTitle: Boolean = false) {
        if (updateSongTitle) {
            currentSongTitle.text   = getSongName()
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
        SongManager.checkAndSkipOutro(context)

        handler.postDelayed(checkOutroSkip, 1000)
    }

    fun release() {
        stopHandler()
        SongManager.release()
    }

    fun pauseSong() {
        SongManager.pause()
    }

    fun stopSong() {
        SongManager.stop()
    }

    fun unpauseSong() {
        SongManager.unpause()
    }

    fun setRepeat(checked: Boolean) {
        SongManager.isRepeating = checked
    }

    fun setIntroTime(context: Context) {
        SongManager.setSongIntroTime(context)
    }

    fun setOutroTime(context: Context) {
        SongManager.setSongOutroTime(context)
    }
}
