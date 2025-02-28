package playlistMenu.managers

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.FragmentActivity
import com.example.akap.R
import playlistMenu.listeners.ThemeSpinnerListener

object ThemeManager {

    private fun getThemes(context: Context): List<String> {
        return context.resources.getStringArray(R.array.app_themes).toList()
    }
    private fun getThemesNames(context: Context): List<String> {
        return context.resources.getStringArray(R.array.themes).toList()
    }

    fun setupThemeSpinner(context: Context, themeSpinner: Spinner) {
        val themes      = getThemes(context)
        val themesNames = getThemesNames(context)

        val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, themesNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        themeSpinner.adapter = adapter

        val savedTheme      = getTheme(context)
        val savedThemeIndex = themes.indexOf(savedTheme).takeIf { it >= 0 } ?: 0

        themeSpinner.setSelection(savedThemeIndex)

        themeSpinner.onItemSelectedListener = ThemeSpinnerListener(
            themes  = themes,
            context = context
        )
    }

    fun applyTheme(theme: String, context: Context) {

        when (theme) {
            "Red Theme"   -> context.setTheme(R.style.RedTheme)
            "Blue Theme"  -> context.setTheme(R.style.BlueTheme)
            "White Theme" -> context.setTheme(R.style.WhiteTheme)
            "Dark Theme"  -> context.setTheme(R.style.DarkTheme)
            else          -> context.setTheme(R.style.AppTheme)
        }

        Log.d("ThemeDebug", "Applied theme: $theme")
    }

    fun saveTheme(context: Context, theme: String) {
        val pref: SharedPreferences = context.getSharedPreferences("Settings", Context.MODE_PRIVATE)
        pref.edit().putString("Theme", theme).apply()
    }

    fun getTheme(context: Context): String {
        val pref: SharedPreferences = context.getSharedPreferences("Settings", Context.MODE_PRIVATE)
        return pref.getString("Theme", "White Theme") ?: "White Theme"
    }

    fun restartActivity(activity: Activity) {
        val intent = activity.intent
        activity.finish()
        activity.startActivity(intent)
    }
}