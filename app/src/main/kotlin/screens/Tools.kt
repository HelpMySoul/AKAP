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
import activities.EditTagsActivity

class Tools : Fragment() {

    private var selectedAudioUri: Uri? = null
    private val selectedAudioUris = mutableListOf<Uri>()
    private var isSplitRequest = false

    private lateinit var pickAudioLauncher: ActivityResultLauncher<Intent>
    private lateinit var pickMultipleAudioLauncher: ActivityResultLauncher<Intent>
    private lateinit var pickVolumeAudioLauncher: ActivityResultLauncher<Intent>

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
        val btnEditTags = view.findViewById<Button>(R.id.btnEditTags) // Кнопка редактирования тегов

        setupActivityResultLaunchers()

        btnTrim.setOnClickListener { pickAudioFileForTrim() }
        btnMerge.setOnClickListener { pickMultipleAudioFilesForMerge() }
        btnSplit.setOnClickListener { pickAudioFileForSplit() }
        btnVolume.setOnClickListener { pickAudioFileForVolumeChange() }
        btnConvert.setOnClickListener { openConvertAudioActivity() }
        btnEditTags.setOnClickListener { openEditTagsActivity() } // Прямой переход к редактированию тегов

        return view
    }

    private fun setupActivityResultLaunchers() {
        pickAudioLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    selectedAudioUri = uri
                    requireContext().contentResolver.takePersistableUriPermission(
                        uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                    Log.d("Tools", "📂 Выбранный файл: $uri")
                    if (isSplitRequest) {
                        openSplitAudioActivity()
                    } else {
                        openTrimAudioActivity()
                    }
                } ?: run {
                    Log.e("Tools", "⚠️ Файл не выбран")
                    showToast("⚠️ Файл не выбран")
                }
            } else {
                Log.e("Tools", "⚠️ Результат не RESULT_OK")
            }
        }

        pickVolumeAudioLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    selectedAudioUri = uri
                    requireContext().contentResolver.takePersistableUriPermission(
                        uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                    Log.d("Tools", "📂 Выбранный файл для громкости: $uri")
                    openChangeVolumeActivity()
                } ?: run {
                    Log.e("Tools", "⚠️ Файл не выбран")
                    showToast("⚠️ Файл не выбран")
                }
            } else {
                Log.e("Tools", "⚠️ Результат не RESULT_OK")
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
                    Log.e("Tools", "⚠️ Выберите минимум 2 файла")
                    showToast("⚠️ Выберите минимум 2 файла")
                }
            } else {
                Log.e("Tools", "⚠️ Результат не RESULT_OK")
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

    private fun openTrimAudioActivity() {
        selectedAudioUri?.let { uri ->
            Log.d("Tools", "✂️ Запуск TrimAudioActivity с файлом: $uri")
            val intent = Intent(requireContext(), TrimAudioActivity::class.java).apply {
                putExtra("AUDIO_URI", uri.toString())
            }
            startActivity(intent)
        } ?: run {
            Log.e("Tools", "⚠️ Сначала выберите аудиофайл")
            showToast("⚠️ Сначала выберите аудиофайл")
        }
    }

    private fun openMergeAudioActivity() {
        if (selectedAudioUris.size > 1) {
            Log.d("Tools", "🎵 Запуск MergeAudioActivity с ${selectedAudioUris.size} файлами")
            val intent = Intent(requireContext(), MergeAudioActivity::class.java).apply {
                putParcelableArrayListExtra("AUDIO_URIS", ArrayList(selectedAudioUris))
            }
            startActivity(intent)
        } else {
            Log.e("Tools", "⚠️ Выберите минимум 2 файла")
            showToast("⚠️ Выберите минимум 2 файла")
        }
    }

    private fun openSplitAudioActivity() {
        selectedAudioUri?.let { uri ->
            Log.d("Tools", "✂️ Запуск SplitAudioActivity с файлом: $uri")
            val intent = Intent(requireContext(), SplitAudioActivity::class.java).apply {
                putExtra("AUDIO_URI", uri.toString())
            }
            startActivity(intent)
        } ?: run {
            Log.e("Tools", "⚠️ Сначала выберите аудиофайл")
            showToast("⚠️ Сначала выберите аудиофайл")
        }
    }

    private fun openChangeVolumeActivity() {
        selectedAudioUri?.let { uri ->
            Log.d("Tools", "🔊 Запуск ChangeVolumeActivity с файлом: $uri")
            val intent = Intent(requireContext(), ChangeVolumeActivity::class.java).apply {
                putExtra("AUDIO_URI", uri.toString())
            }
            startActivity(intent)
        } ?: run {
            Log.e("Tools", "⚠️ Сначала выберите аудиофайл")
            showToast("⚠️ Сначала выберите аудиофайл")
        }
    }

    private fun openEditTagsActivity() {
        // Убрана проверка на selectedAudioUri
        Log.d("Tools", "🏷️ Запуск EditTagsActivity")
        val intent = Intent(requireContext(), EditTagsActivity::class.java)
        startActivity(intent)
    }

    private fun openConvertAudioActivity() {
        Log.d("Tools", "🎵 Запуск ConvertAudioActivity")
        val intent = Intent(requireContext(), ConvertAudioActivity::class.java)
        startActivity(intent)
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}