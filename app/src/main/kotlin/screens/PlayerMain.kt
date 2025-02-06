package screens

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
import androidx.fragment.app.Fragment
import com.example.akap.R
import playlistMenu.interfaces.IPlaylist
import playlistMenu.controllers.SongController
import playlistMenu.interfaces.ISong
import playlistMenu.interfaces.SongPlayerListener

class PlayerMain : Fragment() {

    private lateinit var currentSongTitle: TextView
    private lateinit var currentTimeSeekBar: SeekBar
    private lateinit var toggleSettingsButton: Button
    private lateinit var songControlPanel: View
    private var songController: SongController? = null
    private lateinit var localVolumeSeekBar: SeekBar
    private lateinit var skipIntroCheckBox: CheckBox
    private lateinit var skipOutroCheckBox: CheckBox
    private lateinit var pauseAndPlayButton: ImageButton
    private lateinit var currentTimeText: TextView
    private lateinit var nextSongButton: ImageButton

    private var currentPlaylist: IPlaylist? = null
    private var currentSong: ISong? = null
    private var nextSongClickListener: SongPlayerListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is SongPlayerListener) {
            nextSongClickListener = context
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_player, container, false)

        currentSongTitle = view.findViewById(R.id.currentSongTitle)
        currentTimeSeekBar = view.findViewById(R.id.currentTimeSeekBar)
        toggleSettingsButton = view.findViewById(R.id.toggleSettingsButton)
        songControlPanel = view.findViewById(R.id.included_song_control_panel)
        localVolumeSeekBar = view.findViewById(R.id.localVolumeSeekBar)
        skipIntroCheckBox = view.findViewById(R.id.skipIntroCheckBox)
        skipOutroCheckBox = view.findViewById(R.id.skipOutroCheckBox)
        pauseAndPlayButton = view.findViewById(R.id.pauseAndPlayButton)
        currentTimeText = view.findViewById(R.id.currentSongTimeText)
        nextSongButton = view.findViewById(R.id.nextSongButton)

        nextSongButton.setOnClickListener {
            nextSongClickListener?.onNextSongClicked()
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
        return view
    }

    fun updateSongAndPlaylist(context: Context, song: ISong, playlist: IPlaylist) {
        currentSong = song
        currentPlaylist = playlist
        currentSongTitle.text = song.title

        songController?.release()

        songController = SongController(
            context = requireContext(),
            currentSong = song,
            currentPlaylist = playlist,
            currentSongTitle = currentSongTitle,
            currentTimeSeekBar = currentTimeSeekBar,
            localVolumeSeekBar = localVolumeSeekBar,
            skipIntroCheckBox = skipIntroCheckBox,
            skipOutroCheckBox = skipOutroCheckBox,
            pauseAndPlayButton = pauseAndPlayButton,
            currentTimeText = currentTimeText
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        songController?.release()
    }
    override fun onDetach() {
        super.onDetach()
        nextSongClickListener = null
    }
}
