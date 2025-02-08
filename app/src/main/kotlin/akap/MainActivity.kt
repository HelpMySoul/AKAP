package akap

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import screens.CurrentPlaylist
import com.example.akap.R
import locale.LanguageManager
import playlistMenu.interfaces.IPlaylist
import playlistMenu.interfaces.ISong
import playlistMenu.interfaces.ISongPlayerListener
import playlistMenu.managers.PlaylistManager
import playlistMenu.managers.SongManager
import screens.PlayerMain
import topMenu.TopMenuManager
import kotlin.system.exitProcess


class MainActivity : AppCompatActivity(), ISongPlayerListener {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val savedLanguage = LanguageManager.getSavedLanguage(this)
        LanguageManager.setLocale(this, savedLanguage)

        localBroadcastManagerSetup()



        enableEdgeToEdge()
        setContentView(R.layout.activity_main)



        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        createTopMenuButtons()
        PlaylistManager.initialize(this)
        Toast.makeText(this, "Current language: $savedLanguage", Toast.LENGTH_SHORT).show()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 0)
        }


        if (savedInstanceState == null) {
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.songContainerFragment, CurrentPlaylist())
            transaction.replace(R.id.playerLayout, PlayerMain())
            transaction.commit()
        }


    }
    private fun  localBroadcastManagerSetup() {
        LocalBroadcastManager.getInstance(this).registerReceiver(
            nextSongReceiver,
            IntentFilter("NEXT_SONG")
        )

        LocalBroadcastManager.getInstance(this).registerReceiver(
            playlistShuffleReceiver,
            IntentFilter("SHUFFLE_PLAYLIST")
        )

    }
    private val nextSongReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (SongManager.isRepeating) {
                onRepeatSong()
            } else {
                onNextSong()
            }

        }
    }

    private val playlistShuffleReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            onPlaylistShuffleClicked()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 0) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                restartApp()
            } else {
                showPermissionDeniedMessage()
            }
        }
    }
    private fun restartApp() {
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        val componentName = intent?.component
        val mainIntent = Intent.makeRestartActivityTask(componentName)
        startActivity(mainIntent)
        exitProcess(0)
    }

    private fun showPermissionDeniedMessage() {
        Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
    }

    private fun createTopMenuButtons() {
        val topMenuLayout: LinearLayout = findViewById(R.id.topMenuLayout)



        val buttons = TopMenuManager.loadButtons(this, supportFragmentManager, R.id.songContainerFragment )

        for (button in buttons) {
            val btn = Button(this).apply {
                text = button.name
                setOnClickListener { button.action() }
            }
            topMenuLayout.addView(btn)
        }

    }

    override fun updateSong(song: ISong, playlist: IPlaylist) {
        val playerFragment = supportFragmentManager.findFragmentById(R.id.playerLayout) as? PlayerMain
        playerFragment?.updateSongAndPlaylist(this, song, playlist)
    }


    override fun onNextSong() {
        val playerFragment = supportFragmentManager.findFragmentById(R.id.playerLayout) as? PlayerMain
        playerFragment?.nextSong()

        val playlistFragment = supportFragmentManager.findFragmentById(R.id.songContainerFragment) as? CurrentPlaylist
        playlistFragment?.playNextSong()
    }

    override fun onRepeatSong() {
        val playerFragment = supportFragmentManager.findFragmentById(R.id.playerLayout) as? PlayerMain
        playerFragment?.nextSong()

        val playlistFragment = supportFragmentManager.findFragmentById(R.id.songContainerFragment) as? CurrentPlaylist
        playlistFragment?.repeatSong()
    }

    private fun onPlaylistShuffleClicked() {
        val playlistFragment = supportFragmentManager.findFragmentById(R.id.songContainerFragment) as? CurrentPlaylist
        playlistFragment?.refreshPlaylist()
    }

}
