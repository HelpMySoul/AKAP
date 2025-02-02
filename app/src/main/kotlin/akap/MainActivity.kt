package akap

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.TextUtils.replace
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.example.akap.AllMusic
import com.example.akap.R
import locale.LanguageManager
import playlistMenu.interfaces.ISong
import playlistMenu.interfaces.SongPlayerListener
import screens.PlayerMain
import topMenu.TopMenuManager



class MainActivity : AppCompatActivity(), SongPlayerListener {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val savedLanguage = LanguageManager.getSavedLanguage(this)
        LanguageManager.setLocale(this, savedLanguage)

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        createTopMenuButtons()
        Toast.makeText(this, "Current language: $savedLanguage", Toast.LENGTH_SHORT).show()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 0)
        }


        if (savedInstanceState == null) {
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragmentContainer, AllMusic())
            transaction.replace(R.id.playerLayout, PlayerMain())
            transaction.commit()
        }


    }

    private fun createTopMenuButtons() {
        val topMenuLayout: LinearLayout = findViewById(R.id.topMenuLayout)
        val buttons = TopMenuManager.loadButtons(this, supportFragmentManager,
            R.id.fragmentContainer
        )

        for (button in buttons) {
            val btn = Button(this).apply {
                text = button.name
                setOnClickListener { button.action() }
            }
            topMenuLayout.addView(btn)
        }
    }



    private fun loadFragment(fragment: Fragment) {
        val fragmentManager: FragmentManager = supportFragmentManager
        val transaction: FragmentTransaction = fragmentManager.beginTransaction()
        transaction.replace(R.id.fragmentContainer, fragment)
        transaction.commit()
    }

    override fun updateSong(song: ISong) {
        val playerFragment = supportFragmentManager.findFragmentById(R.id.playerLayout) as? PlayerMain
        playerFragment?.updateSong(this, song)
    }
}
