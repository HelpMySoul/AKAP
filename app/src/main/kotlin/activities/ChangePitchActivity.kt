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

class ChangePitchActivity : AppCompatActivity() {

    private var audioUri: Uri? = null
    private lateinit var pitchInput: EditText
    private lateinit var btnSelectFile: Button
    private lateinit var btnChangePitch: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_pitch)

        btnSelectFile = findViewById(R.id.btnSelectFile)
        btnChangePitch = findViewById(R.id.btnChangePitch)
        pitchInput = findViewById(R.id.pitchInput)

        btnSelectFile.setOnClickListener { selectAudioFile() }
        btnChangePitch.setOnClickListener { changePitch() }
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

    private fun changePitch() {
        if (audioUri == null) {
            Toast.makeText(this, "Выберите аудиофайл", Toast.LENGTH_SHORT).show()
            return
        }
        val pitch = pitchInput.text.toString()
        if (pitch.isEmpty()) {
            Toast.makeText(this, "Введите коэффициент высоты тона", Toast.LENGTH_SHORT).show()
            return
        }

        val outputPath = "/storage/emulated/0/Music/output_pitch.mp3"
        val command = "-i ${audioUri!!.path} -filter:a \"asetrate=44100*${pitch}\" $outputPath"

        FFmpegKit.executeAsync(command) { session ->
            if (ReturnCode.isSuccess(session.returnCode)) {
                Toast.makeText(this, "Высота тона изменена успешно", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Ошибка изменения высоты тона", Toast.LENGTH_LONG).show()
            }
        }
    }
}
