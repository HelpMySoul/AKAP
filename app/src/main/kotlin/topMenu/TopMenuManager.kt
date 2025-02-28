package topMenu

import screens.fragments.Account
import screens.fragments.Playlists
import screens.fragments.Recommendations
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
import screens.MenuFragmentManager


object TopMenuManager {

    private fun loadButtons(context: Context, fragmentManager: FragmentManager, containerId: Int): List<TopMenuButton> {
        return listOf(
            TopMenuButton(context.getString(R.string.current_playlist_string)) {
                openCurrentPlaylist(context, fragmentManager, containerId)
            },
            TopMenuButton(context.getString(R.string.playlists_string)) {
                openPlaylistsScreen(context, fragmentManager, containerId)
            },
            TopMenuButton(context.getString(R.string.recommendations_string)) {
                openFragment(fragmentManager, containerId, Recommendations::class.java.simpleName) {
                    Recommendations()
                }
            },
            TopMenuButton(context.getString(R.string.settings_string)) {
                openFragment(fragmentManager, containerId, Settings::class.java.simpleName) {
                    Settings()
                }
            },
            TopMenuButton(context.getString(R.string.tools_string)) {
                openFragment(fragmentManager, containerId, Tools::class.java.simpleName) {
                    Tools()
                }
            },
            TopMenuButton(context.getString(R.string.account_string)) {
                openFragment(fragmentManager, containerId, Account::class.java.simpleName) {
                    Account()
                }
            }
        )
    }

    private fun openCurrentPlaylist(context: Context, fragmentManager: FragmentManager, containerId: Int) {

        openFragment(fragmentManager, containerId, GlobalManager.getPlaylistName()) {
            CurrentPlaylist().apply {
                arguments = Bundle().apply {
                    putString("playlist_name", GlobalManager.getPlaylistName())
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

    fun createTopMenuButtons(context: Context, topMenuLayout: LinearLayout, supportFragmentManager: FragmentManager) {
        val buttons = loadButtons(context, supportFragmentManager, R.id.songContainerFragment)

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
