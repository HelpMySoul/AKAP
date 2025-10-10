package player.controllers

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.View
import androidx.fragment.app.FragmentManager
import com.example.akap.R
import global.GlobalManager
import notification.services.NotificationService
import player.managers.SongManager
import screens.fragments.CurrentPlaylist
import screens.fragments.PlayerMain

class PlayerEventController(
    private val context:         Context,
    private val fragmentManager: FragmentManager
) {
    fun onPauseOrPlaySong() {
        if (!SongManager.canPlay) {
            onUpdateSong()
            getPlaylistFragment()?.playSong()
        } else if (SongManager.isPaused) {
            onUnpauseSong()
            Log.e("PlayerEventController", SongManager.canPlay.toString())
        } else {
            onPauseSong()
        }
    }

    fun onNextSong() {
        getPlayerFragment()?.apply {
            nextSong()
            playSong()
        }
        getPlaylistFragment()?.playNextSong()
    }

    fun onPreviousSong() {
        getPlayerFragment()?.apply {
            prevSong()
            playSong()
        }
        getPlaylistFragment()?.prevSong()
    }
    fun onRepeatSong() {
        getPlayerFragment()?.apply {
            repeatSong()
            playSong()
        }
        getPlaylistFragment()?.repeatSong()
    }

    fun onPlaylistShuffleClicked() {
        getPlaylistFragment()?.shufflePlaylist()
        onPlaylistRefresh()
    }

    fun onPlaylistRefresh() {
        getPlaylistFragment()?.refresh()
    }

    fun onShowPlayer(playerFrameLayout: View) {
        playerFrameLayout.visibility = View.VISIBLE
    }

    fun onHidePlayer(playerFrameLayout: View) {
        playerFrameLayout.visibility = View.GONE
    }

    fun onPlaySong() {
        getPlayerFragment()?.playSong()
    }

    private fun getPlayerFragment(): PlayerMain? {
        return fragmentManager.findFragmentById(R.id.playerFrameLayout) as? PlayerMain
    }

    private fun getPlaylistFragment(): CurrentPlaylist? {
        return fragmentManager.findFragmentById(R.id.songContainerFragment) as? CurrentPlaylist
    }

    fun onStopSong() {
        getPlayerFragment()?.stopSong()
    }

    fun onPauseSong() {
        getPlayerFragment()?.pauseSong()
    }

    fun onUnpauseSong() {
        getPlayerFragment()?.unpauseSong()
    }

    fun onUpdateSong() {
        getPlayerFragment()?.updateSongController()
    }

    fun onClosePlaylist() {
        getPlayerFragment()?.closePlaylist()
    }

    fun stopNotification(context: Context) {
        val intent = Intent(context, NotificationService::class.java).apply {
            putExtra("show", false)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    fun showCurrentSong() {
        getPlaylistFragment()?.showCurrentSong()
    }

}
