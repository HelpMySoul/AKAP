package screens

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.akap.R
import activities.TrimAudioActivity

class Tools : Fragment() {

    private var selectedAudioUri: Uri? = null

    // Лаунчер для выбора аудиофайла
    private lateinit var pickAudioLauncher: ActivityResultLauncher<Intent>

    // Лаунчер для получения результата от TrimAudioActivity
    private lateinit var trimAudioLauncher: ActivityResultLauncher<Intent>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_tools, container, false)

        val btnTrim = view.findViewById<Button>(R.id.btnTrim)
        val btnMerge = view.findViewById<Button>(R.id.btnMerge)
        val btnSplit = view.findViewById<Button>(R.id.btnSplit)
        val btnVolume = view.findViewById<Button>(R.id.btnVolume)
        val btnConvert = view.findViewById<Button>(R.id.btnConvert)
        val btnEditTags = view.findViewById<Button>(R.id.btnEditTags)
        val btnEqualizer = view.findViewById<Button>(R.id.btnEqualizer)
        val btnVocalProcessing = view.findViewById<Button>(R.id.btnVocalProcessing)

        // Инициализация лаунчеров
        setupActivityResultLaunchers()

        btnTrim.setOnClickListener { pickAudioFile() }

        btnMerge.setOnClickListener { showToast("Функция объединения в разработке") }
        btnSplit.setOnClickListener { showToast("Функция разделения в разработке") }
        btnVolume.setOnClickListener { showToast("Функция изменения громкости в разработке") }
        btnConvert.setOnClickListener { showToast("Функция конвертации в разработке") }
        btnEditTags.setOnClickListener { showToast("Функция редактирования тегов в разработке") }
        btnEqualizer.setOnClickListener { showToast("Функция эквалайзера в разработке") }
        btnVocalProcessing.setOnClickListener { showToast("Функция обработки вокала в разработке") }

        return view
    }

    private fun setupActivityResultLaunchers() {
        // Лаунчер выбора аудиофайла
        pickAudioLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.data
                if (uri != null) {
                    selectedAudioUri = uri
                    openTrimAudioActivity()
                } else {
                    showToast("Файл не выбран")
                }
            }
        }

        // Лаунчер для обрезки аудио
        trimAudioLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val trimmedAudioPath = result.data?.getStringExtra("TRIMMED_AUDIO")
                showToast("Аудио обрезано: $trimmedAudioPath")
            }
        }
    }

    private fun pickAudioFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "audio/*"
        }
        pickAudioLauncher.launch(intent)
    }

    private fun openTrimAudioActivity() {
        selectedAudioUri?.let { uri ->
            val intent = Intent(requireContext(), TrimAudioActivity::class.java)
            intent.putExtra("AUDIO_URI", uri.toString())
            trimAudioLauncher.launch(intent)
        } ?: showToast("Сначала выберите аудиофайл")
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}
