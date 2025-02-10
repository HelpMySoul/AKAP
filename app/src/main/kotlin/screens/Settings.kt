import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.akap.R
import locale.LanguageManager
import playlistMenu.controllers.BroadcastManagerController
import playlistMenu.managers.SongMetadataManager
import kotlin.system.exitProcess

class Settings : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        val languageSpinner: Spinner = view.findViewById(R.id.languageSpinner)
        LanguageManager.setupLanguageSpinner(requireContext(), languageSpinner)

        val deleteButton: Button = view.findViewById(R.id.delete_metadata_button)
        deleteButton.setOnClickListener {
            SongMetadataManager.clearAllMetadata(requireContext())
        }

        return view
    }
}
