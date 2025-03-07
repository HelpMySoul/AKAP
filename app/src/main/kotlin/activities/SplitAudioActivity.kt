package activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import com.example.akap.R
import java.io.File

class SplitAudioActivity : AppCompatActivity() {

    private var audioUri: Uri? = null
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_split_audio)

        val btnSelectFile: Button = findViewById(R.id.btnSelectFile)
        val btnSplit: Button = findViewById(R.id.btnSplit)
        progressBar = findViewById(R.id.progressBar)

        btnSelectFile.setOnClickListener { selectAudioFile() }
        btnSplit.setOnClickListener { splitAudio() }
    }

    private fun selectAudioFile() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "audio/*"
        }
        startActivityForResult(intent, 1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null) {
            audioUri = data.data
            Toast.makeText(this, "Файл выбран", Toast.LENGTH_SHORT).show()
        }
    }

    private fun splitAudio() {
        if (audioUri == null) {
            Toast.makeText(this, "Выберите файл", Toast.LENGTH_SHORT).show()
            return
        }

        val musicDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC), "SplitAudio")
        if (!musicDir.exists()) musicDir.mkdirs()

        val outputPath = File(musicDir, "output_%03d.mp3").absolutePath
        val command = "-i ${audioUri!!.path} -f segment -segment_time 10 -c copy $outputPath"

        progressBar.visibility = View.VISIBLE

        FFmpegKit.executeAsync(command) { session ->
            progressBar.visibility = View.GONE
            if (ReturnCode.isSuccess(session.returnCode)) {
                Toast.makeText(this, "Аудио успешно разделено", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Ошибка разделения аудио", Toast.LENGTH_LONG).show()
            }
        }
    }
}
