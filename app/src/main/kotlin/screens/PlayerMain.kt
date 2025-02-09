package screens

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.akap.R
import playlistMenu.adapters.TimeAdapter
import playlistMenu.controllers.BroadcastManagerController
import playlistMenu.interfaces.IPlaylist
import playlistMenu.controllers.SongController
import playlistMenu.interfaces.ISong
import playlistMenu.interfaces.ISongPlayerListener
import playlistMenu.managers.GlobalManager
import playlistMenu.managers.PlayerSettingsManager
import playlistMenu.managers.SongManager

class PlayerMain : Fragment() {
    private lateinit var currentSongTitle:  TextView
    private lateinit var currentTimeText:   TextView

    private lateinit var currentTimeSeekBar: SeekBar
    private lateinit var localVolumeSeekBar: SeekBar

    private lateinit var toggleSettingsButton:  Button
    private lateinit var shuffleSongButton:     Button
    private lateinit var setIntroButton:        Button
    private lateinit var setOutroButton:        Button

    private lateinit var pauseAndPlayButton:    ImageButton
    private lateinit var nextSongButton:        ImageButton

    private lateinit var songControlPanel: View

    private var songController: SongController? = null

    private lateinit var skipIntroCheckBox:  CheckBox
    private lateinit var skipOutroCheckBox:  CheckBox
    private lateinit var repeatSongCheckBox: CheckBox

    private var currentPlaylist:        IPlaylist?           = null
    private var currentSong:            ISong?               = null
    private var playerClickListener:    ISongPlayerListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is ISongPlayerListener) {
            playerClickListener = context
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_player, container, false)

        currentSongTitle        = view.findViewById(R.id.currentSongTitle)
        currentTimeSeekBar      = view.findViewById(R.id.currentTimeSeekBar)
        toggleSettingsButton    = view.findViewById(R.id.toggleSettingsButton)
        songControlPanel        = view.findViewById(R.id.included_song_control_panel)
        localVolumeSeekBar      = view.findViewById(R.id.localVolumeSeekBar)
        skipIntroCheckBox       = view.findViewById(R.id.skipIntroCheckBox)
        skipOutroCheckBox       = view.findViewById(R.id.skipOutroCheckBox)
        pauseAndPlayButton      = view.findViewById(R.id.pauseAndPlayButton)
        currentTimeText         = view.findViewById(R.id.currentSongTimeText)
        nextSongButton          = view.findViewById(R.id.nextSongButton)
        repeatSongCheckBox      = view.findViewById(R.id.repeatCheckBox)
        shuffleSongButton       = view.findViewById(R.id.shuffleSongButton)
        setIntroButton          = view.findViewById(R.id.introSkipButton)
        setOutroButton          = view.findViewById(R.id.skipOutroButton)

        nextSongButton.setOnClickListener {
            BroadcastManagerController(requireContext()).sendBroadcast("NEXT_SONG")
        }

        shuffleSongButton.setOnClickListener {
            shuffleCurrentPlaylist()
        }

        repeatSongCheckBox.setOnClickListener {
            SongManager.isRepeating = repeatSongCheckBox.isChecked
            PlayerSettingsManager.saveSettings(requireContext())
        }

        setIntroButton.setOnClickListener {
            SongManager.setSongIntroTime(requireContext())
            PlayerSettingsManager.saveSettings(requireContext())
        }

        setOutroButton.setOnClickListener {
            SongManager.setSongOutroTime(requireContext())
            PlayerSettingsManager.saveSettings(requireContext())
        }

        toggleSettingsButton.setOnClickListener {
            if (songControlPanel.visibility == View.VISIBLE) {
                songControlPanel.visibility = View.GONE
                toggleSettingsButton.text = getString(R.string.show_settings)
            } else {
                songControlPanel.visibility = View.VISIBLE
                toggleSettingsButton.text = getString(R.string.hide_settings)
            }
        }

        toggleSettingsButton.performClick()
        return view
    }

     fun nextSong() {
         if (currentPlaylist == null) {
            Toast.makeText(requireContext(), requireContext().getString(R.string.No_Playlist), Toast.LENGTH_SHORT).show()
            return
         }
         if (SongManager.isRepeating) {
             currentPlaylist?.getCurrentSong()?.let {
                 updateSongAndPlaylist(requireContext(), it, currentPlaylist!!)
             }
         } else {
             currentPlaylist?.getNext().let {
                 if (it != null) {
                     updateSongAndPlaylist(requireContext(), it, currentPlaylist!!)
                 }
             }
         }

    }
    fun playSong() {
        songController?.startPlaying()
    }

    private fun shuffleCurrentPlaylist() {
        if (currentPlaylist == null) {
            Toast.makeText(requireContext(), requireContext().getString(R.string.No_Playlist), Toast.LENGTH_SHORT).show()
            return
        }
        currentPlaylist?.shuffle()

        currentPlaylist!!.getFirstSong()?.let { updateSongAndPlaylist(requireContext(), it, currentPlaylist!!) }

        BroadcastManagerController(requireContext()).sendBroadcast("SHUFFLE_PLAYLIST")
    }

    fun updateSongAndPlaylist(context: Context, song: ISong?, playlist: IPlaylist) {
        if (song != null) {
            updateSong(song, playlist)
            updateSongController(song, playlist)
        }
    }

    @SuppressLint("SetTextI18n")
    fun updateSong(song: ISong?, playlist: IPlaylist) {
        currentSong     = song
        currentPlaylist = playlist

        currentPlaylist?.getCurrentSong()?.let { GlobalManager.updateSongID(it.id, requireContext()) }
    }

    private fun updateSongController(song: ISong?, playlist: IPlaylist) {
        songController?.release()

        songController = SongController(
            context             = requireContext(),
            currentSong         = song,
            currentPlaylist     = playlist,
            currentSongTitle    = currentSongTitle,
            currentTimeSeekBar  = currentTimeSeekBar,
            localVolumeSeekBar  = localVolumeSeekBar,
            skipIntroCheckBox   = skipIntroCheckBox,
            skipOutroCheckBox   = skipOutroCheckBox,
            pauseAndPlayButton  = pauseAndPlayButton,
            currentTimeText     = currentTimeText,
            repeatSongCheckBox  = repeatSongCheckBox
        )
    }
    override fun onDestroy() {
        super.onDestroy()
        songController?.release()
    }
    override fun onDetach() {
        super.onDetach()
        playerClickListener = null
    }
}
