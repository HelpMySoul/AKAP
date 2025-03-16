package screens

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.akap.R
import activities.MergeAudioActivity
import activities.TrimAudioActivity
import activities.SplitAudioActivity
import activities.ChangeVolumeActivity
import activities.ConvertAudioActivity

class Tools : Fragment() {

    private var selectedAudioUri: Uri? = null
    private val selectedAudioUris = mutableListOf<Uri>()
    private var isSplitRequest = false

    private lateinit var pickAudioLauncher: ActivityResultLauncher<Intent>
    private lateinit var pickMultipleAudioLauncher: ActivityResultLauncher<Intent>
    private lateinit var pickVolumeAudioLauncher: ActivityResultLauncher<Intent>
    private lateinit var trimAudioLauncher: ActivityResultLauncher<Intent>
    private lateinit var mergeAudioLauncher: ActivityResultLauncher<Intent>
    private lateinit var splitAudioLauncher: ActivityResultLauncher<Intent>
    private lateinit var changeVolumeLauncher: ActivityResultLauncher<Intent>
    private lateinit var convertAudioLauncher: ActivityResultLauncher<Intent>



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

        setupActivityResultLaunchers()

        btnTrim.setOnClickListener { pickAudioFileForTrim() }
        btnMerge.setOnClickListener { pickMultipleAudioFilesForMerge() }
        btnSplit.setOnClickListener { pickAudioFileForSplit() }
        btnVolume.setOnClickListener { pickAudioFileForVolumeChange() }
        btnConvert.setOnClickListener { pickAudioFileForConversion() }
        btnEditTags.setOnClickListener { showToast("Функция редактирования тегов в разработке") }
        btnEqualizer.setOnClickListener { showToast("Функция эквалайзера в разработке") }
        btnVocalProcessing.setOnClickListener { showToast("Функция обработки вокала в разработке") }

        return view
    }

    private fun setupActivityResultLaunchers() {
        pickAudioLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    selectedAudioUri = uri
                    requireContext().contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                    Log.d("Tools", "📂 Выбранный файл: $uri")
                    if (isSplitRequest) {
                        openSplitAudioActivity()
                    } else {
                        openTrimAudioActivity()
                    }
                } ?: showToast("⚠️ Файл не выбран")
            }
        }

        pickVolumeAudioLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    selectedAudioUri = uri
                    requireContext().contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                    Log.d("Tools", "📂 Выбранный файл для громкости: $uri")
                    openChangeVolumeActivity()
                } ?: showToast("⚠️ Файл не выбран")
            }
        }

        convertAudioLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    selectedAudioUri = uri
                    requireContext().contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                    Log.d("Tools", "📂 Выбранный файл для конвертации: $uri")
                    openConvertAudioActivity()
                } ?: showToast("⚠️ Файл не выбран")
            }
        }


        trimAudioLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val trimmedAudioPath = result.data?.getStringExtra("TRIMMED_AUDIO")
                showToast("✂️ Аудио обрезано: $trimmedAudioPath")
            }
        }

        pickMultipleAudioLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                selectedAudioUris.clear()

                result.data?.clipData?.let { clipData ->
                    for (i in 0 until clipData.itemCount) {
                        selectedAudioUris.add(clipData.getItemAt(i).uri)
                    }
                } ?: result.data?.data?.let { singleUri ->
                    selectedAudioUris.add(singleUri)
                }

                if (selectedAudioUris.size > 1) {
                    openMergeAudioActivity()
                } else {
                    showToast("⚠️ Выберите минимум 2 файла")
                }
            }
        }


        mergeAudioLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                showToast("🔀 Аудио успешно объединено")
            }
        }

        splitAudioLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                showToast("✂️ Аудио успешно разделено")
            }
        }

        changeVolumeLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                showToast("🔊 Громкость успешно изменена")
            }
        }

        convertAudioLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                showToast("🎵 Аудио успешно конвертировано")
            }
        }

    }



    private fun pickAudioFileForTrim() {
        isSplitRequest = false
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "audio/*"
        }
        pickAudioLauncher.launch(intent)
    }

    private fun pickMultipleAudioFilesForMerge() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "audio/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        pickMultipleAudioLauncher.launch(intent)
    }

    private fun pickAudioFileForSplit() {
        isSplitRequest = true
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "audio/*"
        }
        pickAudioLauncher.launch(intent)
    }

    private fun pickAudioFileForVolumeChange() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "audio/*"
        }
        pickVolumeAudioLauncher.launch(intent)
    }

    private fun pickAudioFileForConversion() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "audio/*"
        }
        convertAudioLauncher.launch(intent)
    }


    private fun openTrimAudioActivity() {
        selectedAudioUri?.let { uri ->
            val intent = Intent(requireContext(), TrimAudioActivity::class.java).apply {
                putExtra("AUDIO_URI", uri.toString())
            }
            trimAudioLauncher.launch(intent)
        } ?: showToast("⚠️ Сначала выберите аудиофайл")
    }

    private fun openMergeAudioActivity() {
        val intent = Intent(requireContext(), MergeAudioActivity::class.java).apply {
            putParcelableArrayListExtra("AUDIO_URIS", ArrayList(selectedAudioUris))
        }
        mergeAudioLauncher.launch(intent)
    }

    private fun openSplitAudioActivity() {
        selectedAudioUri?.let { uri ->
            val intent = Intent(requireContext(), SplitAudioActivity::class.java).apply {
                putExtra("AUDIO_URI", uri.toString())
            }
            splitAudioLauncher.launch(intent)
        } ?: showToast("⚠️ Сначала выберите аудиофайл")
    }

    private fun openChangeVolumeActivity() {
        selectedAudioUri?.let { uri ->
            val intent = Intent(requireContext(), ChangeVolumeActivity::class.java).apply {
                putExtra("AUDIO_URI", uri)
            }
            changeVolumeLauncher.launch(intent)
        } ?: showToast("⚠️ Сначала выберите аудиофайл")
    }

    private fun openConvertAudioActivity() {
        selectedAudioUri?.let { uri ->
            val intent = Intent(requireContext(), ConvertAudioActivity::class.java).apply {
                putExtra("AUDIO_URI", uri.toString())
            }
            convertAudioLauncher.launch(intent)
        } ?: showToast("⚠️ Сначала выберите аудиофайл")
    }


    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}
