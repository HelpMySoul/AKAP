package builders

import android.app.AlertDialog
import android.content.Context
import android.widget.EditText
import android.widget.SeekBar
import android.widget.Toast
import com.example.akap.R

class SetTimeBuilder (
    private val context:     Context,
    private val currentTime: Int,
    private val maxTime:     Int,
    private val onChange:    (Int) -> Unit
    ) {
        fun built() {
            val dialogBuilder = AlertDialog.Builder(context)
            val input         = SeekBar(context)

            input.max      = maxTime
            input.progress = currentTime

            dialogBuilder.setTitle(context.getString(R.string.edit_time))
            dialogBuilder.setView(input)

            dialogBuilder.setPositiveButton(context.getString(R.string.save)) { _, _ ->
                val newTime = input.progress
                onChange(newTime)
            }
            dialogBuilder.setNegativeButton(context.getString(R.string.cancel)) { dialog, _ -> dialog.cancel() }

            val alertDialog = dialogBuilder.create()
            alertDialog.show()
        }
}