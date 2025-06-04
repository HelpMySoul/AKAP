package settings.theme.locale

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.example.akap.R
import player.controllers.PlaylistController
import player.managers.PlaylistManager
import java.util.Locale

object LanguageManager {

    fun setLocale(context: Context, languageCode: String) {
        val locale = Locale(languageCode)

        val resources     = context.resources
        val configuration = resources.configuration

        configuration.setLocale(locale)
        resources.updateConfiguration(configuration, resources.displayMetrics)
    }

    @SuppressLint("ApplySharedPref")
    fun saveLanguage(context: Context, languageCode: String) {
        val pref = context.getSharedPreferences("screens.fragments.Settings", Context.MODE_PRIVATE)
        pref.edit().putString("Language", languageCode).commit()
    }

    fun getSavedLanguage(context: Context): String {
        val pref = context.getSharedPreferences("screens.fragments.Settings", Context.MODE_PRIVATE)
        return pref.getString("Language", "ru") ?: "ru"
    }

    fun setupLanguageSpinner(context: Context, spinner: Spinner) {
        val languages     = context.resources.getStringArray(R.array.languages).toList()
        val languageCodes = context.resources.getStringArray(R.array.languages_codes).toList()

        val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, languages)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        val savedLanguage = getSavedLanguage(context)
        val selectedIndex = languageCodes.indexOf(savedLanguage).takeIf { it >= 0 } ?: 0
        spinner.setSelection(selectedIndex)

        spinner.onItemSelectedListener = LanguageSpinnerListener (
            languageCodes = languageCodes,
            context       = context
        )
    }
}