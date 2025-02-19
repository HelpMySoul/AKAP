package activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.akap.R

class EditTagsActivity : AppCompatActivity() {

    private var audioUri: Uri? = null
    private lateinit var titleInput: EditText
    private lateinit var artistInput: EditText
    private lateinit var albumInput: EditText
    private lateinit var fileInfo: TextView
    private lateinit var btnSelectFile: Button
    private lateinit var btnSaveTags: Button

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_tags)

        btnSelectFile = findViewById(R.id.btnSelectFile)
        btnSaveTags = findViewById(R.id.btnSaveTags)
        titleInput = findViewById(R.id.titleInput)
        artistInput = findViewById(R.id.artistInput)
        albumInput = findViewById(R.id.albumInput)
        fileInfo = findViewById(R.id.fileInfo)

        btnSelectFile.setOnClickListener { selectAudioFile() }
        btnSaveTags.setOnClickListener { saveTags() }
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
            displayMetadata()
        }
    }

    private fun displayMetadata() {
        if (audioUri == null) return

        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(this, audioUri)

        titleInput.setText(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE))
        artistInput.setText(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST))
        albumInput.setText(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM))

        fileInfo.text = "Выбранный файл: ${audioUri.toString()}"
        retriever.release()
    }

    private fun saveTags() {
        Toast.makeText(this, "Редактирование тегов не поддерживается стандартными API", Toast.LENGTH_SHORT).show()
    }
}
