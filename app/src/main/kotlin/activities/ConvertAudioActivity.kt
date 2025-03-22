package activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.documentfile.provider.DocumentFile
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import com.example.akap.R
import java.io.File

class ConvertAudioActivity : AppCompatActivity() {

    private var selectedAudioUri: Uri? = null
    private lateinit var formatSpinner: Spinner
    private lateinit var btnSelectFolder: Button
    private lateinit var btnConvert: Button
    private lateinit var tvStatus: TextView
    private lateinit var pickFileLauncher: ActivityResultLauncher<Intent>
    private var outputFolderUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_convert_audio)

        formatSpinner = findViewById(R.id.formatSpinner)
        btnSelectFolder = findViewById(R.id.btnSelectFolder)
        btnConvert = findViewById(R.id.btnConvert)
        tvStatus = findViewById(R.id.tvStatus)
        val btnSelectFile = findViewById<Button>(R.id.btnSelectFile)

        // Настройка Spinner для выбора формата
        val formats = arrayOf("MP3", "WAV", "AAC", "FLAC")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, formats)
        formatSpinner.adapter = adapter

        btnSelectFolder.setOnClickListener { selectOutputFolder() }
        btnConvert.setOnClickListener { convertAudio() }
        btnSelectFile.setOnClickListener { pickAudioFile() }

        pickFileLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    selectedAudioUri = uri
                    Log.d("ConvertAudioActivity", "📂 Выбранный файл: $uri")
                    tvStatus.text = "Выбран файл: ${getFileName(uri)}"
                } ?: showToast("⚠️ Файл не выбран")
            }
        }
    }

    private fun selectOutputFolder() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        startActivityForResult(intent, REQUEST_CODE_FOLDER)
    }

    private fun pickAudioFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "audio/*"
        }
        pickFileLauncher.launch(intent)
    }

    private fun convertAudio() {
        if (selectedAudioUri == null || outputFolderUri == null) {
            showToast("Выберите файл и папку")
            return
        }

        val inputFile = copyFileToCache(selectedAudioUri!!) ?: run {
            showToast("Ошибка при обработке файла")
            return
        }

        val selectedFormat = formatSpinner.selectedItem.toString().lowercase()
        val outputFileName = "converted_audio.$selectedFormat"
        val tempOutputFile = File(cacheDir, outputFileName)

        tvStatus.text = "🔄 Конвертация в $selectedFormat..."

        val command = when (selectedFormat) {
            "mp3" -> "-y -i \"${inputFile.absolutePath}\" -codec:a libmp3lame -q:a 2 \"${tempOutputFile.absolutePath}\""
            "wav" -> "-y -i \"${inputFile.absolutePath}\" -codec:a pcm_s16le \"${tempOutputFile.absolutePath}\""
            "aac" -> "-y -i \"${inputFile.absolutePath}\" -codec:a aac -b:a 192k \"${tempOutputFile.absolutePath}\""
            "flac" -> "-y -i \"${inputFile.absolutePath}\" -codec:a flac -compression_level 12 \"${tempOutputFile.absolutePath}\""
            else -> {
                showToast("⚠️ Неподдерживаемый формат")
                return
            }
        }

        FFmpegKit.executeAsync(command) { session ->
            if (ReturnCode.isSuccess(session.returnCode)) {
                runOnUiThread {
                    saveFileToSelectedFolder(tempOutputFile, outputFileName)
                }
            } else {
                runOnUiThread {
                    tvStatus.text = "❌ Ошибка конвертации"
                    showToast("Ошибка: ${session.failStackTrace}")
                }
            }
            inputFile.delete()
        }
    }

    private fun saveFileToSelectedFolder(tempFile: File, fileName: String) {
        try {
            outputFolderUri?.let { folderUri ->
                val pickedDir = DocumentFile.fromTreeUri(this, folderUri)
                if (pickedDir == null || !pickedDir.canWrite()) {
                    showToast("⚠️ Ошибка: нет доступа к папке")
                    return
                }

                val newFile = pickedDir.createFile("audio/*", fileName)
                if (newFile == null) {
                    showToast("⚠️ Ошибка: не удалось создать файл")
                    return
                }

                contentResolver.openOutputStream(newFile.uri)?.use { outputStream ->
                    tempFile.inputStream().copyTo(outputStream)
                }

                tvStatus.text = "✅ Файл сохранен!"
                showToast("Файл сохранен: ${newFile.uri}")
                Log.d("ConvertAudioActivity", "✅ Файл сохранен в папке: ${newFile.uri}")

            } ?: showToast("⚠️ Ошибка: папка не выбрана")
        } catch (e: Exception) {
            tvStatus.text = "❌ Ошибка сохранения файла"
            showToast("Ошибка сохранения файла")
            Log.e("ConvertAudioActivity", "Ошибка при сохранении файла", e)
        }
    }

    private fun copyFileToCache(uri: Uri): File? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val fileName = getFileName(uri) ?: "temp_audio_file"
            val file = File(cacheDir, fileName)
            file.outputStream().use { outputStream ->
                inputStream?.copyTo(outputStream)
            }
            file
        } catch (e: Exception) {
            Log.e("ConvertAudioActivity", "Ошибка копирования файла", e)
            null
        }
    }

    private fun getFileName(uri: Uri): String? {
        var displayName: String? = null
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    displayName = cursor.getString(nameIndex)
                }
            }
        }
        return displayName
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_FOLDER && resultCode == Activity.RESULT_OK) {
            outputFolderUri = data?.data
            showToast("📁 Папка выбрана")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val REQUEST_CODE_FOLDER = 1001
    }
}
