package screens.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.akap.R
import player.interfaces.ISong
import settings.songSettingsCard.SongSettingsController

class SongSettingsCard (private val song: ISong, private  val fragmentRefresh: () -> Unit): Fragment() {

    private lateinit var changeNameSettingButton:      Button
    private lateinit var changeAuthorSettingButton:    Button
    private lateinit var discardMetaDataSettingButton: Button

    private lateinit var songNameSettingCard:   TextView
    private lateinit var songAuthorSettingCard: TextView


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.song_setting_card, container, false)

        changeNameSettingButton      = view.findViewById(R.id.changeNameSettingButton)
        changeAuthorSettingButton    = view.findViewById(R.id.changeAuthorSettingButton)
        discardMetaDataSettingButton = view.findViewById(R.id.discardMetaDataSettingButton)

        songNameSettingCard   = view.findViewById(R.id.songNameSettingCard)
        songAuthorSettingCard = view.findViewById(R.id.songAuthorSettingCard)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        songNameSettingCard.text   = song.title
        songAuthorSettingCard.text = song.artist

        changeNameSettingButton.setOnClickListener {
            SongSettingsController.changeSongName(requireContext(), song) { refresh() }
        }

        changeAuthorSettingButton.setOnClickListener {
            SongSettingsController.changeAuthor(requireContext(), song) { refresh() }
        }

        discardMetaDataSettingButton.setOnClickListener {
            SongSettingsController.discardMetaData(requireContext(), song)
        }
    }

    private fun refresh() {
        songNameSettingCard.text   = song.title
        songAuthorSettingCard.text = song.artist

        fragmentRefresh.invoke()
    }
}