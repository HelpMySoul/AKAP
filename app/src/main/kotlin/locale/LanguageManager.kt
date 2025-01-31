package locale

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.example.akap.R
import java.util.Locale

class LanguageManager(private val context: Context) {

    fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val resources = context.resources
        val configuration = resources.configuration
        configuration.setLocale(locale)
        resources.updateConfiguration(configuration, resources.displayMetrics)
    }

    fun saveLanguage(languageCode: String) {
        val sharedPreferences = context.getSharedPreferences("Settings", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("Language", languageCode).apply()
    }

    fun getSavedLanguage(): String {
        val sharedPreferences = context.getSharedPreferences("Settings", Context.MODE_PRIVATE)
        return sharedPreferences.getString("Language", "en") ?: "en"
    }

    fun setupLanguageSpinner(spinner: Spinner) {
        val languages = context.resources.getStringArray(R.array.languages).toList()
        val languageCodes = context.resources.getStringArray(R.array.languages_codes).toList()

        val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, languages)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        val savedLanguage = getSavedLanguage()
        val selectedIndex = languageCodes.indexOf(savedLanguage).takeIf { it >= 0 } ?: 0
        spinner.setSelection(selectedIndex)

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedLanguage = languageCodes[position]

                if (selectedLanguage != getSavedLanguage()) {
                    saveLanguage(selectedLanguage)
                    setLocale(selectedLanguage)
                    restartActivity(context as Activity)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun restartActivity(activity: Activity) {
        val intent = activity.intent
        activity.finish()
        activity.startActivity(intent)
    }
}
