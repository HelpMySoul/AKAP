package activities

import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import com.example.akap.R
import java.io.File

class TrimAudioActivity : AppCompatActivity() {

    private var audioUri: Uri? = null
    private var startTime: Int = 0
    private var endTime: Int = 100

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

        // Получаем URI аудиофайла из Intent
        val uriString = intent.getStringExtra("AUDIO_URI")
        if (uriString == null) {
            Toast.makeText(this, "Ошибка: аудиофайл не передан", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        audioUri = Uri.parse(uriString)
        if (audioUri == null) {
            Toast.makeText(this, "Ошибка: неверный URI аудиофайла", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        try {
            mediaPlayer = MediaPlayer.create(this, audioUri)
            if (mediaPlayer.duration <= 0) {
                Toast.makeText(this, "Ошибка: неверный формат аудиофайла", Toast.LENGTH_SHORT).show()
                finish()
                return
            }

            seekBarEnd.max = mediaPlayer.duration
            seekBarEnd.progress = mediaPlayer.duration
        } catch (e: Exception) {
            Toast.makeText(this, "Ошибка загрузки аудиофайла", Toast.LENGTH_SHORT).show()
            finish()
        }

        seekBarStart.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                startTime = progress
                txtStart.text = "Старт: ${progress / 1000} сек"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        seekBarEnd.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                endTime = progress
                txtEnd.text = "Конец: ${progress / 1000} сек"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        btnTrimConfirm.setOnClickListener {
            trimAudio()
        }
    }

    private fun trimAudio() {
        try {
            val inputStream = contentResolver.openInputStream(audioUri!!)
            val outputPath = "${cacheDir.absolutePath}/trimmed_audio.mp3"

            // Копируем файл во временный каталог
            inputStream?.use { input ->
                File(outputPath).outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            val command = "-i $outputPath -ss ${startTime / 1000} -to ${endTime / 1000} -c copy $outputPath"

            FFmpegKit.executeAsync(command) { session ->
                if (ReturnCode.isSuccess(session.returnCode)) {
                    val resultIntent = Intent()
                    resultIntent.putExtra("TRIMMED_AUDIO", outputPath)
                    setResult(RESULT_OK, resultIntent)
                    finish()
                } else {
                    runOnUiThread {
                        Toast.makeText(this, "Ошибка обрезки аудио", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } catch (e: Exception) {
            runOnUiThread {
                Toast.makeText(this, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::mediaPlayer.isInitialized) {
            mediaPlayer.release()
        }
    }
}