package activities

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

class SplitAudioActivity : AppCompatActivity() {

    private var audioUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_split_audio)

        val btnSelectFile: Button = findViewById(R.id.btnSelectFile)
        val btnSplit: Button = findViewById(R.id.btnSplit)

        btnSelectFile.setOnClickListener { selectAudioFile() }
        btnSplit.setOnClickListener { splitAudio() }
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

    private fun splitAudio() {
        if (audioUri == null) {
            Toast.makeText(this, "Выберите файл", Toast.LENGTH_SHORT).show()
            return
        }

        val command = "-i ${audioUri!!.path} -f segment -segment_time 10 -c copy /storage/emulated/0/Music/output%03d.mp3"
        FFmpegKit.executeAsync(command) { session ->
            if (ReturnCode.isSuccess(session.returnCode)) {
                Toast.makeText(this, "Аудио успешно разделено", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Ошибка разделения аудио", Toast.LENGTH_LONG).show()
            }
        }
    }
}
