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

class RemoveSilenceActivity : AppCompatActivity() {

    private var audioUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_remove_silence)

        val btnSelectFile: Button = findViewById(R.id.btnSelectFile)
        val btnRemoveSilence: Button = findViewById(R.id.btnRemoveSilence)

        btnSelectFile.setOnClickListener { selectAudioFile() }
        btnRemoveSilence.setOnClickListener { removeSilence() }
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

    private fun removeSilence() {
        if (audioUri == null) {
            Toast.makeText(this, "Выберите аудиофайл", Toast.LENGTH_SHORT).show()
            return
        }

        val command = "-i ${audioUri!!.path} -af silenceremove=1:0:-50dB /storage/emulated/0/Music/no_silence.mp3"
        FFmpegKit.executeAsync(command) { session ->
            if (ReturnCode.isSuccess(session.returnCode)) {
                Toast.makeText(this, "Тишина успешно удалена", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Ошибка удаления тишины", Toast.LENGTH_LONG).show()
            }
        }
    }
}
