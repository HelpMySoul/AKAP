package settings.theme.locale

import android.app.Activity
import android.content.Context
import android.view.View
import android.widget.AdapterView
import settings.RestartManager
import settings.theme.locale.LanguageManager.getSavedLanguage
import settings.theme.locale.LanguageManager.saveLanguage
import settings.theme.locale.LanguageManager.setLocale

class LanguageSpinnerListener (private val languageCodes: List<String>,
                               private val context: Context
                              ) : AdapterView.OnItemSelectedListener {

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val language = languageCodes[position]

        if (language != getSavedLanguage(context)) {
            saveLanguage(context, language)
            setLocale(context, language)
            RestartManager.restartApplication(context)
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {}
}