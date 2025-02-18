
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import com.arthenica.ffmpegkit.Session
import com.example.akap.R
import tools.controllers.SongPickerController

class Tools : Fragment() {
    lateinit var songPickerController: SongPickerController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_tools, container, false)

        val btnTrim: Button = view.findViewById(R.id.btnTrim)
        val btnMerge: Button = view.findViewById(R.id.btnMerge)
        val btnSplit: Button = view.findViewById(R.id.btnSplit)
        val btnVolume: Button = view.findViewById(R.id.btnVolume)
        val btnConvert: Button = view.findViewById(R.id.btnConvert)
        val btnEditTags: Button = view.findViewById(R.id.btnEditTags)
        val btnEqualizer: Button = view.findViewById(R.id.btnEqualizer)
        val btnVocalProcessing: Button = view.findViewById(R.id.btnVocalProcessing)

        btnTrim.setOnClickListener { openTrimActivity() }
        btnMerge.setOnClickListener { mergeAudio() }
        btnSplit.setOnClickListener { splitAudio() }
        btnVolume.setOnClickListener { changeVolume() }
        btnConvert.setOnClickListener { convertAudio() }
        btnEditTags.setOnClickListener { editTags() }
        btnEqualizer.setOnClickListener { openEqualizer() }
        btnVocalProcessing.setOnClickListener { processVocal() }

        songPickerController = SongPickerController(this, parentFragmentManager)

        songPickerController.setListeners(viewLifecycleOwner,
            "GET_TRIM_SONG_PATH" to { /*selectedSongPath ->
                val intent = Intent(requireContext(), TrimAudioActivity::class.java)
                intent.putExtra("AUDIO_URI", Uri.parse(selectedSongPath))
                startActivity(intent)*/ //TODO() create controller for it
            }
        )

        return view
    }

    private fun executeFFmpegCommand(command: String, successMessage: String, errorMessage: String) {
        FFmpegKit.executeAsync(command) { session: Session ->
            if (ReturnCode.isSuccess(session.returnCode)) {
                Toast.makeText(requireContext(), successMessage, Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun openTrimActivity() {
        songPickerController.openSongPicker("GET_TRIM_SONG_PATH")
    }

    private fun mergeAudio() {
        val command = "-i concat:input1.mp3|input2.mp3 -c copy output.mp3"
        executeFFmpegCommand(command, "Аудио успешно объединено", "Ошибка объединения аудио")
    }

    private fun splitAudio() {
        val command = "-i input.mp3 -f segment -segment_time 10 -c copy output%03d.mp3"
        executeFFmpegCommand(command, "Аудио успешно разделено", "Ошибка разделения аудио")
    }

    private fun changeVolume() {
        val command = "-i input.mp3 -filter:a volume=1.5 output.mp3"
        executeFFmpegCommand(command, "Громкость успешно изменена", "Ошибка изменения громкости")
    }

    private fun convertAudio() {
        val command = "-i input.mp3 output.wav"
        executeFFmpegCommand(command, "Аудио успешно конвертировано", "Ошибка конвертации аудио")
    }

    private fun editTags() {
        Toast.makeText(requireContext(), "Функция редактирования тегов пока не реализована", Toast.LENGTH_SHORT).show()
    }

    private fun openEqualizer() {
        Toast.makeText(requireContext(), "Функция эквалайзера пока не реализована", Toast.LENGTH_SHORT).show()
    }

    private fun processVocal() {
        val command = "-i input.mp3 -af \"highpass=f=200, lowpass=f=3000\" output.mp3"
        executeFFmpegCommand(command, "Вокал успешно обработан", "Ошибка обработки вокала")
    }
}
