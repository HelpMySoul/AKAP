package com.example.akap.activities


import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import com.example.akap.R

class VocalProcessingActivity : AppCompatActivity() {

    private var audioUri: Uri? = null
    private lateinit var fileInfo: TextView
    private lateinit var btnSelectFile: Button
    private lateinit var btnProcessVocal: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vocal_processing)

        btnSelectFile = findViewById(R.id.btnSelectFile)
        btnProcessVocal = findViewById(R.id.btnProcessVocal)
        fileInfo = findViewById(R.id.fileInfo)

        btnSelectFile.setOnClickListener { selectAudioFile() }
        btnProcessVocal.setOnClickListener { processVocal() }
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

    private fun processVocal() {
        if (audioUri == null) {
            Toast.makeText(this, "Выберите аудиофайл", Toast.LENGTH_SHORT).show()
            return
        }

        val inputPath = audioUri!!.path
        val outputPath = "/storage/emulated/0/Music/processed_vocal.mp3"
        val command = "-i $inputPath -af \"highpass=f=200, lowpass=f=3000\" $outputPath"

        FFmpegKit.executeAsync(command) { session ->
            if (ReturnCode.isSuccess(session.returnCode)) {
                Toast.makeText(this, "Вокал успешно обработан", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Ошибка обработки вокала", Toast.LENGTH_LONG).show()
            }
        }
    }
}
