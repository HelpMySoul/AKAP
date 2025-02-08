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
import playlistMenu.managers.GlobalManager
import playlistMenu.managers.MenuFragmentManager


object TopMenuManager {

    fun loadButtons(context: Context, fragmentManager: FragmentManager, containerId: Int): List<TopMenuButton> {
        return listOf(
            TopMenuButton(context.getString(R.string.Current_playlist_S)) {
                openCurrentPlaylist(context, fragmentManager, containerId)
            },
            TopMenuButton(context.getString(R.string.Playlists_S)) {
                openPlaylistsScreen(context, fragmentManager, containerId)
            },
            TopMenuButton(context.getString(R.string.Recommendations_S)) {
                openFragment(fragmentManager, containerId, Recommendations::class.java.simpleName) {
                    Recommendations()
                }
            },
            TopMenuButton(context.getString(R.string.Settings_S)) {
                openFragment(fragmentManager, containerId, Settings::class.java.simpleName) {
                    Settings()
                }
            },
            TopMenuButton(context.getString(R.string.Tools_S)) {
                openFragment(fragmentManager, containerId, Tools::class.java.simpleName) {
                    Tools()
                }
            },
            TopMenuButton(context.getString(R.string.Account_S)) {
                openFragment(fragmentManager, containerId, Account::class.java.simpleName) {
                    Account()
                }
            }
        )
    }

    private fun openCurrentPlaylist(context: Context, fragmentManager: FragmentManager, containerId: Int) {

        openFragment(fragmentManager, containerId, GlobalManager.playlistName) {
            CurrentPlaylist().apply {
                arguments = Bundle().apply {
                    putString("playlist_name", GlobalManager.playlistName)
                }
            }
        }
    }

    private fun openPlaylistsScreen(context: Context, fragmentManager: FragmentManager, containerId: Int) {
        openFragment(fragmentManager, containerId, Playlists::class.java.simpleName) {
            Playlists()
        }
    }

    private fun openFragment(fragmentManager: FragmentManager, containerId: Int, fragmentTag: String, fragmentFactory: () -> Fragment) {
        MenuFragmentManager.openFragment(fragmentManager, containerId, fragmentTag, fragmentFactory)
    }
}
