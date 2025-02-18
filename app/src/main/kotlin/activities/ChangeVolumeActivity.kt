package activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import com.example.akap.R

class ChangeVolumeActivity : AppCompatActivity() {

    private var audioUri: Uri? = null
    private var volumeLevel: Float = 1.0f // Стандартное значение громкости

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_volume)

        val btnSelectFile: Button = findViewById(R.id.btnSelectFile)
        val btnChangeVolume: Button = findViewById(R.id.btnChangeVolume)
        val seekBarVolume: SeekBar = findViewById(R.id.seekBarVolume)

        btnSelectFile.setOnClickListener { selectAudioFile() }
        btnChangeVolume.setOnClickListener { changeVolume() }

        // Обновляем громкость по ползунку
        seekBarVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                volumeLevel = progress / 100f // Переводим в диапазон 0.0 - 2.0
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
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

    private fun changeVolume() {
        if (audioUri == null) {
            Toast.makeText(this, "Выберите файл", Toast.LENGTH_SHORT).show()
            return
        }

        val command = "-i ${audioUri!!.path} -filter:a \"volume=$volumeLevel\" /storage/emulated/0/Music/output_volume.mp3"
        FFmpegKit.executeAsync(command) { session ->
            if (ReturnCode.isSuccess(session.returnCode)) {
                Toast.makeText(this, "Громкость успешно изменена", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Ошибка изменения громкости", Toast.LENGTH_LONG).show()
            }
        }
    }
}
