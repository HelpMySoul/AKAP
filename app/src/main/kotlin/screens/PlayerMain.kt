package screens

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.akap.R
import playlistMenu.songControl.SongController
import playlistMenu.interfaces.ISong
import java.io.IOException

class PlayerMain : Fragment() {

    private lateinit var currentSongTitle: TextView
    private lateinit var currentTimeSeekBar: SeekBar
    private lateinit var toggleSettingsButton: Button
    private lateinit var songControlPanel: View
    private var songController: SongController? = null
    private lateinit var globalVolumeSeekBar: SeekBar
    private lateinit var localVolumeSeekBar: SeekBar
    private lateinit var skipIntroCheckBox: CheckBox
    private lateinit var skipOutroCheckBox: CheckBox
    private lateinit var playButton: Button
    private lateinit var pauseButton: Button
    private lateinit var stopButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_player, container, false)

        currentSongTitle = view.findViewById(R.id.currentSongTitle)
        currentTimeSeekBar = view.findViewById(R.id.currentTimeSeekBar)
        toggleSettingsButton = view.findViewById(R.id.toggleSettingsButton)
        songControlPanel = view.findViewById(R.id.included_song_control_panel)
        globalVolumeSeekBar = view.findViewById(R.id.globalVolumeSeekBar)
        localVolumeSeekBar = view.findViewById(R.id.localVolumeSeekBar)
        skipIntroCheckBox = view.findViewById(R.id.skipIntroCheckBox)
        skipOutroCheckBox = view.findViewById(R.id.skipOutroCheckBox)
        playButton = view.findViewById(R.id.playButton)
        pauseButton = view.findViewById(R.id.pauseButton)
        stopButton = view.findViewById(R.id.stopButton)

        toggleSettingsButton.setOnClickListener {
            if (songControlPanel.visibility == View.VISIBLE) {
                songControlPanel.visibility = View.GONE
                toggleSettingsButton.text = getString(R.string.show_settings)
            } else {
                songControlPanel.visibility = View.VISIBLE
                toggleSettingsButton.text = getString(R.string.hide_settings)
            }
        }

        return view
    }

    fun updateSong( context: Context, song: ISong) {
        currentSongTitle.text = song.title

        songController?.release()

        songController = SongController(
            context = requireContext(),
            currentSong = song,
            currentSongTitle = currentSongTitle,
            currentTimeSeekBar = currentTimeSeekBar,
            globalVolumeSeekBar = globalVolumeSeekBar,
            localVolumeSeekBar = localVolumeSeekBar,
            skipIntroCheckBox = skipIntroCheckBox,
            skipOutroCheckBox = skipOutroCheckBox,
            playButton = playButton,
            pauseButton = pauseButton,
            stopButton = stopButton


        )
    }

    override fun onDestroy() {
        super.onDestroy()
        songController?.release()
    }
}