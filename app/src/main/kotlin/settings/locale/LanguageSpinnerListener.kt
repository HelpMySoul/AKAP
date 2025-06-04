package settings.theme.locale

import android.app.Activity
import android.content.Context
import android.view.View
import android.widget.AdapterView
import settings.RestartManager

class LanguageSpinnerListener (private val languageCodes: List<String>,
                               private val context: Context
                              ) : AdapterView.OnItemSelectedListener {

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val language = languageCodes[position]

        if (language != LanguageManager.getSavedLanguage(context)) {
            LanguageManager.saveLanguage(context, language)
            LanguageManager.setLocale(context, language)
            RestartManager.restartApplication(context)
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {}
}