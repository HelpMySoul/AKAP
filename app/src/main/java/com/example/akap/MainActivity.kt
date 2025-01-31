package com.example.akap

import StartMenu.TopMenuManager
import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import locale.LanguageManager

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val languageManager = LanguageManager(this)
        val savedLanguage = languageManager.getSavedLanguage()
        languageManager.setLocale(savedLanguage)

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        createTopMenuButtons()
        Toast.makeText(this, "Current language: $savedLanguage", Toast.LENGTH_SHORT).show()
    }

    private fun createTopMenuButtons() {
        val topMenuLayout: LinearLayout = findViewById(R.id.topMenuLayout)
        val buttons = TopMenuManager.loadButtons(this, supportFragmentManager, R.id.fragmentContainer)

        for (button in buttons) {
            val btn = Button(this).apply {
                text = button.name
                setOnClickListener { button.action() }
            }
            topMenuLayout.addView(btn)
        }
    }


}
