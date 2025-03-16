package activities

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
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
    private var volumeLevel = 1.0 // Default 1.0 (100%)
    private var progressDialog: ProgressDialog? = null
    private var outputPath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_volume)

        val editTextVolume = findViewById<EditText>(R.id.editTextVolume)
        val btnChangeVolume = findViewById<Button>(R.id.btnChangeVolume)

        // Получаем URI аудиофайла
        audioUri = intent.getParcelableExtra("AUDIO_URI")
        if (audioUri == null) {
            Log.e("ChangeVolumeActivity", "❌ Ошибка: URI аудиофайла не получен")
            Toast.makeText(this, "Ошибка получения файла", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        progressDialog = ProgressDialog(this)
        progressDialog!!.setMessage("Обработка...")
        progressDialog!!.setCancelable(false)

        btnChangeVolume.setOnClickListener {
            val volumeText = editTextVolume.text.toString()
            volumeLevel = volumeText.toDoubleOrNull() ?: 1.0
            if (volumeLevel < 0.0 || volumeLevel > 2.0) {
                Toast.makeText(this, "Громкость должна быть от 0.0 до 2.0", Toast.LENGTH_SHORT).show()
            } else {
                Log.d("ChangeVolumeActivity", "🔊 Новый уровень громкости: $volumeLevel")
                changeVolume()
            }
        }
    }

    private fun changeVolume() {
        val inputPath = copyFileToInternalStorage(audioUri!!)
        if (inputPath.isEmpty()) {
            Log.e("ChangeVolumeActivity", "❌ Ошибка: Файл не скопирован в хранилище")
            Toast.makeText(this, "Ошибка копирования файла", Toast.LENGTH_LONG).show()
            return
        }

        val outputFile = File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), "output_volume.mp3")
        outputPath = outputFile.absolutePath

        val command = "-y -i \"$inputPath\" -filter:a \"volume=$volumeLevel\" \"$outputPath\""
        Log.d("ChangeVolumeActivity", "⏳ Запускаем FFmpegKit с командой: $command")

        progressDialog!!.show()

        FFmpegKit.executeAsync(command) { session: FFmpegSession ->
            progressDialog!!.dismiss()
            if (ReturnCode.isSuccess(session.returnCode)) {
                Log.d("ChangeVolumeActivity", "✅ Громкость изменена")
                showSuccessDialog()
            } else {
                Log.e("ChangeVolumeActivity", "❌ Ошибка FFmpeg")
                Toast.makeText(this, "Ошибка изменения громкости", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showSuccessDialog() {
        runOnUiThread {
            AlertDialog.Builder(this)
                .setTitle("Громкость изменена")
                .setMessage("Файл успешно обработан.")
                .setPositiveButton("ОК") { _, _ ->
                    MediaScannerConnection.scanFile(this, arrayOf(outputPath), null, null)
                    val resultIntent = Intent()
                    resultIntent.putExtra("OUTPUT_AUDIO", outputPath)
                    setResult(RESULT_OK, resultIntent)
                    finish()
                }
                .setCancelable(false)
                .show()
        }
    }

    private fun copyFileToInternalStorage(uri: Uri): String {
        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null
        return try {
            inputStream = contentResolver.openInputStream(uri)
            if (inputStream == null) {
                Log.e("ChangeVolumeActivity", "❌ Ошибка: inputStream == null")
                return ""
            }

            val outputFile = File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), "input_audio.mp3")
            outputStream = FileOutputStream(outputFile)

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
}
