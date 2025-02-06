package topMenu

import Account
import screens.Playlists
import Recommendations
import Settings
import Tools
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import screens.CurrentPlaylist
import com.example.akap.R
import playlistMenu.controllers.PlaylistController

object TopMenuManager {

    fun loadButtons(context: Context, fragmentManager: FragmentManager, containerId: Int): List<TopMenuButton> {
        return listOf(
            TopMenuButton(context.getString(R.string.Current_playlist_S)) {
                openCurrentPlaylist(context, fragmentManager, containerId)
            },
            TopMenuButton(context.getString(R.string.Playlists_S)) {
                openPlaylistsScreen(context, fragmentManager, containerId)
            },
            TopMenuButton(context.getString(R.string.Recommendations_S)) { switchScreen(context, fragmentManager, containerId, Recommendations()) },
            TopMenuButton(context.getString(R.string.Settings_S)) { switchScreen(context, fragmentManager, containerId, Settings()) },
            TopMenuButton(context.getString(R.string.Tools_S)) { switchScreen(context, fragmentManager, containerId, Tools()) },
            TopMenuButton(context.getString(R.string.Account_S)) { switchScreen(context, fragmentManager, containerId, Account()) }
        )

    }

    private fun openCurrentPlaylist(context: Context, fragmentManager: FragmentManager, containerId: Int) {
        val playlistController = PlaylistController(context)
        val currentPlaylist = playlistController.getPlaylist(context.getString(R.string.playlist_name))
        currentPlaylist?.name?.let { Log.e("TopMenu", it) }
        val fragment = CurrentPlaylist().apply {
            arguments = Bundle().apply {
                putString("playlist_name", currentPlaylist?.name)
            }
        }

        fragmentManager.beginTransaction()
            .replace(containerId, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun openPlaylistsScreen(context: Context, fragmentManager: FragmentManager, containerId: Int) {
        val existingFragment = fragmentManager.findFragmentByTag("Playlists")

        if (existingFragment != null) {
            fragmentManager.popBackStack("Playlists", 0)
        } else {
            val fragment = Playlists()

            fragmentManager.beginTransaction()
                .replace(containerId, fragment, "Playlists")
                .addToBackStack("Playlists")
                .commit()
        }
    }

    private fun switchScreen(context: Context, fragmentManager: FragmentManager, containerId: Int, fragment: Fragment) {
        fragmentManager.beginTransaction()
            .replace(containerId, fragment)
            .addToBackStack(null)
            .commit()
    }

}
