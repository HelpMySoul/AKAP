package com.example.akap

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import settings.theme.locale.LanguageManager
import java.util.Locale

@RunWith(AndroidJUnit4::class)
class LanguageTest {

    private val languageManager = LanguageManager

    private var appContext:          Context = InstrumentationRegistry.getInstrumentation().targetContext
    private var currentLocale:       Locale  = appContext.resources.configuration.locales.get(0)
    private var currentLanguageCode: String  = currentLocale.language

    @Before
    fun setUp() {
        appContext          = InstrumentationRegistry.getInstrumentation().targetContext
        currentLocale       = appContext.resources.configuration.locales.get(0)
        currentLanguageCode = currentLocale.language
    }

    @Test
    fun languageChangeTest() {
        languageManager.saveLanguage(appContext, currentLanguageCode)
        languageManager.setLocale(appContext, currentLanguageCode)

        assertEquals(currentLanguageCode, languageManager.getSavedLanguage(appContext))
    }

    @Test
    fun languageGetTest() {
        languageManager.saveLanguage(appContext, currentLanguageCode)

        assertEquals(currentLanguageCode, languageManager.getSavedLanguage(appContext))
    }
}
