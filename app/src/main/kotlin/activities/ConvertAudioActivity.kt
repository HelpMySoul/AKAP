package com.example.akap.activities


import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import com.example.akap.R

class ConvertAudioActivity : AppCompatActivity() {

    private var audioUri: Uri? = null
    private var selectedFormat: String = "wav"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_convert_audio)

        val btnSelectFile: Button = findViewById(R.id.btnSelectFile)
        val btnConvert: Button = findViewById(R.id.btnConvert)
        val formatSpinner: Spinner = findViewById(R.id.formatSpinner)

        // Заполняем спиннер форматами
        val formats = arrayOf("wav", "aac", "ogg", "flac")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, formats)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        formatSpinner.adapter = adapter

        btnSelectFile.setOnClickListener { selectAudioFile() }
        btnConvert.setOnClickListener { convertAudio(formatSpinner.selectedItem.toString()) }
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
        }
    }

    private fun convertAudio(format: String) {
        if (audioUri == null) {
            Toast.makeText(this, "Выберите файл", Toast.LENGTH_SHORT).show()
            return
        }

        val command = "-i ${audioUri!!.path} /storage/emulated/0/Music/output.$format"
        FFmpegKit.executeAsync(command) { session ->
            if (ReturnCode.isSuccess(session.returnCode)) {
                Toast.makeText(this, "Аудио успешно конвертировано в $format", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Ошибка конвертации", Toast.LENGTH_LONG).show()
            }
        }
    }
}
