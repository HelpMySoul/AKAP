package activities

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import com.example.akap.R
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class MergeAudioActivity : AppCompatActivity() {

    private lateinit var progressBar: ProgressBar
    private lateinit var textViewStatus: TextView
    private lateinit var buttonMerge: Button

    private var audioUris: List<Uri>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_merge_audio)

        progressBar = findViewById(R.id.progressBar)
        textViewStatus = findViewById(R.id.textViewStatus)
        buttonMerge = findViewById(R.id.buttonMerge)

        audioUris = intent.getParcelableArrayListExtra("AUDIO_URIS")

        buttonMerge.setOnClickListener {
            if (audioUris.isNullOrEmpty()) {
                textViewStatus.text = "Файлы не выбраны!"
            } else {
                mergeAudioFiles(audioUris!!)
            }
        }
    }

    private fun mergeAudioFiles(audioUris: List<Uri>) {
        progressBar.visibility = View.VISIBLE
        textViewStatus.text = "Идёт объединение аудио..."

        val inputFiles = audioUris.mapNotNull { uri ->
            val file = uriToFile(uri)
            file?.absolutePath
        }

        if (inputFiles.isEmpty()) {
            runOnUiThread {
                textViewStatus.text = "Ошибка: Файлы не найдены!"
                progressBar.visibility = View.GONE
            }
            return
        }

        val outputFile = getOutputFile()
        val outputFilePath = outputFile.absolutePath
        val ffmpegCommand = "-y -i \"concat:${inputFiles.joinToString("|")}\" -acodec copy \"$outputFilePath\""

        FFmpegKit.executeAsync(ffmpegCommand) { session ->
            val returnCode = session.returnCode

            runOnUiThread {
                progressBar.visibility = View.GONE
                if (ReturnCode.isSuccess(returnCode)) {
                    textViewStatus.text = "Объединение завершено!"
                    saveToMediaStore(outputFile)
                    val intent = Intent().apply {
                        putExtra("MERGED_AUDIO_PATH", outputFilePath)
                    }
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                } else {
                    textViewStatus.text = "Ошибка при объединении!"
                }
            }
        }
    }

    private fun uriToFile(uri: Uri): File? {
        val file = File(cacheDir, "temp_${System.currentTimeMillis()}.mp3")
        return try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            val outputStream = FileOutputStream(file)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getOutputFile(): File {
        val musicDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC), "MergedAudio")
        if (!musicDir.exists()) {
            musicDir.mkdirs()
        }
        return File(musicDir, "merged_audio.mp3")
    }

    private fun saveToMediaStore(file: File) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Audio.Media.DISPLAY_NAME, file.name)
                put(MediaStore.Audio.Media.MIME_TYPE, "audio/mp3")
                put(MediaStore.Audio.Media.RELATIVE_PATH, Environment.DIRECTORY_MUSIC + "/MergedAudio")
                put(MediaStore.Audio.Media.IS_PENDING, 1)
            }

            val uri = contentResolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values)
            uri?.let {
                contentResolver.openOutputStream(it)?.use { outputStream ->
                    file.inputStream().use { inputStream -> inputStream.copyTo(outputStream) }
                }
                values.clear()
                values.put(MediaStore.Audio.Media.IS_PENDING, 0)
                contentResolver.update(uri, values, null, null)
            }
        }
    }
}
