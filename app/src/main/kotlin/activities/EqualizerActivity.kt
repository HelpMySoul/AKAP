package activities

import android.content.Intent
import android.media.MediaPlayer
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.akap.R

class EqualizerActivity : AppCompatActivity() {

    // UI элементы
    private lateinit var btnSelectFile: Button
    private lateinit var btnPlayPause: Button
    private lateinit var btnApplyEqualizer: Button
    private lateinit var fileInfo: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var presetSpinner: Spinner

    // SeekBars
    private lateinit var bassControl: SeekBar
    private lateinit var lowMidControl: SeekBar
    private lateinit var midControl: SeekBar
    private lateinit var highMidControl: SeekBar
    private lateinit var trebleControl: SeekBar
    private lateinit var presenceControl: SeekBar
    private lateinit var volumeControl: SeekBar
    private lateinit var balanceControl: SeekBar

    // Значения
    private lateinit var bassValue: TextView
    private lateinit var lowMidValue: TextView
    private lateinit var midValue: TextView
    private lateinit var highMidValue: TextView
    private lateinit var trebleValue: TextView
    private lateinit var presenceValue: TextView
    private lateinit var volumeValue: TextView
    private lateinit var balanceValue: TextView

    // Аудио компоненты
    private lateinit var mediaPlayer: MediaPlayer
    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null

    // Состояние
    private var isPlaying = false
    private var currentFileUri: Uri? = null
    private var audioSessionId = 0

    companion object {
        private const val FILE_SELECT_CODE = 100
        private const val TAG = "EqualizerActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_equalizer)

        initViews()
        setupListeners()
        setupSpinner()
        initMediaPlayer()
    }

    private fun initViews() {
        btnSelectFile = findViewById(R.id.btnSelectFile)
        btnPlayPause = findViewById(R.id.btnPlayPause)
        btnApplyEqualizer = findViewById(R.id.btnApplyEqualizer)
        fileInfo = findViewById(R.id.fileInfo)
        progressBar = findViewById(R.id.progressBar)
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

        setupSeekBarListeners()
    }

    private fun setupSeekBarListeners() {
        bassControl.setOnSeekBarChangeListener(createSeekBarListener(bassValue, 1f) { applyEqualizerSettings() })
        lowMidControl.setOnSeekBarChangeListener(createSeekBarListener(lowMidValue, 1f) { applyEqualizerSettings() })
        midControl.setOnSeekBarChangeListener(createSeekBarListener(midValue, 1f) { applyEqualizerSettings() })
        highMidControl.setOnSeekBarChangeListener(createSeekBarListener(highMidValue, 1f) { applyEqualizerSettings() })
        trebleControl.setOnSeekBarChangeListener(createSeekBarListener(trebleValue, 1f) { applyEqualizerSettings() })
        presenceControl.setOnSeekBarChangeListener(createSeekBarListener(presenceValue, 1f) { applyEqualizerSettings() })

        volumeControl.setOnSeekBarChangeListener(createSeekBarListener(volumeValue, 0.01f, 100) { applyVolume() })
        balanceControl.setOnSeekBarChangeListener(createBalanceSeekBarListener())
    }

