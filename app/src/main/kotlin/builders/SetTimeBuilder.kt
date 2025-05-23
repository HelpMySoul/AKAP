package builders

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.widget.SeekBar
import android.widget.TextView
import com.example.akap.R
import playlistMenu.adapters.TimeAdapter

class SetTimeBuilder(
    private val title:       String,
    private val context:     Context,
    private val currentTime: Int,
    private val maxTime:     Int,
    private val onChange:    (Int) -> Unit
) {
    @SuppressLint("SetTextI18n")
    fun built() {
        val dialogBuilder = AlertDialog.Builder(context)
        val input         = SeekBar(context)
        val timeText      = TextView(context)

        val container = android.widget.LinearLayout(context).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(50, 50, 50, 50)
            addView(timeText)
            addView(input)
        }

        timeText.text  = "${TimeAdapter.formatTime(currentTime)} / ${TimeAdapter.formatTime(maxTime)}"
        input.max      = maxTime
        input.progress = currentTime

        input.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                timeText.text = "${TimeAdapter.formatTime(progress)} / ${TimeAdapter.formatTime(maxTime)}"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        dialogBuilder.setTitle(title)
        dialogBuilder.setView(container)

        dialogBuilder.setPositiveButton(context.getString(R.string.save)) { _, _ ->
            val newTime = input.progress
            onChange(newTime)
        }
        dialogBuilder.setNegativeButton(context.getString(R.string.cancel)) { dialog, _ -> dialog.cancel() }

        val alertDialog = dialogBuilder.create()
        alertDialog.show()
    }
}