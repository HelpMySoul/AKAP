package topMenu

import screens.fragments.Playlists
import screens.fragments.Settings
import screens.fragments.Tools
import android.content.Context
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.widget.Button
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import screens.fragments.CurrentPlaylist
import com.example.akap.R
import global.GlobalManager
import screens.AppFragmentManager


object TopMenuManager {

    private fun getListOfButtons(context: Context, fragmentManager: FragmentManager, containerId: Int): List<TopMenuButton> {
        return listOf(
            TopMenuButton(context.getString(R.string.current_playlist_string)) {
                openCurrentPlaylist(context, fragmentManager, containerId)
            },
            TopMenuButton(context.getString(R.string.Playlists_S)) {
                openPlaylistsScreen(context, fragmentManager, containerId)
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
            }
        )
    }

    private fun openCurrentPlaylist(context: Context, fragmentManager: androidx.fragment.app.FragmentManager, containerId: Int) {

        openFragment(fragmentManager, containerId, GlobalManager.getPlaylistName()) {
            CurrentPlaylist().apply {
                arguments = Bundle().apply {
                    putString("playlist_name", GlobalManager.getPlaylistName())
                }
            }
        }
    }

    private fun openPlaylistsScreen(context: Context, fragmentManager: androidx.fragment.app.FragmentManager, containerId: Int) {
        openFragment(fragmentManager, containerId, Playlists::class.java.simpleName) {
            Playlists()
        }
    }

    private fun openFragment(
        fragmentManager: FragmentManager,
        containerId:     Int,
        fragmentTag:     String,
        fragmentFactory: () -> Fragment) {
        AppFragmentManager.openFragment(fragmentManager, containerId, fragmentTag, fragmentFactory)
    }

    fun createTopMenuButtons(context: Context, topMenuLayout: LinearLayout, supportFragmentManager: androidx.fragment.app.FragmentManager) {
        val buttons = getListOfButtons(context, supportFragmentManager, R.id.songContainerFragment )

        for (button in buttons) {
            val styledContext = ContextThemeWrapper(context, R.style.AppButtonStyle)
            val btn = Button(styledContext, null, 0).apply {
                text = button.name
                setOnClickListener { button.action() }
            }
            topMenuLayout.addView(btn)
        }
    }
}