    private fun createSeekBarListener(
        textView: TextView,
        multiplier: Float,
        offset: Int = 0,
        callback: () -> Unit
    ): SeekBar.OnSeekBarChangeListener {
        return object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                val value = (progress - offset) * multiplier
                textView.text = "%.1f".format(value)
                if (fromUser) callback()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) { callback() }
        }
    }

    private fun createBalanceSeekBarListener(): SeekBar.OnSeekBarChangeListener {
        return object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                val balance = (progress - 10) / 10f
                balanceValue.text = "%.1f".format(balance)
                if (fromUser) applyBalance()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) { applyBalance() }
        }
    }

    private fun setupListeners() {
        btnSelectFile.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "audio/*"
                addCategory(Intent.CATEGORY_OPENABLE)
            }
            startActivityForResult(intent, FILE_SELECT_CODE)
        }

        btnPlayPause.setOnClickListener {
            if (isPlaying) {
                pauseAudio()
            } else {
                playAudio()
            }
        }

        btnApplyEqualizer.setOnClickListener {
            applyAllAudioSettings()
            Toast.makeText(this, "Настройки применены", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupSpinner() {
        val presets = arrayOf(
            "Плоский", "Поп", "Рок", "Джаз", "Классика", "Бас", "Голос"
        )

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            presets
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        presetSpinner.adapter = adapter

        presetSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                applyPreset(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun initMediaPlayer() {
        mediaPlayer = MediaPlayer()
        mediaPlayer.setOnCompletionListener {
            isPlaying = false
            btnPlayPause.text = "Воспроизвести"
        }

        mediaPlayer.setOnPreparedListener {
            initAudioEffects()
            progressBar.visibility = android.view.View.GONE
            isPlaying = true
            btnPlayPause.text = "Пауза"
            mediaPlayer.start()
            applyAllAudioSettings() // Применяем настройки после начала воспроизведения
        }
    }

    private fun initAudioEffects() {
        releaseAudioEffects()
        audioSessionId = mediaPlayer.audioSessionId

        try {
            equalizer = Equalizer(0, audioSessionId).apply {
                enabled = true
            }

            bassBoost = BassBoost(0, audioSessionId).apply {
                enabled = true
            }

            applyAllAudioSettings()

        } catch (e: Exception) {
            Log.e(TAG, "Audio effects initialization failed", e)
            Toast.makeText(this, "Аудио эффекты не поддерживаются", Toast.LENGTH_LONG).show()
        }
    }

    private fun applyAllAudioSettings() {
        applyEqualizerSettings()
        applyVolume()
        applyBalance()
    }

    private fun applyEqualizerSettings() {
        equalizer?.let { eq ->
            val minLevel = eq.bandLevelRange[0].toInt()
            val maxLevel = eq.bandLevelRange[1].toInt()
            val range = maxLevel - minLevel

            setBandLevel(eq, 60f, bassControl.progress, minLevel, range)
            setBandLevel(eq, 230f, lowMidControl.progress, minLevel, range)
            setBandLevel(eq, 910f, midControl.progress, minLevel, range)
            setBandLevel(eq, 3600f, highMidControl.progress, minLevel, range)
            setBandLevel(eq, 14000f, trebleControl.progress, minLevel, range)
            setBandLevel(eq, 16000f, presenceControl.progress, minLevel, range)
        }
    }

    private fun setBandLevel(eq: Equalizer, freq: Float, progress: Int, minLevel: Int, range: Int) {
        val band = findBandForFrequency(eq, freq)
        if (band != -1) {
            val level = (minLevel + (progress * range / 100)).toShort()
            eq.setBandLevel(band.toShort(), level)
        }
    }

    private fun findBandForFrequency(eq: Equalizer, freq: Float): Int {
        for (i in 0 until eq.numberOfBands) {
            val bandFreq = eq.getCenterFreq(i.toShort())
            if (bandFreq.toFloat() >= freq) {
                return i
            }
        }
        return eq.numberOfBands - 1
    }

    private fun applyVolume() {
        applyBalance() // Всегда обновляем баланс при изменении громкости
    }

    private fun applyBalance() {
        val balance = (balanceControl.progress - 10) / 10f // -1.0 (лево) до 1.0 (право)
        val masterVolume = volumeControl.progress / 100f

        val leftVolume = when {
            balance < 0 -> 1f
            balance > 0 -> 1f - balance
            else -> 1f
        }

        val rightVolume = when {
            balance > 0 -> 1f
            balance < 0 -> 1f + balance
            else -> 1f
        }

        mediaPlayer.setVolume(leftVolume * masterVolume, rightVolume * masterVolume)
    }

    private fun applyPreset(position: Int) {
        when (position) {
            0 -> resetEqualizer()
            1 -> setPopPreset()
            2 -> setRockPreset()
            3 -> setJazzPreset()
            4 -> setClassicPreset()
            5 -> setBassPreset()
            6 -> setVoicePreset()
        }
        applyEqualizerSettings()
    }

    private fun resetEqualizer() {
        bassControl.progress = 50
        lowMidControl.progress = 50
        midControl.progress = 50
        highMidControl.progress = 50
        trebleControl.progress = 50
        presenceControl.progress = 50
    }

    private fun setPopPreset() {
        bassControl.progress = 60
        lowMidControl.progress = 45
        midControl.progress = 55
        highMidControl.progress = 65
        trebleControl.progress = 60
        presenceControl.progress = 50
    }

    private fun setRockPreset() {
        bassControl.progress = 70
        lowMidControl.progress = 60
        midControl.progress = 50
        highMidControl.progress = 40
        trebleControl.progress = 50
        presenceControl.progress = 45
    }

    private fun setJazzPreset() {
        bassControl.progress = 55
        lowMidControl.progress = 60
        midControl.progress = 65
        highMidControl.progress = 55
        trebleControl.progress = 50
        presenceControl.progress = 45
    }

    private fun setClassicPreset() {
        bassControl.progress = 40
        lowMidControl.progress = 45
        midControl.progress = 50
        highMidControl.progress = 55
        trebleControl.progress = 60
        presenceControl.progress = 65
    }

    private fun setBassPreset() {
        bassControl.progress = 90
        lowMidControl.progress = 70
        midControl.progress = 40
        highMidControl.progress = 30
        trebleControl.progress = 35
        presenceControl.progress = 30
    }

    private fun setVoicePreset() {
        bassControl.progress = 40
        lowMidControl.progress = 55
        midControl.progress = 70
        highMidControl.progress = 65
        trebleControl.progress = 50
        presenceControl.progress = 45
    }

    private fun playAudio() {
        if (currentFileUri == null) {
            Toast.makeText(this, "Выберите аудиофайл", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            progressBar.visibility = android.view.View.VISIBLE
            mediaPlayer.reset()
            mediaPlayer.setDataSource(this, currentFileUri!!)
            mediaPlayer.prepareAsync()

            // Сброс баланса при выборе нового файла
            balanceControl.progress = 10
            balanceValue.text = "0.0"

        } catch (e: Exception) {
            progressBar.visibility = android.view.View.GONE
            Log.e(TAG, "Ошибка воспроизведения", e)
            Toast.makeText(this, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun pauseAudio() {
        mediaPlayer.pause()
        isPlaying = false
        btnPlayPause.text = "Воспроизвести"
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FILE_SELECT_CODE && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                currentFileUri = uri
                val fileName = uri.lastPathSegment ?: "Неизвестный файл"
                fileInfo.text = "Выбран файл: $fileName"

                isPlaying = false
                btnPlayPause.text = "Воспроизвести"
            }
        }
    }

    private fun releaseAudioEffects() {
        equalizer?.release()
        bassBoost?.release()

        equalizer = null
        bassBoost = null
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
        releaseAudioEffects()
    }
}