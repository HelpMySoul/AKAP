package screens.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Spinner
import androidx.fragment.app.Fragment
import com.example.akap.R
import settings.theme.locale.LanguageManager
import playlistMenu.managers.SongMetadataManager
import settings.theme.ThemeManager

class Settings : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        val languageSpinner: Spinner = view.findViewById(R.id.languageSpinner)
        LanguageManager.setupLanguageSpinner(requireContext(), languageSpinner)

        val themeSpinner: Spinner = view.findViewById(R.id.themeSpinner)
        ThemeManager.setupThemeSpinner(requireContext(), themeSpinner)

        val deleteButton: Button = view.findViewById(R.id.delete_metadata_button)
        deleteButton.setOnClickListener {
            SongMetadataManager.clearAllMetadata(requireContext())
        }

        return view
    }
}
