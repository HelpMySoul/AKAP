package com.example.akap.activities


import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import com.example.akap.R

class EqualizerActivity : AppCompatActivity() {

    private var audioUri: Uri? = null
    private lateinit var fileInfo: TextView
    private lateinit var btnSelectFile: Button
    private lateinit var btnApplyEqualizer: Button
    private lateinit var bassControl: SeekBar
    private lateinit var midControl: SeekBar
    private lateinit var trebleControl: SeekBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_equalizer)

        btnSelectFile = findViewById(R.id.btnSelectFile)
        btnApplyEqualizer = findViewById(R.id.btnApplyEqualizer)
        fileInfo = findViewById(R.id.fileInfo)
        bassControl = findViewById(R.id.bassControl)
        midControl = findViewById(R.id.midControl)
        trebleControl = findViewById(R.id.trebleControl)

        btnSelectFile.setOnClickListener { selectAudioFile() }
        btnApplyEqualizer.setOnClickListener { applyEqualizer() }
    }

    private fun selectAudioFile() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "audio/*"
        startActivityForResult(intent, 1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null) {
            audioUri = data.data
            fileInfo.text = "Выбранный файл: ${audioUri.toString()}"
        }
    }

    private fun applyEqualizer() {
        if (audioUri == null) {
            Toast.makeText(this, "Выберите аудиофайл", Toast.LENGTH_SHORT).show()
            return
        }

        val inputPath = audioUri!!.path
        val outputPath = "/storage/emulated/0/Music/equalized_audio.mp3"

        // Получаем значения с эквалайзера
        val bassGain = bassControl.progress.toFloat() / 10
        val midGain = midControl.progress.toFloat() / 10
        val trebleGain = trebleControl.progress.toFloat() / 10

        // Создаем FFmpeg-команду
        val command = "-i $inputPath -af " +
                "\"equalizer=f=100:t=q:w=1:g=$bassGain, " +
                "equalizer=f=1000:t=q:w=1:g=$midGain, " +
                "equalizer=f=10000:t=q:w=1:g=$trebleGain\" " +
                "$outputPath"

        FFmpegKit.executeAsync(command) { session ->
            if (ReturnCode.isSuccess(session.returnCode)) {
                Toast.makeText(this, "Эквалайзер успешно применён", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Ошибка обработки звука", Toast.LENGTH_LONG).show()
            }
        }
    }
}
