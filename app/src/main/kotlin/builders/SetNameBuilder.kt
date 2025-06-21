package builders

import android.app.AlertDialog
import android.content.Context
import android.widget.EditText
import android.widget.Toast
import com.example.akap.R

class SetNameBuilder(
    private val context:     Context,
    private val currentName: String,
    private val onChange:    (String) -> Unit
) {

    fun built() {
        val dialogBuilder = AlertDialog.Builder(context, R.style.AlertDialogTheme)
        val input         = EditText(context)

        input.setText(currentName)

        dialogBuilder.setTitle(context.getString(R.string.builder_name_text))
        dialogBuilder.setView(input)

        dialogBuilder.setPositiveButton(context.getString(R.string.save)) { _, _ ->
            val newName = input.text.toString()
            if (newName.isNotEmpty()) {
                onChange(newName)
            } else {
                Toast.makeText(context, context.getString(R.string.wrong_name), Toast.LENGTH_SHORT).show()
            }
        }
        dialogBuilder.setNegativeButton(context.getString(R.string.cancel)) { dialog, _ -> dialog.cancel() }

        val alertDialog = dialogBuilder.create()
        alertDialog.show()
    }
}