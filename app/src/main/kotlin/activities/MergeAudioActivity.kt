package com.example.akap.activities


import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import com.example.akap.R

class MergeAudioActivity : AppCompatActivity() {

    private var audioUri1: Uri? = null
    private var audioUri2: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_merge_audio)

        val btnSelectFile1: Button = findViewById(R.id.btnSelectFile1)
        val btnSelectFile2: Button = findViewById(R.id.btnSelectFile2)
        val btnMerge: Button = findViewById(R.id.btnMerge)

        btnSelectFile1.setOnClickListener { selectAudioFile(1) }
        btnSelectFile2.setOnClickListener { selectAudioFile(2) }
        btnMerge.setOnClickListener { mergeAudio() }
    }

    private fun selectAudioFile(requestCode: Int) {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "audio/*"
        startActivityForResult(intent, requestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && data != null) {
            when (requestCode) {
                1 -> audioUri1 = data.data
                2 -> audioUri2 = data.data
            }
        }
    }

    private fun mergeAudio() {
        if (audioUri1 == null || audioUri2 == null) {
            Toast.makeText(this, "Выберите два файла", Toast.LENGTH_SHORT).show()
            return
        }

        val command = "-i concat:${audioUri1!!.path}|${audioUri2!!.path} -c copy /storage/emulated/0/Music/merged.mp3"
        FFmpegKit.executeAsync(command) { session ->
            if (ReturnCode.isSuccess(session.returnCode)) {
                Toast.makeText(this, "Аудио успешно объединено", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Ошибка объединения аудио", Toast.LENGTH_LONG).show()
            }
        }
    }
}
