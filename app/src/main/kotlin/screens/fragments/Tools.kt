package screens.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build

import android.os.Bundle
import android.os.Environment
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
import activities.*

class Tools : Fragment() {

    private var selectedAudioUri: Uri? = null
    private val selectedAudioUris = mutableListOf<Uri>()
    private var isSplitRequest = false

    private lateinit var pickAudioLauncher: ActivityResultLauncher<Intent>
    private lateinit var pickMultipleAudioLauncher: ActivityResultLauncher<Intent>
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var requestManageStorageLauncher: ActivityResultLauncher<Intent>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_tools, container, false)

        val btnTrim = view.findViewById<Button>(R.id.btnTrim)
        val btnMerge = view.findViewById<Button>(R.id.btnMerge)
        val btnSplit = view.findViewById<Button>(R.id.btnSplit)
        val btnConvert = view.findViewById<Button>(R.id.btnConvert)
        val btnEditTags = view.findViewById<Button>(R.id.btnEditTags)
        val btnEqualizer = view.findViewById<Button>(R.id.btnEqualizer)
        val btnRecord = view.findViewById<Button>(R.id.btnRecord)

        setupActivityResultLaunchers()

        btnTrim.setOnClickListener { pickAudioFileForTrim() }
        btnMerge.setOnClickListener { pickMultipleAudioFilesForMerge() }
        btnSplit.setOnClickListener { pickAudioFileForSplit() }
        btnConvert.setOnClickListener { openConvertAudioActivity() }
        btnEditTags.setOnClickListener { openEditTagsActivity() }
        btnEqualizer.setOnClickListener { openEqualizerActivity() }
        btnRecord.setOnClickListener { openRecordAudioActivity() }

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

        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                Log.d("Tools", "Разрешение предоставлено")
            } else {
                showToast("⚠️ Разрешение на чтение файлов не предоставлено")
            }
        }

        requestManageStorageLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Environment.isExternalStorageManager()) {
                Log.d("Tools", "Доступ к хранилищу предоставлен")
            } else {
                showToast("⚠️ Разрешение на доступ ко всем файлам не предоставлено")
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

    private fun openTrimAudioActivity() {
        selectedAudioUri?.let { uri ->
            Log.d("Tools", "✂️ Запуск TrimAudioActivity с файлом: $uri")
            val intent = Intent(requireContext(), TrimAudioActivity::class.java).apply {
                putExtra("AUDIO_URI", uri.toString())
            }
            startActivity(intent)
        } ?: showToast("⚠️ Сначала выберите аудиофайл")
    }

    private fun openMergeAudioActivity() {
        if (selectedAudioUris.size > 1) {
            Log.d("Tools", "🎵 Запуск MergeAudioActivity с ${selectedAudioUris.size} файлами")
            val intent = Intent(requireContext(), MergeAudioActivity::class.java).apply {
                putParcelableArrayListExtra("AUDIO_URIS", ArrayList(selectedAudioUris))
            }
            startActivity(intent)
        } else {
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
        } ?: showToast("⚠️ Сначала выберите аудиофайл")
    }

    private fun openEditTagsActivity() {
        Log.d("Tools", "🏷️ Запуск EditTagsActivity")
        val intent = Intent(requireContext(), EditTagsActivity::class.java)
        startActivity(intent)
    }

    private fun openConvertAudioActivity() {
        Log.d("Tools", "🎵 Запуск ConvertAudioActivity")
        val intent = Intent(requireContext(), ConvertAudioActivity::class.java)
        startActivity(intent)
    }

    private fun openEqualizerActivity() {
        Log.d("Tools", "🎚️ Запуск EqualizerActivity")
        val intent = Intent(requireContext(), EqualizerActivity::class.java)
        startActivity(intent)
    }

    private fun openRecordAudioActivity() {
        Log.d("Tools", "🎙️ Запуск RecordAudioActivity")
        val intent = Intent(requireContext(), RecordAudioActivity::class.java)
        startActivity(intent)
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}