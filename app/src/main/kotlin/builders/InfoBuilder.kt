package builders

import android.app.AlertDialog
import android.content.Context
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.akap.R

class InfoBuilder (private val context: Context, private val string: String) {
    fun built() {
        val dialogBuilder = AlertDialog.Builder(context)

        dialogBuilder.setTitle(context.getString(R.string.info_title))
        val textView = TextView(context).apply {
            text = string
            setPadding(32, 32, 32, 32)
            textSize = 16f
        }
        dialogBuilder.setView(textView)
        dialogBuilder.setNegativeButton(context.getString(R.string.close)) { dialog, _ -> dialog.cancel() }

        val alertDialog = dialogBuilder.create()
        alertDialog.show()
    }
}