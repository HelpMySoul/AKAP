package playlistMenu.controllers

import android.content.Context
import android.util.Log
import android.view.View
import androidx.fragment.app.FragmentManager
import com.example.akap.R
import playlistMenu.managers.GlobalManager
import playlistMenu.managers.SongManager
import screens.CurrentPlaylist
import screens.PlayerMain

class PlayerEventController(
    private val context:            Context,
    private val fragmentManager:    FragmentManager
) {
    fun onNextOrRepeatSong() {
        Log.d("PlayerEventController", "onNextOrRepeatSong called")
        if (SongManager.isRepeating) onRepeatSong() else onNextSong()
    }

    private fun onNextSong() {
        getPlayerFragment()?.apply {
            nextSong()
            playSong()
        }
        getPlaylistFragment()?.playNextSong()
    }

    private fun onRepeatSong() {
        getPlayerFragment()?.apply {
            nextSong()
            playSong()
        }
        getPlaylistFragment()?.repeatSong()
    }

    fun onPlaylistShuffleClicked() {
        getPlaylistFragment()?.playFirstInPlaylist()
    }

    fun onPlaylistRefresh() {
        getPlaylistFragment()?.refresh()
    }

    fun onShowPlayer(playerFrameLayout: View) {
        if (playerFrameLayout.visibility != View.VISIBLE) {
            playerFrameLayout.visibility = View.VISIBLE
            val playerFragment = getPlayerFragment()

            val playlist = PlaylistController(context).getPlaylist(GlobalManager.getPlaylistName())

            if (playlist != null) {
                val song = playlist.getCurrentSong()
                playerFragment?.updateSongAndPlaylist(context, song, playlist)
            }
        }
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
}
