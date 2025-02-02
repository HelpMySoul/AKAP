package playlistMenu.songControl

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.CheckBox
import android.widget.SeekBar
import android.widget.TextView
import playlistMenu.interfaces.ISong

@SuppressLint("SetTextI18n")
class SongController(
    context: Context,
    currentSong: ISong,
    currentSongTitle: TextView,
    currentTimeSeekBar: SeekBar,
    globalVolumeSeekBar: SeekBar,
    localVolumeSeekBar: SeekBar,
    skipIntroCheckBox: CheckBox,
    skipOutroCheckBox: CheckBox,
    playButton: Button,
    pauseButton: Button,
    stopButton: Button
) {

    private val handler = Handler(Looper.getMainLooper())
    private val updateSeekBarTask = Runnable { updateSeekBar(currentTimeSeekBar) }

    init {
        SongManager.play(context, currentSong)

        currentSongTitle.text = "${currentSong.artist} - ${currentSong.title}"
        currentTimeSeekBar.max = currentSong.duration.toInt()

        currentTimeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    SongManager.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        globalVolumeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                SongManager.setGlobalVolume(context, progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        localVolumeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                SongManager.setLocalVolume(currentSong, progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        skipIntroCheckBox.setOnCheckedChangeListener { _, isChecked ->
            SongManager.skipTheIntro = isChecked
        }

        skipOutroCheckBox.setOnCheckedChangeListener { _, isChecked ->
            SongManager.skipTheOutro = isChecked
        }

        playButton.setOnClickListener { SongManager.play(context, currentSong) }
        pauseButton.setOnClickListener { SongManager.pause() }
        stopButton.setOnClickListener { SongManager.stop() }

        startUpdatingSeekBar()
    }

    private fun startUpdatingSeekBar() {
        handler.post(updateSeekBarTask)
    }

    private fun stopUpdatingSeekBar() {
        handler.removeCallbacks(updateSeekBarTask)
    }

    private fun updateSeekBar(seekBar: SeekBar) {
        val currentPosition = SongManager.getCurrentPosition()
        seekBar.progress = currentPosition

        handler.postDelayed(updateSeekBarTask, 1000)
    }


    fun release() {
        stopUpdatingSeekBar()
        SongManager.release()
    }
}