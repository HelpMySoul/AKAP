package activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import com.example.akap.R
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import java.io.File
import java.io.IOException
import java.util.Locale

class EqualizerActivity : AppCompatActivity() {

    private var audioUri: Uri? = null
    private var tempFile: File? = null
    private lateinit var fileInfo: TextView
    private lateinit var btnSelectFile: Button
    private lateinit var btnApplyEqualizer: Button
    private lateinit var btnPlayPause: Button
    private lateinit var presetSpinner: Spinner
    private lateinit var bassControl: SeekBar
    private lateinit var lowMidControl: SeekBar
    private lateinit var midControl: SeekBar
    private lateinit var highMidControl: SeekBar
    private lateinit var trebleControl: SeekBar
    private lateinit var presenceControl: SeekBar
    private lateinit var volumeControl: SeekBar
    private lateinit var balanceControl: SeekBar
    private lateinit var bassValue: TextView
    private lateinit var lowMidValue: TextView
    private lateinit var midValue: TextView
    private lateinit var highMidValue: TextView
    private lateinit var trebleValue: TextView
    private lateinit var presenceValue: TextView
    private lateinit var volumeValue: TextView
    private lateinit var balanceValue: TextView
    private lateinit var player: ExoPlayer
    private var isPlaying = false

    private val presets = mapOf(
        "Обычный" to floatArrayOf(0f, 0f, 0f, 0f, 0f, 0f),
        "Рок" to floatArrayOf(5f, 4f, 3f, 2f, 1f, 0f),
        "Поп" to floatArrayOf(3f, 2f, 1f, 1f, 2f, 3f),
        "Джаз" to floatArrayOf(2f, 3f, 4f, 3f, 2f, 1f),
        "Классика" to floatArrayOf(0f, 1f, 2f, 3f, 4f, 5f)
    )

