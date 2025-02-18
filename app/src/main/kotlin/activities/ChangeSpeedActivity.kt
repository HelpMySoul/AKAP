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

class ChangeSpeedActivity : AppCompatActivity() {

    private var audioUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_speed)

        val btnSelectFile: Button = findViewById(R.id.btnSelectFile)
        val btnChangeSpeed: Button = findViewById(R.id.btnChangeSpeed)
        val speedInput: EditText = findViewById(R.id.speedInput)

        btnSelectFile.setOnClickListener { selectAudioFile() }
        btnChangeSpeed.setOnClickListener {
            val speed = speedInput.text.toString().toFloatOrNull()
            if (speed != null && speed > 0) {
                changeSpeed(speed)
            } else {
                Toast.makeText(this, "Введите корректную скорость", Toast.LENGTH_SHORT).show()
            }
        }
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

    private fun changeSpeed(speed: Float) {
        if (audioUri == null) {
            Toast.makeText(this, "Выберите аудиофайл", Toast.LENGTH_SHORT).show()
            return
        }

        val command = "-i ${audioUri!!.path} -filter:a \"atempo=$speed\" -vn /storage/emulated/0/Music/speed_changed.mp3"
        FFmpegKit.executeAsync(command) { session ->
            if (ReturnCode.isSuccess(session.returnCode)) {
                Toast.makeText(this, "Скорость изменена", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Ошибка изменения скорости", Toast.LENGTH_LONG).show()
            }
        }
    }
}
