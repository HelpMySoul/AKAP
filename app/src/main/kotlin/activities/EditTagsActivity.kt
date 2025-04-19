package activities

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.akap.R
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import org.jaudiotagger.tag.datatype.Artwork
import java.io.File
import java.io.InputStream

class EditTagsActivity : AppCompatActivity() {

    private var audioUri: Uri? = null
    private var coverUri: Uri? = null
    private lateinit var titleInput: EditText
    private lateinit var artistInput: EditText
    private lateinit var albumInput: EditText
    private lateinit var fileInfo: TextView
    private lateinit var coverImage: ImageView
    private lateinit var btnSelectFile: Button
    private lateinit var btnSelectCover: Button
    private lateinit var btnSaveTags: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_tags)

        btnSelectFile = findViewById(R.id.btnSelectFile)
        btnSelectCover = findViewById(R.id.btnSelectCover)
        btnSaveTags = findViewById(R.id.btnSaveTags)
        titleInput = findViewById(R.id.titleInput)
        artistInput = findViewById(R.id.artistInput)
        albumInput = findViewById(R.id.albumInput)
        fileInfo = findViewById(R.id.fileInfo)
        coverImage = findViewById(R.id.coverImage)

        btnSelectFile.setOnClickListener { selectAudioFile() }
        btnSelectCover.setOnClickListener { selectCoverImage() }
        btnSaveTags.setOnClickListener { saveTags() }

        // Получаем URI аудиофайла из Intent
        audioUri = intent.getStringExtra("AUDIO_URI")?.let { Uri.parse(it) }
        if (audioUri != null) {
            displayMetadata()
        }
    }

    private fun selectAudioFile() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "audio/*"
        startActivityForResult(intent, 1)
    }

    private fun selectCoverImage() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, 2)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && data != null) {
            when (requestCode) {
                1 -> {
                    audioUri = data.data
                    displayMetadata()
                }
                2 -> {
                    coverUri = data.data
                    coverImage.setImageURI(coverUri)
                }
            }
        }
    }

    private fun displayMetadata() {
        if (audioUri == null) return

        try {
            // Создаем временный файл для аудио
            val tempFile = File.createTempFile("temp_audio", ".mp3", cacheDir)
            val inputStream: InputStream? = contentResolver.openInputStream(audioUri!!)
            inputStream?.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            // Читаем метаданные из временного файла
            val audioFile = AudioFileIO.read(tempFile)
            val tag = audioFile.tag

            titleInput.setText(tag.getFirst(FieldKey.TITLE))
            artistInput.setText(tag.getFirst(FieldKey.ARTIST))
            albumInput.setText(tag.getFirst(FieldKey.ALBUM))

            // Отображаем обложку, если она есть
            val artwork = tag.firstArtwork
            if (artwork != null) {
                val coverBytes = artwork.binaryData
                val coverBitmap = BitmapFactory.decodeByteArray(coverBytes, 0, coverBytes.size)
                coverImage.setImageBitmap(coverBitmap)
            }

            fileInfo.text = "Выбранный файл: ${audioUri.toString()}"
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Ошибка при чтении тегов", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveTags() {
        if (audioUri == null) {
            Toast.makeText(this, "Сначала выберите аудиофайл", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            // Создаем временный файл для аудио
            val tempFile = File.createTempFile("temp_audio", ".mp3", cacheDir)
            val inputStream: InputStream? = contentResolver.openInputStream(audioUri!!)
            inputStream?.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            // Читаем и изменяем метаданные
            val audioFile = AudioFileIO.read(tempFile)
            val tag = audioFile.tagOrCreateAndSetDefault

            // Сохраняем текстовые теги
            tag.setField(FieldKey.TITLE, titleInput.text.toString())
            tag.setField(FieldKey.ARTIST, artistInput.text.toString())
            tag.setField(FieldKey.ALBUM, albumInput.text.toString())

            // Сохраняем обложку, если она выбрана
            if (coverUri != null) {
                val coverInputStream = contentResolver.openInputStream(coverUri!!)
                val coverBytes = coverInputStream?.readBytes()
                if (coverBytes != null) {
                    val artwork = Artwork()
                    artwork.binaryData = coverBytes
                    artwork.mimeType = "image/jpeg" // Укажите правильный MIME-тип
                    tag.deleteArtworkField()
                    tag.setField(artwork)
                }
            }

            // Сохраняем изменения в файл
            audioFile.commit()

            // Копируем измененный файл обратно в исходное расположение
            val outputStream = contentResolver.openOutputStream(audioUri!!)
            outputStream?.use { output ->
                tempFile.inputStream().use { input ->
                    input.copyTo(output)
                }
            }

            Toast.makeText(this, "Теги и обложка успешно сохранены", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Ошибка при сохранении тегов", Toast.LENGTH_SHORT).show()
        }
    }
}