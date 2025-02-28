package builders

import android.app.AlertDialog
import android.content.Context
import android.widget.EditText
import android.widget.Toast
import com.example.akap.R

class SetNewPlaylistBuilder(
    private val context:     Context,
    private val currentName: String,
    private val onCreate:    (String) -> Unit
) {

    fun built() {
        val dialogBuilder = AlertDialog.Builder(context)
        val input         = EditText(context)

        input.setText(currentName)

        dialogBuilder.setTitle(context.getString(R.string.set_playlist_name))
        dialogBuilder.setView(input)

        dialogBuilder.setPositiveButton(context.getString(R.string.create)) { _, _ ->
            val newName = input.text.toString()
            if (newName.isNotEmpty()) {
                onCreate(newName)
            } else {
                Toast.makeText(context, context.getString(R.string.playlist_name_empty), Toast.LENGTH_SHORT).show()
            }
        }
        dialogBuilder.setNegativeButton(context.getString(R.string.cancel)) { dialog, _ -> dialog.cancel() }

        val alertDialog = dialogBuilder.create()
        alertDialog.show()
    }
}