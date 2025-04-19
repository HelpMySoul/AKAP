package activities

import android.content.ContentValues
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import com.example.akap.R
import java.io.File
import java.io.IOException

class TrimAudioActivity : AppCompatActivity() {

    private var audioUri: Uri? = null
    private var startTime = 0f
    private var endTime = 0f

    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var seekBarStart: SeekBar
    private lateinit var seekBarEnd: SeekBar
    private lateinit var btnTrimConfirm: Button
    private lateinit var txtStart: TextView
    private lateinit var txtEnd: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trim_audio)

        seekBarStart = findViewById(R.id.seekBarStart)
        seekBarEnd = findViewById(R.id.seekBarEnd)
        btnTrimConfirm = findViewById(R.id.btnTrimConfirm)
        txtStart = findViewById(R.id.txtStart)
        txtEnd = findViewById(R.id.txtEnd)

        val uriString = intent.getStringExtra("AUDIO_URI")
        if (uriString.isNullOrEmpty()) {
            showToast("Ошибка: аудиофайл не передан")
            finish()
            return
        }

        audioUri = Uri.parse(uriString)

        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(this@TrimAudioActivity, audioUri!!)
                prepare()
            }

            val durationInSeconds = (mediaPlayer.duration / 1000).toFloat()

            seekBarStart.max = durationInSeconds.toInt()
            seekBarEnd.max = durationInSeconds.toInt()
            seekBarEnd.progress = durationInSeconds.toInt()

            seekBarStart.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    startTime = progress.toFloat()
                    txtStart.text = "Старт: ${formatSeconds(startTime)}"
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })

            seekBarEnd.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    endTime = progress.toFloat()
                    txtEnd.text = "Конец: ${formatSeconds(endTime)}"
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })

            btnTrimConfirm.setOnClickListener { trimAudio() }

        } catch (e: Exception) {
            showToast("Ошибка загрузки аудиофайла: ${e.message}")
            finish()
        }
    }

    private fun formatSeconds(seconds: Float): String {
        val minutes = (seconds / 60).toInt()
        val secs = (seconds % 60).toInt()
        return String.format("%02d:%02d", minutes, secs)
    }

    private fun trimAudio() {
        val trimmedFileName = "trimmed_audio_${System.currentTimeMillis()}.mp3"
        val trimmedFile = File(cacheDir, trimmedFileName)

        val inputPath = getRealPathFromUri(audioUri!!) ?: run {
            showToast("Ошибка: путь к файлу не найден")
            return
        }

        val command = arrayOf(
            "-i", inputPath,
            "-ss", startTime.toString(),
            "-to", endTime.toString(),
            "-c", "copy",
            trimmedFile.absolutePath
        )

        Log.d("FFMPEG", "Запускаем обрезку с командой: ${command.joinToString(" ")}")

        FFmpegKit.executeAsync(command.joinToString(" ")) { session ->
            val returnCode = session.returnCode
            if (ReturnCode.isSuccess(returnCode)) {
                Log.d("FFMPEG", "Аудио успешно обрезано: ${trimmedFile.absolutePath}")

                val savedUri = saveTrimmedAudioToMusic(trimmedFile)
                runOnUiThread {
                    if (savedUri != null) {
                        showToast("Файл сохранен в: $savedUri")
                        val resultIntent = Intent()
                        resultIntent.putExtra("TRIMMED_AUDIO", savedUri.toString())
                        setResult(RESULT_OK, resultIntent)
                        finish()
                    } else {
                        showToast("Ошибка сохранения файла")
                    }
                }
            } else {
                Log.e("FFMPEG", "Ошибка при обрезке аудио: ${session.allLogsAsString}")
                runOnUiThread { showToast("Ошибка обрезки аудио") }
            }
        }
    }

    private fun saveTrimmedAudioToMusic(file: File): Uri? {
        val values = ContentValues().apply {
            put(MediaStore.Audio.Media.DISPLAY_NAME, file.name)
            put(MediaStore.Audio.Media.MIME_TYPE, "audio/mp3")
            put(MediaStore.Audio.Media.RELATIVE_PATH, Environment.DIRECTORY_MUSIC)
            put(MediaStore.Audio.Media.IS_PENDING, 1)
        }

        val resolver = contentResolver
        val collection = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val uri = resolver.insert(collection, values) ?: return null

        try {
            resolver.openOutputStream(uri)?.use { outputStream ->
                file.inputStream().use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

            values.clear()
            values.put(MediaStore.Audio.Media.IS_PENDING, 0)
            resolver.update(uri, values, null, null)

            return uri
        } catch (e: IOException) {
            Log.e("TrimAudio", "Ошибка сохранения файла: ${e.message}")
            return null
        }
    }

    private fun getRealPathFromUri(uri: Uri): String? {
        val inputStream = contentResolver.openInputStream(uri) ?: return null
        val file = File(cacheDir, "temp_audio.mp3")
        inputStream.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return file.absolutePath
    }

    private fun showToast(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::mediaPlayer.isInitialized) {
            mediaPlayer.release()
        }
    }
}
