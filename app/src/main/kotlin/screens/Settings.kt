import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Spinner
import androidx.fragment.app.Fragment
import com.example.akap.R
import locale.LanguageManager

class Settings : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        val languageSpinner: Spinner = view.findViewById(R.id.languageSpinner)

        LanguageManager.setupLanguageSpinner(requireContext(), languageSpinner)

        return view
    }
}
