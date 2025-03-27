package activities

import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import com.example.akap.R
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.io.IOException

class VocalProcessingActivity : AppCompatActivity() {

    private var file: File? = null
    private lateinit var processedFile: File // Инициализируем позже
    private val selectedEffects = mutableListOf<String>() // Список выбранных эффектов

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vocal_processing)

        // Инициализация processedFile после инициализации контекста
        processedFile = File(filesDir, "processed_audio.wav")

        // Получаем Uri из Intent
        val audioUriString = intent.getStringExtra("AUDIO_URI")
        val audioUri = Uri.parse(audioUriString)

        // Проверяем, что Uri не null
        if (audioUri == null) {
            Toast.makeText(this, "Ошибка: Uri не найден", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Обрабатываем Uri
        val filePath = getFilePathFromUri(audioUri)
        file = File(filePath)

        val btnSelectFile = findViewById<Button>(R.id.btnSelectFile)
        val btnAddReverb = findViewById<Button>(R.id.btnAddReverb)
        val btnAddCompression = findViewById<Button>(R.id.btnAddCompression)
        val btnAddEqualizer = findViewById<Button>(R.id.btnAddEqualizer)
        val btnApplyProcessing = findViewById<Button>(R.id.btnApplyProcessing)

        // Выбор файла
        btnSelectFile.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "audio/*"
            startActivityForResult(intent, REQUEST_CODE_SELECT_FILE)
        }

        // Добавление реверберации
        btnAddReverb.setOnClickListener {
            selectedEffects.add("aecho=0.8:0.88:60:0.4")
            Snackbar.make(it, "Реверберация добавлена", Snackbar.LENGTH_SHORT).show()
        }

        // Добавление компрессии
        btnAddCompression.setOnClickListener {
            selectedEffects.add("compand=.3,.7,.5,-5,-80,0.2")
            Snackbar.make(it, "Компрессия добавлена", Snackbar.LENGTH_SHORT).show()
        }

        // Добавление эквалайзера
        btnAddEqualizer.setOnClickListener {
            selectedEffects.add("equalizer=f=1000:t=q:w=1:g=5")
            Snackbar.make(it, "Эквалайзер добавлен", Snackbar.LENGTH_SHORT).show()
        }

        // Обработка файла
        btnApplyProcessing.setOnClickListener {
            if (file != null && selectedEffects.isNotEmpty()) {
                applyVocalProcessing(file!!, selectedEffects)
            } else {
                Snackbar.make(it, "Выберите файл и добавьте эффекты", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_SELECT_FILE) {
            data?.data?.let { uri ->
                // Получаем реальный путь к файлу
                val filePath = getFilePathFromUri(uri)
                file = File(filePath)
                Toast.makeText(this, "Файл выбран: ${file?.name}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getFilePathFromUri(uri: Uri): String {
        // Используем ContentResolver для получения реального пути
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val displayName = it.getString(it.getColumnIndexOrThrow("_display_name"))
                return File(cacheDir, displayName).absolutePath
            }
        }
        return uri.path ?: throw IllegalArgumentException("Не удалось получить путь к файлу")
    }

    private fun applyVocalProcessing(inputFile: File, effects: List<String>) {
        try {
            val outputFile = processedFile.absolutePath
            val effectsString = effects.joinToString(",") // Объединяем эффекты через запятую
            val command = "-i ${inputFile.absolutePath} -af $effectsString $outputFile"

            FFmpegKit.executeAsync(command) { session ->
                val returnCode = session.returnCode
                runOnUiThread {
                    if (ReturnCode.isSuccess(returnCode)) {
                        Toast.makeText(this@VocalProcessingActivity, "Обработка завершена", Toast.LENGTH_SHORT).show()
                        startProcessedFilePlayback()
                    } else {
                        Toast.makeText(this@VocalProcessingActivity, "Ошибка обработки: код $returnCode", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Ошибка обработки: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startProcessedFilePlayback() {
        val mediaPlayer = MediaPlayer()
        try {
            mediaPlayer.setDataSource(processedFile.absolutePath)
            mediaPlayer.prepare()
            mediaPlayer.start()

            // Освобождаем ресурсы после завершения воспроизведения
            mediaPlayer.setOnCompletionListener {
                mediaPlayer.release()
            }
        } catch (e: IOException) {
            Toast.makeText(this, "Ошибка воспроизведения: ${e.message}", Toast.LENGTH_SHORT).show()
            mediaPlayer.release() // Освобождаем ресурсы в случае ошибки
        }
    }

    companion object {
        private const val REQUEST_CODE_SELECT_FILE = 101
    }
}