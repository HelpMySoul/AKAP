package screens.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.akap.R
import global.GlobalManager
import player.managers.SongManager
import settings.theme.locale.LanguageManager
import player.managers.SongMetadataManager
import settings.player.PlayerSettingsManager
import settings.theme.ThemeManager

class Settings : Fragment() {
    @SuppressLint("UseSwitchCompatOrMaterialCode", "SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        val languageSpinner: Spinner = view.findViewById(R.id.languageSpinner)
        LanguageManager.setupLanguageSpinner(requireContext(), languageSpinner)

        val themeSpinner: Spinner = view.findViewById(R.id.themeSpinner)
        ThemeManager.setupThemeSpinner(requireContext(), themeSpinner)

        val outroSwitch: Switch = view.findViewById(R.id.outroSkipSwitch)
        outroSwitch.isChecked   = SongManager.autoOutroSkip

        outroSwitch.setOnClickListener {
            context?.let { context -> SongManager.setAutoOutroSkip(context, outroSwitch.isChecked) }
        }

        val outroSeekBar: SeekBar = view.findViewById(R.id.outroSkipSeekBar)

        outroSeekBar.max      = 100
        outroSeekBar.progress = GlobalManager.outroSkipPercent

        val outroTimeText: TextView = view.findViewById(R.id.outroTimeText)

        outroTimeText.text = "${GlobalManager.outroSkipPercent}%"

        outroSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                outroTimeText.text = "$progress%"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                GlobalManager.outroSkipPercent = outroSeekBar.progress
                outroTimeText.text = "${GlobalManager.outroSkipPercent}%"
                context?.let { PlayerSettingsManager.saveSettings(it) }
            }
        })

        val deleteButton: Button = view.findViewById(R.id.delete_metadata_button)
        deleteButton.setOnClickListener {
            SongMetadataManager.clearAllMetadata(requireContext())
        }

        return view
    }
}
