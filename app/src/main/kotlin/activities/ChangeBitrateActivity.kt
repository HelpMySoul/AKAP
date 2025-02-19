package activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import com.example.akap.R

class ChangeBitrateActivity : AppCompatActivity() {

    private var audioUri: Uri? = null
    private lateinit var bitrateInput: EditText
    private lateinit var btnSelectFile: Button
    private lateinit var btnChangeBitrate: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_bitrate)

        btnSelectFile = findViewById(R.id.btnSelectFile)
        btnChangeBitrate = findViewById(R.id.btnChangeBitrate)
        bitrateInput = findViewById(R.id.bitrateInput)

        btnSelectFile.setOnClickListener { selectAudioFile() }
        btnChangeBitrate.setOnClickListener { changeBitrate() }
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

    private fun changeBitrate() {
        if (audioUri == null) {
            Toast.makeText(this, "Выберите аудиофайл", Toast.LENGTH_SHORT).show()
            return
        }
        val bitrate = bitrateInput.text.toString()
        if (bitrate.isEmpty()) {
            Toast.makeText(this, "Введите битрейт", Toast.LENGTH_SHORT).show()
            return
        }

        val outputPath = "/storage/emulated/0/Music/output_bitrate.mp3"
        val command = "-i ${audioUri!!.path} -b:a ${bitrate}k $outputPath"

        FFmpegKit.executeAsync(command) { session ->
            if (ReturnCode.isSuccess(session.returnCode)) {
                Toast.makeText(this, "Битрейт изменён успешно", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Ошибка изменения битрейта", Toast.LENGTH_LONG).show()
            }
        }
    }
}