    private val selectAudioFileLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            data?.data?.let { uri ->
                audioUri = uri
                fileInfo.text = getString(R.string.selected_file, uri.toString())
                Log.d("EqualizerActivity", "Audio file selected: $audioUri")

                // Save temp file
                tempFile = File(cacheDir, "temp_audio.mp3")
                try {
                    contentResolver.openInputStream(uri)?.use { inputStream ->
                        tempFile?.outputStream()?.use { outputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }
                    Log.d("EqualizerActivity", "Temp file created: ${tempFile?.absolutePath}")
                    playAudio(tempFile!!)
                } catch (e: IOException) {
                    Log.e("EqualizerActivity", "Error while saving temp file", e)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_equalizer)

        // Initialize Views
        initializeViews()

        // Initialize ExoPlayer
        player = ExoPlayer.Builder(this).build()

        // Button Click Handlers
        btnSelectFile.setOnClickListener { selectAudioFile() }
        btnApplyEqualizer.setOnClickListener { applyEqualizer() }
        btnPlayPause.setOnClickListener {
            if (isPlaying) {
                player.pause()
                btnPlayPause.text = "Воспроизвести"
            } else {
                player.play()
                btnPlayPause.text = "Пауза"
            }
            isPlaying = !isPlaying
        }

        // Setup Preset Spinner
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, presets.keys.toList())
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        presetSpinner.adapter = adapter

        presetSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                val selectedPreset = presets.values.toList()[position]
                bassControl.progress = (selectedPreset[0] * 10).toInt()
                lowMidControl.progress = (selectedPreset[1] * 10).toInt()
                midControl.progress = (selectedPreset[2] * 10).toInt()
                highMidControl.progress = (selectedPreset[3] * 10).toInt()
                trebleControl.progress = (selectedPreset[4] * 10).toInt()
                presenceControl.progress = (selectedPreset[5] * 10).toInt()
                updateValues()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Update volume and balance values dynamically
        volumeControl.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                volumeValue.text = String.format(Locale.getDefault(), "%d", progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        balanceControl.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                balanceValue.text = String.format(Locale.getDefault(), "%d", progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun initializeViews() {
        btnSelectFile = findViewById(R.id.btnSelectFile)
        btnApplyEqualizer = findViewById(R.id.btnApplyEqualizer)
        btnPlayPause = findViewById(R.id.btnPlayPause)
        fileInfo = findViewById(R.id.fileInfo)
        presetSpinner = findViewById(R.id.presetSpinner)
        bassControl = findViewById(R.id.bassControl)
        lowMidControl = findViewById(R.id.lowMidControl)
        midControl = findViewById(R.id.midControl)
        highMidControl = findViewById(R.id.highMidControl)
        trebleControl = findViewById(R.id.trebleControl)
        presenceControl = findViewById(R.id.presenceControl)
        volumeControl = findViewById(R.id.volumeControl)
        balanceControl = findViewById(R.id.balanceControl)
        bassValue = findViewById(R.id.bassValue)
        lowMidValue = findViewById(R.id.lowMidValue)
        midValue = findViewById(R.id.midValue)
        highMidValue = findViewById(R.id.highMidValue)
        trebleValue = findViewById(R.id.trebleValue)
        presenceValue = findViewById(R.id.presenceValue)
        volumeValue = findViewById(R.id.volumeValue)
        balanceValue = findViewById(R.id.balanceValue)
    }

    private fun selectAudioFile() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "audio/*"
        selectAudioFileLauncher.launch(intent)
    }

    private fun applyEqualizer() {
        if (audioUri == null) {
            Toast.makeText(this, "Выберите аудиофайл", Toast.LENGTH_SHORT).show()
            return
        }

        val progressDialog = AlertDialog.Builder(this)
            .setMessage("Применение эквалайзера...")
            .setCancelable(false)
            .create()
        progressDialog.show()

        val outputFile = File(getExternalFilesDir(null), "equalized_audio.mp3")
        if (outputFile.exists()) outputFile.delete()

        // Нормализация громкости (от 0 до 1)
        val volume = volumeControl.progress / 100f

        // Нормализация баланса (от -1 до 1)
        val balance = (balanceControl.progress - 50) / 50f

        // Команда FFmpeg с эквалайзером, громкостью и балансом
        val command = "-y -i ${tempFile!!.absolutePath} -af " +
                "\"equalizer=f=100:t=q:w=1:g=${bassControl.progress / 10f}," +
                "equalizer=f=200:t=q:w=1:g=${lowMidControl.progress / 10f}," +
                "equalizer=f=1000:t=q:w=1:g=${midControl.progress / 10f}," +
                "equalizer=f=3000:t=q:w=1:g=${highMidControl.progress / 10f}," +
                "equalizer=f=6000:t=q:w=1:g=${trebleControl.progress / 10f}," +
                "equalizer=f=12000:t=q:w=1:g=${presenceControl.progress / 10f}," +
                "volume=${volume}," +
                "pan=stereo|c0=${1 - balance}|c1=${1 + balance}\" ${outputFile.absolutePath}"

        Log.d("EqualizerActivity", "FFmpeg Command: $command")

        FFmpegKit.executeAsync(command) { session ->
            runOnUiThread {
                progressDialog.dismiss()
                if (ReturnCode.isSuccess(session.returnCode)) {
                    Toast.makeText(this@EqualizerActivity, "Эквалайзер успешно применён", Toast.LENGTH_SHORT).show()
                    Log.d("EqualizerActivity", "Filter applied successfully!")
                    playAudio(outputFile)
                } else {
                    Toast.makeText(this@EqualizerActivity, "Ошибка применения эквалайзера", Toast.LENGTH_SHORT).show()
                    Log.e("EqualizerActivity", "Error applying filter: ${session.returnCode}")
                }
            }
        }
    }

    private fun playAudio(file: File) {
        val mediaItem = MediaItem.fromUri(Uri.fromFile(file))
        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()
        btnPlayPause.text = "Пауза"
        isPlaying = true
        Log.d("EqualizerActivity", "Playing audio: ${file.absolutePath}")
    }

    private fun updateValues() {
        bassValue.text = String.format(Locale.getDefault(), "%d", bassControl.progress)
        lowMidValue.text = String.format(Locale.getDefault(), "%d", lowMidControl.progress)
        midValue.text = String.format(Locale.getDefault(), "%d", midControl.progress)
        highMidValue.text = String.format(Locale.getDefault(), "%d", highMidControl.progress)
        trebleValue.text = String.format(Locale.getDefault(), "%d", trebleControl.progress)
        presenceValue.text = String.format(Locale.getDefault(), "%d", presenceControl.progress)
        volumeValue.text = String.format(Locale.getDefault(), "%d", volumeControl.progress)
        balanceValue.text = String.format(Locale.getDefault(), "%d", balanceControl.progress)
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }
}