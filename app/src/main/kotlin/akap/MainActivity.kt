package akap

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.akap.R
import locale.LanguageManager
import notification.services.NotificationService
import playlistMenu.adapters.SongAdapter
import playlistMenu.controllers.BroadcastManagerController
import playlistMenu.controllers.PlayerEventController
import playlistMenu.controllers.PlaylistController
import playlistMenu.interfaces.IPlaylist
import playlistMenu.interfaces.ISong
import playlistMenu.interfaces.ISongPlayerListener
import playlistMenu.managers.GlobalManager
import playlistMenu.managers.PlayerSettingsManager
import playlistMenu.managers.PlaylistManager
import playlistMenu.managers.SongManager
import screens.CurrentPlaylist
import screens.PlayerMain
import topMenu.TopMenuManager
import kotlin.system.exitProcess


class MainActivity : AppCompatActivity(), ISongPlayerListener {

    private lateinit var broadcastManagerController: BroadcastManagerController
    private lateinit var playerEventController:      PlayerEventController

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        PlayerSettingsManager.loadSettings(this)

        val savedLanguage = LanguageManager.getSavedLanguage(this)
        LanguageManager.setLocale(this, savedLanguage)

        broadcastManagerController = BroadcastManagerController(this)
        localBroadcastManagerSetup()

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        TopMenuManager.createTopMenuButtons(this, findViewById(R.id.topMenuLayout), supportFragmentManager)

        PlaylistManager.initialize(this)

        Toast.makeText(this, "${this.getString(R.string.current_language_text)}: $savedLanguage", Toast.LENGTH_SHORT).show()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 0)
        }

        if (savedInstanceState == null) {
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.songContainerFragment, CurrentPlaylist())
            transaction.replace(R.id.playerFrameLayout, PlayerMain())
            transaction.commit()
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
        stopService(Intent(this, NotificationService::class.java))
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
