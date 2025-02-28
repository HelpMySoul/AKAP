package builders

import android.app.AlertDialog
import android.content.Context
import android.widget.EditText
import android.widget.Toast
import com.example.akap.R

class EditPlaylistNameBuilder(
    private val context:     Context,
    private val currentName: String,
    private val onSave:      (String) -> Unit
) {

    fun built() {
        val dialogBuilder = AlertDialog.Builder(context)
        val input         = EditText(context)

        input.setText(currentName)

        dialogBuilder.setTitle(context.getString(R.string.Edit_Playlist_Name))
        dialogBuilder.setView(input)

        dialogBuilder.setPositiveButton(context.getString(R.string.Save)) { _, _ ->
            val newName = input.text.toString()
            if (newName.isNotEmpty()) {
                onSave(newName)
            } else {
                Toast.makeText(context, context.getString(R.string.Playlist_Name_Empty), Toast.LENGTH_SHORT).show()
            }
        }
        dialogBuilder.setNegativeButton(context.getString(R.string.Cancel)) { dialog, _ -> dialog.cancel() }

        val alertDialog = dialogBuilder.create()
        alertDialog.show()
    }
}