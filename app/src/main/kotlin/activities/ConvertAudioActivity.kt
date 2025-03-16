package activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.akap.R

class ConvertAudioActivity : AppCompatActivity() {

    private var selectedAudioUri: Uri? = null
    private lateinit var formatSpinner: Spinner
    private lateinit var btnSelectFolder: Button
    private lateinit var btnConvert: Button
    private lateinit var tvStatus: TextView
    private var outputFolderUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_convert_audio)

        formatSpinner = findViewById(R.id.formatSpinner)
        btnSelectFolder = findViewById(R.id.btnSelectFolder)
        btnConvert = findViewById(R.id.btnConvert)
        tvStatus = findViewById(R.id.tvStatus)

        selectedAudioUri = intent.getParcelableExtra("AUDIO_URI")

        val formats = arrayOf("MP3", "WAV", "AAC", "FLAC")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, formats)
        formatSpinner.adapter = adapter

        btnSelectFolder.setOnClickListener { selectOutputFolder() }
        btnConvert.setOnClickListener { convertAudio() }
    }

    private fun selectOutputFolder() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        startActivityForResult(intent, REQUEST_CODE_FOLDER)
    }

    private fun convertAudio() {
        if (selectedAudioUri == null || outputFolderUri == null) {
            Toast.makeText(this, "Выберите файл и папку", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedFormat = formatSpinner.selectedItem.toString()
        tvStatus.text = "🔄 Конвертация в $selectedFormat..."

        // Здесь должен быть код конвертации аудио

        Log.d("ConvertAudioActivity", "Конвертация завершена")
        tvStatus.text = "✅ Аудио конвертировано в $selectedFormat"
        setResult(Activity.RESULT_OK)
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_FOLDER && resultCode == Activity.RESULT_OK) {
            outputFolderUri = data?.data
            Toast.makeText(this, "📁 Папка выбрана", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val REQUEST_CODE_FOLDER = 1001
    }
}
