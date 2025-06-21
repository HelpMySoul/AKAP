package player.controllers

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.widget.CheckBox
import android.widget.SeekBar
import android.widget.TextView
import broadcast.BroadcastManagerController
import global.GlobalManager
import notification.services.NotificationService
import player.adapters.TimeAdapter
import player.interfaces.ISong
import player.managers.SongManager
import player.managers.SongMetadataManager
import player.services.MusicFinderService

@SuppressLint("SetTextI18n")
class SongController(
    private val context:            Context,
    private val currentSongTitle:   TextView,
    private val currentTimeText:    TextView,
    private val currentTimeSeekBar: SeekBar,
    private val localVolumeSeekBar: SeekBar,
    private val skipIntroCheckBox:  CheckBox,
    private val skipOutroCheckBox:  CheckBox,
    private val repeatSongCheckBox: CheckBox
) {

    private val handler           = Handler(Looper.getMainLooper())
    private val updateSeekBarTask = Runnable { updateSongTime() }
    private val checkOutroSkip    = Runnable { outroSkip() }

    private val currentSong: ISong
        get() {
            return MusicFinderService(context).findSongById(GlobalManager.getSongID())
        }

    init {
        setupListeners()
        updateUI(true)
    }

    fun startPlaying() {
        playCurrentSong()
        createNotificationIntent()
        BroadcastManagerController(context).sendBroadcast("SHOW_PLAYER")
    }

    fun setupSong() {
        currentSong.let { SongManager.setSong(context, it) }
    }

    private fun playCurrentSong() {
        currentSong.let { SongManager.play(context, it) }
        playNextListenerSetup()

        Handler(Looper.getMainLooper()).postDelayed( {
            currentSongTitle.isSelected = true
            updateUI(true)
        }, 33)

        updateUI()

        startHandler()
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
                    SongManager.unpause(context)
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
        playNextListenerSetup()
    }

    private fun playNext() {
        if (SongManager.isRepeating) {
            BroadcastManagerController(context).sendBroadcast("REPEAT_SONG")
        } else {
            BroadcastManagerController(context).sendBroadcast("NEXT_SONG")
        }
        playNextListenerSetup() 
    }

    private fun playNextListenerSetup() {
        SongManager.setOnCompletionListener {
            playNext()
            updateUI(true)
        }
    }

    private fun  getSongName(): String {
        return "${currentSong.artist} - ${currentSong?.title}"
    }

    fun updateUI(updateSongTitle: Boolean = false) {
        if (updateSongTitle) {
            currentSongTitle.text = getSongName()
        }

        currentTimeSeekBar.max      = currentSong.duration.toInt()
        currentTimeSeekBar.progress = SongManager.getCurrentPosition()
        currentTimeText.text        = "${TimeAdapter.formatTime(SongManager.getCurrentPosition())} / " +
            currentSong.duration.let {
                TimeAdapter.formatTime(it.toInt())
            }

        currentSongTitle.isSelected = true
        localVolumeSeekBar.progress = SongManager.getLocalVolume()
    }

    private fun startHandler() {
        stopHandler()
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
            currentSong.duration.let {
                TimeAdapter.formatTime(it.toInt())
            }
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
        SongManager.unpause(context)
        startHandler()

        createNotificationIntent()
    }

    private fun createNotificationIntent() {
        val intent = Intent(context, NotificationService::class.java).apply {
            putExtra("title", currentSong.title)
            putExtra("artist", currentSong.artist)
            putExtra("show",true)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    fun setRepeat(checked: Boolean) {
        SongManager.isRepeating = checked
    }

    fun setCurrentIntroTime(context: Context) {
        SongManager.setCurrentSongIntroTime(context)
    }

    fun setIntroTime(int: Int) {
        SongManager.setSongIntroTime(context, int)
    }

    fun setOutroTime(int: Int) {
        SongManager.setSongOutroTime(context, int)
    }

    fun setCurrentOutroTime(context: Context) {
        SongManager.setCurrentSongOutroTime(context)
    }


    fun getCurrentTime(): Int {
       return SongManager.getCurrentPosition()
    }

    fun getMaxTime(): Int {
        return SongManager.getMaxPosition()
    }

}
