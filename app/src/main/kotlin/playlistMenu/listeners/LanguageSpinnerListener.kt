package playlistMenu.listeners

import android.app.Activity
import android.content.Context
import android.view.View
import android.widget.AdapterView
import locale.LanguageManager.getSavedLanguage
import locale.LanguageManager.restartActivity
import locale.LanguageManager.saveLanguage
import locale.LanguageManager.setLocale

class LanguageSpinnerListener (private val languageCodes: List<String>,
                               private val context: Context
                              ) : AdapterView.OnItemSelectedListener {

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val language = languageCodes[position]

        if (language != getSavedLanguage(context)) {
            saveLanguage(context, language)
            setLocale(context, language)
            restartActivity(context as Activity)
        }
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {}
}