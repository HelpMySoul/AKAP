package akap

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import broadcast.BroadcastManagerController
import com.example.akap.R
import bluetooth.MediaButtonHandler
import global.GlobalManager
import player.controllers.PlayerEventController
import player.managers.PlaylistManager
import player.managers.SongManager
import screens.AppFragmentManager
import screens.fragments.CurrentPlaylist
import screens.fragments.PlayerMain
import settings.player.PlayerSettingsManager
import settings.theme.ThemeManager
import settings.theme.locale.LanguageManager
import topMenu.TopMenuManager


class MainActivity : AppCompatActivity() {

    private lateinit var broadcastManagerController: BroadcastManagerController
    private lateinit var playerEventController:      PlayerEventController
    private lateinit var mediaButtonHandler:         MediaButtonHandler
    private var          restartApp:                 Boolean = false

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val savedLanguage = LanguageManager.getSavedLanguage(this)
        LanguageManager.setLocale(this, savedLanguage)

        broadcastManagerController = BroadcastManagerController(this)
        localBroadcastManagerSetup()

        ThemeManager.applyTheme(ThemeManager.getTheme(this), this)

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        if (savedInstanceState == null) {
            initializeComponents()
        }

        GlobalManager.updateDisplayedPlaylistName(GlobalManager.getPlayedPlaylistName())

        AppFragmentManager.openFragment(supportFragmentManager, R.id.songContainerFragment, GlobalManager.getPlayedPlaylistName()) {
            CurrentPlaylist().apply {
                arguments = Bundle().apply {
                    putString("playlist_name", GlobalManager.getPlayedPlaylistName())
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun initializeComponents() {
        PlayerSettingsManager.loadSettings(this)

        PlaylistManager.initialize(this)

        TopMenuManager.createTopMenuButtons(this, findViewById(R.id.topMenuLayout), supportFragmentManager)

        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.playerFrameLayout, PlayerMain())
        transaction.commit()

        mediaButtonHandler = MediaButtonHandler(this)
        mediaButtonHandler.initialize()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 0)
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_MEDIA_AUDIO), 1)
        }
    }

    private fun  localBroadcastManagerSetup() {
        playerEventController       = PlayerEventController(this, supportFragmentManager)
        broadcastManagerController  = BroadcastManagerController(applicationContext)

        broadcastManagerController.registerReceivers(
            "NEXT_SONG"             to { playerEventController.onNextSong()                                       },
            "REPEAT_SONG"           to { playerEventController.onRepeatSong()                                     },
            "PAUSE_OR_PLAY_SONG"    to { playerEventController.onPauseOrPlaySong()                                },
            "PREVIOUS_SONG"         to { playerEventController.onPreviousSong()                                   },
            "SHUFFLE_PLAYLIST"      to { playerEventController.onPlaylistShuffleClicked()                         },
            "REFRESH_PLAYLIST"      to { playerEventController.onPlaylistRefresh()                                },
            "CLOSE_PLAYLIST"        to { playerEventController.onClosePlaylist()                                  },
            "SHOW_PLAYER"           to { playerEventController.onShowPlayer(findViewById(R.id.playerFrameLayout)) },
            "HIDE_PLAYER"           to { playerEventController.onHidePlayer(findViewById(R.id.playerFrameLayout)) },
            "PLAY_SONG"             to { playerEventController.onPlaySong()                                       },
            "STOP_SONG"             to { playerEventController.onStopSong()                                       },
            "PAUSE_SONG"            to { playerEventController.onPauseSong()                                      },
            "UPDATE_SONG"           to { playerEventController.onUpdateSong()                                     },
            "STOP_NOTIFICATION"     to { playerEventController.stopNotification(this)                      },
            "RESTART_APP"           to { restartApp()                                                             },
            "RESTART_ACTIVITY"      to { restartActivity()                                                        }
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        SongManager.stop()
        if (!restartApp) {
            BroadcastManagerController(this).sendBroadcast("STOP_NOTIFICATION")
            broadcastManagerController.unregisterAll()
            if (::mediaButtonHandler.isInitialized) {
                mediaButtonHandler.release()
            }

            finishAffinity()
            restartApp = false
        }
        Log.e("NotificationServiceError","Destroyed MainAct")
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 0) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                restartApp()
            } else {
                Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun restartApp() {
        BroadcastManagerController(this).sendBroadcast("STOP_SONG")

        val pm         = applicationContext.packageManager
        val intent     = pm.getLaunchIntentForPackage(applicationContext.packageName)
        val mainIntent = Intent.makeRestartActivityTask(intent?.component)
        applicationContext.startActivity(mainIntent)
        Runtime.getRuntime().exit(0)
    }

    @SuppressLint("UnsafeIntentLaunch")
    private fun restartActivity() {
        restartApp = true
        val intent = intent
        finish()
        startActivity(intent)
    }
}
