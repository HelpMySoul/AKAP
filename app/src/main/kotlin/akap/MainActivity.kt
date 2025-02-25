package akap

import android.Manifest
import android.app.ActivityManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.media.AudioManager
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
import kotlinx.coroutines.delay
import locale.LanguageManager
import mediaReceiver.MediaButtonHandler
import mediaReceiver.MediaButtonReceiver
import notification.services.NotificationService
import playlistMenu.controllers.BroadcastManagerController
import playlistMenu.controllers.PlayerEventController
import playlistMenu.interfaces.IPlaylist
import playlistMenu.interfaces.ISong
import playlistMenu.interfaces.ISongPlayerListener
import playlistMenu.managers.PlayerSettingsManager
import playlistMenu.managers.PlaylistManager
import playlistMenu.managers.SongManager
import playlistMenu.managers.ThemeManager
import screens.CurrentPlaylist
import screens.PlayerMain
import topMenu.TopMenuManager
import kotlin.system.exitProcess


class MainActivity : AppCompatActivity(), ISongPlayerListener {

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
            "NEXT_SONG"             to { playerEventController.onNextSong()                                          },
            "REPEAT_SONG"           to { playerEventController.onRepeatSong()                                        },
            "PAUSE_OR_PLAY_SONG"    to { playerEventController.onPauseOrPlaySong()                                   },
            "PREVIOUS_SONG"         to { playerEventController.onPreviousSong()                                      },
            "SHUFFLE_PLAYLIST"      to { playerEventController.onPlaylistShuffleClicked()                            },
            "REFRESH_PLAYLIST"      to { playerEventController.onPlaylistRefresh()                                   },
            "SHOW_PLAYER"           to { playerEventController.onShowPlayer(findViewById(R.id.playerFrameLayout))    },
            "PLAY_SONG"             to { playerEventController.onPlaySong()                                          },
            "STOP_SONG"             to { playerEventController.onStopSong()                                          },
            "PAUSE_SONG"            to { playerEventController.onPauseSong()                                         },
            "RESTART_APP"           to { restartApp()                                                                }
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        broadcastManagerController.unregisterAll()
        mediaButtonHandler.release()
        finishAffinity()
        stopService(Intent(this, NotificationService::class.java))
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
        val componentName = packageManager.getLaunchIntentForPackage(packageName)?.component
        val mainIntent = Intent.makeRestartActivityTask(componentName)

        startActivity(mainIntent)
        exitProcess(0)
    }

    override fun updateSong(song: ISong?, playlist: IPlaylist) {
        val playerFragment = supportFragmentManager.findFragmentById(R.id.playerFrameLayout) as? PlayerMain
        playerFragment?.updateSongAndPlaylist(this, song, playlist)
        playerFragment?.playSong()
    }
}
