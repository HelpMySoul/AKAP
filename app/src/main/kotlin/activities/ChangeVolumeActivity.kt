package activities

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.FFmpegSession
import com.arthenica.ffmpegkit.ReturnCode
import com.example.akap.R
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

class ChangeVolumeActivity : AppCompatActivity() {

    private var audioUri: Uri? = null
    private var volumeLevel = 1.0 // Уровень громкости по умолчанию (100%)
    private var progressDialog: ProgressDialog? = null
    private var outputPath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_volume)

        val editTextVolume = findViewById<EditText>(R.id.editTextVolume)
        val btnChangeVolume = findViewById<Button>(R.id.btnChangeVolume)

        // Получаем URI аудиофайла из Intent
        val audioUriString = intent.getStringExtra("AUDIO_URI")
        if (audioUriString != null) {
            audioUri = Uri.parse(audioUriString)
            Log.d("ChangeVolumeActivity", "📂 Получен URI: $audioUri")
        } else {
            Log.e("ChangeVolumeActivity", "❌ Ошибка: URI аудиофайла не получен")
            Toast.makeText(this, "Ошибка получения файла", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Инициализация ProgressDialog
        progressDialog = ProgressDialog(this).apply {
            setMessage("Обработка...")
            setCancelable(false)
        }

        // Обработчик нажатия на кнопку "Изменить громкость"
        btnChangeVolume.setOnClickListener {
            val volumeText = editTextVolume.text.toString()
            volumeLevel = volumeText.toDoubleOrNull() ?: 1.0

            // Проверка допустимого диапазона громкости
            if (volumeLevel < 0.0 || volumeLevel > 2.0) {
                Toast.makeText(this, "Громкость должна быть от 0.0 до 2.0", Toast.LENGTH_SHORT).show()
            } else {
                Log.d("ChangeVolumeActivity", "🔊 Новый уровень громкости: $volumeLevel")
                changeVolume()
            }
        }
    }

    /**
     * Изменение громкости аудиофайла с помощью FFmpeg.
     */
    private fun changeVolume() {
        // Копируем файл во внутреннее хранилище
        val inputPath = copyFileToInternalStorage(audioUri!!)
        if (inputPath.isEmpty()) {
            Log.e("ChangeVolumeActivity", "❌ Ошибка: Файл не скопирован в хранилище")
            Toast.makeText(this, "Ошибка копирования файла", Toast.LENGTH_LONG).show()
            return
        }

        // Создаем выходной файл
        val outputFile = File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), "output_volume.mp3")
        outputPath = outputFile.absolutePath

        // Команда FFmpeg для изменения громкости
        val command = if (volumeLevel == 0.0) {
            // Если громкость 0, полностью отключаем звук
            "-y -i \"$inputPath\" -af \"volume=0:enable='eq(n,0)'\" \"$outputPath\""
        } else {
            // Иначе устанавливаем указанный уровень громкости
            "-y -i \"$inputPath\" -af \"volume=$volumeLevel\" \"$outputPath\""
        }

        Log.d("ChangeVolumeActivity", "⏳ Запускаем FFmpegKit с командой: $command")

        // Показываем ProgressDialog
        progressDialog?.show()

        // Выполнение команды FFmpeg асинхронно
        FFmpegKit.executeAsync(command) { session: FFmpegSession ->
            progressDialog?.dismiss()

            if (ReturnCode.isSuccess(session.returnCode)) {
                Log.d("ChangeVolumeActivity", "✅ Громкость изменена")
                showSuccessDialog()
            } else {
                Log.e("ChangeVolumeActivity", "❌ Ошибка FFmpeg: ${session.failStackTrace}")
                runOnUiThread {
                    Toast.makeText(this, "Ошибка изменения громкости", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    /**
     * Показывает диалог успешного завершения.
     */
    private fun showSuccessDialog() {
        runOnUiThread {
            AlertDialog.Builder(this)
                .setTitle("Громкость изменена")
                .setMessage("Файл успешно обработан.")
                .setPositiveButton("ОК") { _, _ ->
                    // Сканируем файл, чтобы он появился в галерее
                    MediaScannerConnection.scanFile(this, arrayOf(outputPath), null, null)

                    // Возвращаем результат в вызывающую активность
                    val resultIntent = Intent().apply {
                        putExtra("OUTPUT_AUDIO", outputPath)
                    }
                    setResult(RESULT_OK, resultIntent)
                    finish()
                }
                .setCancelable(false)
                .show()
        }
    }

    /**
     * Копирует файл из URI во внутреннее хранилище.
     *
     * @param uri URI файла.
     * @return Абсолютный путь к скопированному файлу или пустая строка в случае ошибки.
     */
    private fun copyFileToInternalStorage(uri: Uri): String {
        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null
        return try {
            inputStream = contentResolver.openInputStream(uri)
            if (inputStream == null) {
                Log.e("ChangeVolumeActivity", "❌ Ошибка: inputStream == null")
                return ""
            }

            // Получаем имя файла из URI
            val fileName = getFileName(uri) ?: "input_audio.mp3"

            // Создаем файл во внутреннем хранилище
            val outputFile = File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), fileName)
            outputStream = FileOutputStream(outputFile)

            // Копируем данные
            val buffer = ByteArray(1024)
            var length: Int
            while (inputStream.read(buffer).also { length = it } > 0) {
                outputStream.write(buffer, 0, length)
            }

            Log.d("ChangeVolumeActivity", "📁 Файл скопирован: ${outputFile.absolutePath}")
            outputFile.absolutePath
        } catch (e: Exception) {
            Log.e("ChangeVolumeActivity", "❌ Ошибка копирования файла: ${e.message}")
            ""
        } finally {
            inputStream?.close()
            outputStream?.close()
        }
    }

    /**
     * Получает имя файла из URI.
     *
     * @param uri URI файла.
     * @return Имя файла или null, если не удалось получить.
     */
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
}