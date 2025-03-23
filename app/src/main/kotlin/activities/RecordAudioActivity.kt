package activities

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.akap.R
import java.io.IOException

class RecordAudioActivity : AppCompatActivity() {

    private var mediaRecorder: MediaRecorder? = null
    private var outputUri: Uri? = null
    private var isRecording = false
    private lateinit var btnRecord: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record_audio)

        btnRecord = findViewById(R.id.btnRecord)
        btnRecord.setOnClickListener {
            if (isRecording) {
                stopRecording()
            } else {
                if (checkPermissions()) {
                    startRecording()
                } else {
                    requestPermissions()
                }
            }
        }
    }

    // Проверка разрешений
    private fun checkPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // На Android 10+ WRITE_EXTERNAL_STORAGE не требуется
            ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        } else {
            // На Android 9 и ниже нужно разрешение на запись в хранилище
            ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    }

    // Запрос разрешений
    private fun requestPermissions() {
        val permissions = mutableListOf(Manifest.permission.RECORD_AUDIO)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE) // Нужно только для Android 9 и ниже
        }
        ActivityCompat.requestPermissions(this, permissions.toTypedArray(), REQUEST_RECORD_AUDIO_PERMISSION)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                startRecording()
            } else {
                Toast.makeText(this, "Разрешения не предоставлены", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Метод для получения URI файла через MediaStore
    private fun getOutputUri(): Uri? {
        val contentValues = ContentValues().apply {
            put(MediaStore.Audio.Media.DISPLAY_NAME, "AUDIO_${System.currentTimeMillis()}.mp4")
            put(MediaStore.Audio.Media.MIME_TYPE, "audio/mp4")
            put(MediaStore.Audio.Media.RELATIVE_PATH, "Music/") // Файл сохранится в папку "Музыка"
        }
        return contentResolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, contentValues)
    }

    // Начало записи
    private fun startRecording() {
        outputUri = getOutputUri()
        if (outputUri == null) {
            Toast.makeText(this, "Ошибка доступа к хранилищу", Toast.LENGTH_SHORT).show()
            return
        }

        contentResolver.openFileDescriptor(outputUri!!, "w")?.use { pfd ->
            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(pfd.fileDescriptor)

                try {
                    prepare()
                    start()
                    isRecording = true
                    btnRecord.text = "Остановить запись"
                    Toast.makeText(this@RecordAudioActivity, "🎙️ Запись началась", Toast.LENGTH_SHORT).show()
                } catch (e: IOException) {
                    Log.e("RecordAudioActivity", "❌ Ошибка записи: ${e.message}")
                    Toast.makeText(this@RecordAudioActivity, "Ошибка записи", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Остановка записи
    private fun stopRecording() {
        mediaRecorder?.apply {
            stop()
            release()
        }
        mediaRecorder = null
        isRecording = false
        btnRecord.text = "Начать запись"

        Toast.makeText(this, "✅ Запись сохранена в 'Музыке'", Toast.LENGTH_LONG).show()
        Log.d("RecordAudioActivity", "🎙️ Запись сохранена: $outputUri")
    }

    override fun onStop() {
        super.onStop()
        mediaRecorder?.release()
        mediaRecorder = null
    }

    companion object {
        private const val REQUEST_RECORD_AUDIO_PERMISSION = 200
    }
}
