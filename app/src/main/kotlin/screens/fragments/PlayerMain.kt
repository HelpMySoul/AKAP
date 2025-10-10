package screens.fragments

import android.os.Bundle
import android.util.Log
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
import builders.InfoBuilder
import builders.SetTimeBuilder
import global.GlobalManager
import player.controllers.PlaylistController
import player.interfaces.IPlaylist
import player.controllers.SongController
import player.interfaces.ISong
import settings.player.PlayerSettingsManager
import player.managers.SongManager

class PlayerMain : Fragment() {
    private lateinit var currentSongTitle: TextView
    private lateinit var currentTimeText:  TextView

    private lateinit var currentTimeSeekBar: SeekBar
    private lateinit var localVolumeSeekBar: SeekBar

    private lateinit var toggleSettingsButton: Button

    private lateinit var setIntroButton: ImageButton
    private lateinit var setOutroButton: ImageButton

    private lateinit var introInfoButton: ImageButton
    private lateinit var outroInfoButton: ImageButton

    private lateinit var pauseAndPlayButton: ImageButton
    private lateinit var nextSongButton:     ImageButton

    private lateinit var songControlPanel: View

    private var songController: SongController? = null

    private lateinit var skipIntroCheckBox:  CheckBox
    private lateinit var skipOutroCheckBox:  CheckBox
    private lateinit var repeatSongCheckBox: CheckBox

    private val currentPlaylist: IPlaylist?
        get() {
            return PlaylistController(requireContext()).getPlaylist(GlobalManager.getPlayedPlaylistName())
        }

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
        setIntroButton       = view.findViewById(R.id.introSkipButton)
        setOutroButton       = view.findViewById(R.id.skipOutroButton)
        introInfoButton      = view.findViewById(R.id.infoIntroButton)
        outroInfoButton      = view.findViewById(R.id.infoOutroButton)

        currentSongTitle.setOnClickListener {
            BroadcastManagerController(requireContext()).sendBroadcast("SHOW_CURRENT_SONG")
        }

        nextSongButton.setOnClickListener {
            BroadcastManagerController(requireContext()).sendBroadcast("NEXT_SONG")
        }

        repeatSongCheckBox.setOnClickListener {
            songController?.setRepeat(repeatSongCheckBox.isChecked)
            PlayerSettingsManager.saveSettings(requireContext())
        }

        setIntroButton.setOnClickListener {
            context?.let {
                context -> setTimeBuilderToListener(context.getString(R.string.edit_intro_time)) { newTime ->
                    songController?.setIntroTime(newTime)
                }
            }
        }

        setOutroButton.setOnClickListener {
            context?.let {
                context -> setTimeBuilderToListener(context.getString(R.string.edit_outro_time)) { newTime ->
                    songController?.setOutroTime(newTime)
                }
            }
        }

        outroInfoButton.setOnClickListener {
            makeInfoBuilder(context?.getString(R.string.outro_info))
        }

        introInfoButton.setOnClickListener {
            makeInfoBuilder(context?.getString(R.string.intro_info))
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

    private fun makeInfoBuilder(string: String?) {
        context?.let {
            if (string != null) {
                InfoBuilder(it, string).built()
            }
        }
    }

    private fun setTimeBuilderToListener(title: String, action: (Int) -> Unit) {
        if (SongManager.canPlay)
        {
            makeTimeBuilder(title) { newTime ->
                action.invoke(newTime)
                PlayerSettingsManager.saveSettings(requireContext())
            }
        }
        else {
            Toast.makeText(context, R.string.no_song_playing, Toast.LENGTH_SHORT).show()
        }
    }

    private fun makeTimeBuilder(title: String, onChange : (Int) -> Unit){
        val currentTime = songController?.getCurrentTime()  ?: 0
        val maxTime     = songController?.getMaxTime()      ?: 0

        val builder =  SetTimeBuilder(
            title       = title,
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

         val currentTime: Int = songController?.getCurrentTime() ?: 0

         Log.e("PlayerMain", "${SongManager.autoOutroSkip} ${currentTime.toFloat() / songController?.getMaxTime()!!.toFloat()} ${GlobalManager.outroSkipPercent.toFloat()/100f}")

         if (SongManager.autoOutroSkip &&
             currentTime.toFloat() / songController?.getMaxTime()!!.toFloat() > GlobalManager.outroSkipPercent.toFloat()/100){
             context?.let { songController!!.setCurrentOutroTime(requireContext())
             }
         }
         songControllerCheck(currentPlaylist?.getNext())
    }

    fun repeatSong() {
        if (SongManager.isRepeating) {
            updateSongController()
        }
    }

    fun prevSong() {
        if (currentPlaylist == null) {
            Toast.makeText(requireContext(), requireContext().getString(R.string.no_playlist), Toast.LENGTH_SHORT).show()
            return
        }

        val currentTime: Int = songController?.getCurrentTime() ?: 0

        if (SongManager.jumpToStart &&
            currentTime.toFloat() / songController?.getMaxTime()!!.toFloat() > GlobalManager.jumpToStartPercent.toFloat()/100)
        {
            songControllerCheck(currentPlaylist?.getCurrentSong())
        }
        else
        {
            songControllerCheck(currentPlaylist?.getBefore())
        }
    }

    private fun songControllerCheck(song: ISong?) {
        if (song != null) {
            GlobalManager.updateSongID(song.id, requireContext())
            updateSongController()
        }
    }

    fun playSong() {
        songController?.startPlaying()
        pauseAndPlayButton.setImageResource(R.drawable.pause)
    }

    fun updateSongController() {
        if (songController == null) {
            songController = SongController(
                context            = requireContext(),
                currentSongTitle   = currentSongTitle,
                currentTimeText    = currentTimeText,
                currentTimeSeekBar = currentTimeSeekBar,
                localVolumeSeekBar = localVolumeSeekBar,
                skipIntroCheckBox  = skipIntroCheckBox,
                skipOutroCheckBox  = skipOutroCheckBox,
                repeatSongCheckBox = repeatSongCheckBox
            )
        }
        songController!!.setupSong()
        songController?.updateUI()
    }

    fun pauseSong() {
        songController?.pauseSong()
        pauseAndPlayButton.setImageResource(R.drawable.play)
    }

    fun stopSong() {
        BroadcastManagerController(requireContext()).sendBroadcast("HIDE_PLAYER")
        songController?.stopSong()
        pauseAndPlayButton.setImageResource(R.drawable.play)
    }

    fun closePlaylist() {
        songController?.release()
    }

    fun unpauseSong() {
        if (!SongManager.canPlay) {
            songController?.setupSong()
        }

        songController?.unpauseSong()
        pauseAndPlayButton.setImageResource(R.drawable.pause)
    }
}
