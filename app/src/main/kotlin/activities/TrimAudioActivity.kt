package activities

import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
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

        // Получаем путь к файлу из Intent
        audioUri = intent.getParcelableExtra("AUDIO_URI")

        if (audioUri != null) {
            mediaPlayer = MediaPlayer.create(this, audioUri)
            seekBarEnd.max = mediaPlayer.duration
            seekBarEnd.progress = mediaPlayer.duration
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
            check()
            trimAudio()
        }
    }

    private  fun  check() {
        val inputPath = audioUri.toString()

        val command = "-i $inputPath"

        FFmpegKit.executeAsync(command) { session ->
            val returnCode = session.returnCode
            if (ReturnCode.isSuccess(returnCode)) {
                Log.d("FFmpegKit", "Информация о файле: ${session.allLogsAsString}")
            } else {
                Log.e("FFmpegKit", "Ошибка: ${session.failStackTrace}")
            }
        }
    }

    private fun trimAudio() {
        Toast.makeText(applicationContext, audioUri.toString(), Toast.LENGTH_SHORT).show()
        val inputPath = audioUri.toString()

        val lastSlashIndex = inputPath.lastIndexOf('/')
        val directoryPath = inputPath.substring(0, lastSlashIndex + 1)
        val outputPath = "${directoryPath}trimmed_audio.mp3"

        val command = "-i $inputPath -ss ${startTime / 1000} -to ${endTime / 1000} -c copy $outputPath"

        FFmpegKit.executeAsync(command) { session ->
            if (ReturnCode.isSuccess(session.returnCode)) {
                val resultIntent = Intent()
                resultIntent.putExtra("TRIMMED_AUDIO", outputPath)
                setResult(RESULT_OK, resultIntent)
                finish()
                Log.d("TrimAct", "Trimmed $outputPath")
            } else {
                Log.d("TrimAct", "Error $outputPath")
            }
        }
    }
}
