package builders

import android.app.AlertDialog
import android.content.Context
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Toast
import com.example.akap.R

class SelectionBuilder (
    private val context:  Context,
    private val name:     String,
    private val variants: List<String>,
    private val action:   (String) -> Unit
    ) {
    private var selectionRule: String = variants[0]

    fun built() {
        val dialogBuilder = AlertDialog.Builder(context, R.style.AlertDialogTheme)

        val spinner = Spinner(context)

        val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, variants)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.setSelection(0)

        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(
                50,
                50,
                50,
                50
            )
            addView(spinner)
        }

        dialogBuilder.setView(container)
        dialogBuilder.setTitle(name)

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectionRule = parent?.getItemAtPosition(position).toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) { }
        }

        dialogBuilder.setPositiveButton(context.getString(R.string.save)) { _, _ ->
            action.invoke(selectionRule)
        }
        dialogBuilder.setNegativeButton(context.getString(R.string.cancel)) { dialog, _ -> dialog.cancel() }

        val alertDialog = dialogBuilder.create()
        alertDialog.show()
    }
}