package activities

import android.content.ContentValues
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
import java.text.SimpleDateFormat
import java.util.*

class SplitAudioActivity : AppCompatActivity() {

    private var audioUri: Uri? = null
    private var segmentDuration = 30

    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var btnSplit: Button
    private lateinit var txtInfo: TextView
    private lateinit var seekBarDuration: SeekBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_split_audio)

        btnSplit = findViewById(R.id.btnSplit)
        txtInfo = findViewById(R.id.txtInfo)
        seekBarDuration = findViewById(R.id.seekBarDuration)

        val uriString = intent.getStringExtra("AUDIO_URI")
        if (uriString.isNullOrEmpty()) {
            showToast("Ошибка: аудиофайл не передан")
            finish()
            return
        }

        audioUri = Uri.parse(uriString)

        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(this@SplitAudioActivity, audioUri!!)
                prepare()
            }

            val durationInSeconds = (mediaPlayer.duration / 1000).toInt()
            txtInfo.text = "Длительность: ${formatSeconds(durationInSeconds)}"

            seekBarDuration.max = durationInSeconds.coerceAtMost(60)
            seekBarDuration.progress = segmentDuration

            seekBarDuration.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    segmentDuration = progress.coerceAtLeast(5)
                    txtInfo.text = "Длительность сегмента: ${formatSeconds(segmentDuration)}"
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })

            btnSplit.setOnClickListener { splitAudio() }

        } catch (e: Exception) {
            showToast("Ошибка загрузки аудиофайла: ${e.message}")
            finish()
        }
    }

    private fun formatSeconds(seconds: Int): String {
        val minutes = seconds / 60
        val secs = seconds % 60
        return String.format("%02d:%02d", minutes, secs)
    }

    private fun splitAudio() {
        val inputPath = getRealPathFromUri(audioUri!!) ?: run {
            showToast("Ошибка: путь к файлу не найден")
            return
        }

        val durationInSeconds = (mediaPlayer.duration / 1000).toInt()
        val numSegments = (durationInSeconds + segmentDuration - 1) / segmentDuration

        val outputDir = File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), "SplitAudio")
        if (!outputDir.exists()) outputDir.mkdirs()

        for (i in 0 until numSegments) {
            val start = i * segmentDuration
            val end = (start + segmentDuration).coerceAtMost(durationInSeconds)

            // Генерация уникального имени файла
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val splitFile = File(outputDir, "split_part_${i + 1}_$timeStamp.mp3")

            val command = arrayOf(
                "-i", inputPath,
                "-ss", formatTime(start),
                "-to", formatTime(end),
                "-c:a", "libmp3lame",
                "-b:a", "192k",
                splitFile.absolutePath
            )

            Log.d("FFMPEG", "Запускаем разбиение с командой: ${command.joinToString(" ")}")

            FFmpegKit.executeAsync(command.joinToString(" ")) { session ->
                val returnCode = session.returnCode
                if (ReturnCode.isSuccess(returnCode)) {
                    Log.d("FFMPEG", "Часть ${i + 1} сохранена: ${splitFile.absolutePath}")
                    val savedUri = saveSplitAudioToMusic(splitFile, i + 1)
                    runOnUiThread {
                        if (savedUri != null) {
                            showToast("Файл сохранен: $savedUri")
                        } else {
                            showToast("Ошибка сохранения части ${i + 1}")
                        }
                    }
                } else {
                    Log.e("FFMPEG", "Ошибка разбиения части ${i + 1}: ${session.allLogsAsString}")
                    runOnUiThread { showToast("Ошибка разбиения части ${i + 1}") }
                }
            }
        }
    }

    private fun saveSplitAudioToMusic(file: File, index: Int): Uri? {
        val values = ContentValues().apply {
            put(MediaStore.Audio.Media.DISPLAY_NAME, "split_part_$index.mp3")
            put(MediaStore.Audio.Media.MIME_TYPE, "audio/mp3")
            put(MediaStore.Audio.Media.RELATIVE_PATH, "Music/SplitAudio")
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
            Log.e("SplitAudio", "Ошибка сохранения: ${e.message}")
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

    private fun formatTime(seconds: Int): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        return String.format("%02d:%02d:%02d", hours, minutes, secs)
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
