package akap

import android.Manifest
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
import com.example.akap.R
import settings.theme.locale.LanguageManager
import mediaReceiver.MediaButtonHandler
import notification.services.NotificationService
import broadcast.BroadcastManagerController
import playlistMenu.controllers.PlayerEventController
import settings.player.PlayerSettingsManager
import playlistMenu.managers.PlaylistManager
import settings.theme.ThemeManager
import screens.fragments.CurrentPlaylist
import screens.fragments.PlayerMain
import topMenu.TopMenuManager
import kotlin.system.exitProcess


class MainActivity : AppCompatActivity() {

    private lateinit var broadcastManagerController: BroadcastManagerController
    private lateinit var playerEventController:      PlayerEventController
    private lateinit var mediaButtonHandler:         MediaButtonHandler

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
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun initializeComponents() {
        PlayerSettingsManager.loadSettings(this)

        PlaylistManager.initialize(this)

        TopMenuManager.createTopMenuButtons(this, findViewById(R.id.topMenuLayout), supportFragmentManager)

        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.songContainerFragment, CurrentPlaylist())
        transaction.replace(R.id.playerFrameLayout, PlayerMain())
        transaction.commit()

        mediaButtonHandler = MediaButtonHandler(this)
        mediaButtonHandler.initialize()


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 0)
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_MEDIA_AUDIO), 1001)
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
            "STOP_NOTIFICATION"     to { stopNotification()                                                       },
            "RESTART_APP"           to { restartApp()                                                             }
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        broadcastManagerController.unregisterAll()
        if (::mediaButtonHandler.isInitialized) {
            mediaButtonHandler.release()
        }

        stopNotification()
        finishAffinity()
        Log.e("NotificationServiceError","Destroyed MainAct")
    }

    private fun stopNotification() {
        val intent = Intent(this, NotificationService::class.java).apply {
            putExtra("show", false)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            this.startForegroundService(intent)
        } else {
            this.startService(intent)
        }
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
        val componentName = packageManager.getLaunchIntentForPackage(packageName)?.component
        val mainIntent    = Intent.makeRestartActivityTask(componentName)

        startActivity(mainIntent)
        exitProcess(0)
    }
}
