package screens.fragments

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
import broadcast.BroadcastManagerController
import builders.SetTimeBuilder
import playlistMenu.interfaces.IPlaylist
import playlistMenu.controllers.SongController
import playlistMenu.interfaces.ISong
import settings.player.PlayerSettingsManager
import playlistMenu.managers.SongManager

class PlayerMain : Fragment() {
    private lateinit var currentSongTitle: TextView
    private lateinit var currentTimeText:  TextView

    private lateinit var currentTimeSeekBar: SeekBar
    private lateinit var localVolumeSeekBar: SeekBar

    private lateinit var toggleSettingsButton: Button
    private lateinit var shuffleSongButton:    Button
    private lateinit var setIntroButton:       Button
    private lateinit var setOutroButton:       Button

    private lateinit var pauseAndPlayButton: ImageButton
    private lateinit var nextSongButton:     ImageButton

    private lateinit var songControlPanel: View

    private var songController: SongController? = null

    private lateinit var skipIntroCheckBox:  CheckBox
    private lateinit var skipOutroCheckBox:  CheckBox
    private lateinit var repeatSongCheckBox: CheckBox

    private var currentPlaylist: IPlaylist? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_player, container, false)

        currentSongTitle     = view.findViewById(R.id.currentSongTitle)
        currentTimeSeekBar   = view.findViewById(R.id.currentTimeSeekBar)
        toggleSettingsButton = view.findViewById(R.id.toggleSettingsButton)
        songControlPanel     = view.findViewById(R.id.included_song_control_panel)
        localVolumeSeekBar   = view.findViewById(R.id.localVolumeSeekBar)
        skipIntroCheckBox    = view.findViewById(R.id.skipIntroCheckBox)
        skipOutroCheckBox    = view.findViewById(R.id.skipOutroCheckBox)
        pauseAndPlayButton   = view.findViewById(R.id.pauseAndPlayButton)
        currentTimeText      = view.findViewById(R.id.currentSongTimeText)
        nextSongButton       = view.findViewById(R.id.nextSongButton)
        repeatSongCheckBox   = view.findViewById(R.id.repeatCheckBox)
        shuffleSongButton    = view.findViewById(R.id.shuffleSongButton)
        setIntroButton       = view.findViewById(R.id.introSkipButton)
        setOutroButton       = view.findViewById(R.id.skipOutroButton)

        nextSongButton.setOnClickListener {
            BroadcastManagerController(requireContext()).sendBroadcast("NEXT_SONG")
        }

        shuffleSongButton.setOnClickListener {
            shuffleCurrentPlaylist()
        }

        repeatSongCheckBox.setOnClickListener {
            songController?.setRepeat(repeatSongCheckBox.isChecked)
            PlayerSettingsManager.saveSettings(requireContext())
        }

        setIntroButton.setOnClickListener {
            makeBuilder { newTime ->
                songController?.setIntroTime(newTime)
                PlayerSettingsManager.saveSettings(requireContext())
            }
        }

        setOutroButton.setOnClickListener {
            makeBuilder { newTime ->
                songController?.setOutroTime(newTime)
                PlayerSettingsManager.saveSettings(requireContext())
            }
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
        pauseAndPlayButton.setOnClickListener {
            BroadcastManagerController(requireContext()).sendBroadcast("PAUSE_OR_PLAY_SONG")
        }

        toggleSettingsButton.performClick()
        return view
    }

    private fun makeBuilder(onChange : (Int) -> Unit){
        val currentTime = songController?.getCurrentTime()  ?: 0
        val maxTime     = songController?.getMaxTime()      ?: 0

        val builder =  SetTimeBuilder(
            context     = requireContext(),
            currentTime = currentTime,
            maxTime     = maxTime,
            onChange    = onChange
        ).built()

        return builder
    }

     fun nextSong() {
         if (currentPlaylist == null) {
            Toast.makeText(requireContext(), requireContext().getString(R.string.no_playlist), Toast.LENGTH_SHORT).show()
            return
         }

         currentPlaylist?.getNext().let { song ->
             if (song != null) {
                 updateSongAndPlaylist(requireContext(), song, currentPlaylist!!)
             }
         }
    }

    fun repeatSong() {
        if (SongManager.isRepeating) {
            currentPlaylist?.getCurrentSong()?.let { song ->
                updateSongAndPlaylist(requireContext(), song, currentPlaylist!!)
            }
        }
    }

    fun prevSong() {
        if (currentPlaylist == null) {
            Toast.makeText(requireContext(), requireContext().getString(R.string.no_playlist), Toast.LENGTH_SHORT).show()
            return
        }

        currentPlaylist?.getBefore().let { song ->
            if (song != null) {
                updateSongAndPlaylist(requireContext(), song, currentPlaylist!!)
            }
        }
    }

    fun playSong() {
        songController?.startPlaying()
        pauseAndPlayButton.setImageResource(android.R.drawable.ic_media_pause)
    }

    private fun shuffleCurrentPlaylist() {
        if (currentPlaylist == null) {
            Toast.makeText(requireContext(), requireContext().getString(R.string.no_playlist), Toast.LENGTH_SHORT).show()
            return
        }

        BroadcastManagerController(requireContext()).sendBroadcast("SHUFFLE_PLAYLIST")
    }

    fun updateSongAndPlaylist(context: Context, song: ISong?, playlist: IPlaylist?) {
        if (song != null) {
            currentPlaylist = playlist
            if (playlist != null) {
                updateSongController(song, playlist)
            }
            else {
                songController?.release()
            }
        }
    }

    private fun updateSongController(song: ISong?, playlist: IPlaylist) {
        if (songController == null) {
            songController = SongController(
                context            = requireContext(),
                currentSong        = song,
                currentPlaylist    = playlist,
                currentSongTitle   = currentSongTitle,
                currentTimeText    = currentTimeText,
                currentTimeSeekBar = currentTimeSeekBar,
                localVolumeSeekBar = localVolumeSeekBar,
                skipIntroCheckBox  = skipIntroCheckBox,
                skipOutroCheckBox  = skipOutroCheckBox,
                repeatSongCheckBox = repeatSongCheckBox
            )
        } else {
            songController?.updateSong(song, playlist)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        songController?.release()
    }

    fun pauseSong() {
        songController?.pauseSong()
        pauseAndPlayButton.setImageResource(android.R.drawable.ic_media_play)
    }

    fun stopSong() {
        songController?.stopSong()
        pauseAndPlayButton.setImageResource(android.R.drawable.ic_media_play)
    }

    fun closePlaylist() {
        currentPlaylist = null
        songController?.release()
    }

    fun unpauseSong() {
        if (!SongManager.canPlay) {
            songController?.setSong()
        }

        songController?.unpauseSong()
        pauseAndPlayButton.setImageResource(android.R.drawable.ic_media_pause)
    }
}
