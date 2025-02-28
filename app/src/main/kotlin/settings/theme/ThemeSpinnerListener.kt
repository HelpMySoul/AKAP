package settings.theme

import android.app.Activity
import android.content.Context
import android.view.View
import android.widget.AdapterView
import settings.theme.ThemeManager.getTheme
import settings.theme.ThemeManager.restartActivity

class ThemeSpinnerListener(private val themes: List<String>,
                           private val context: Context
                          ) : AdapterView.OnItemSelectedListener {

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val theme = themes[position]

        if (theme != getTheme(context)) {
            ThemeManager.applyTheme(theme, context)
            ThemeManager.saveTheme(context, theme)
            restartActivity(context as Activity)
        }
    }
    override fun onNothingSelected(parent: AdapterView<*>?) {}
}