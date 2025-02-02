package topMenu

import Account
import Playlists
import Recommendations
import Settings
import Tools
import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.akap.AllMusic
import com.example.akap.R

object TopMenuManager {

    fun loadButtons(
        context: Context,
        fragmentManager: FragmentManager,
        containerId: Int
    ): List<TopMenuButton> {
        return listOf(
            TopMenuButton(context.getString(R.string.AllMusic_S)) { switchScreen(context, fragmentManager, containerId, AllMusic()) },
            TopMenuButton(context.getString(R.string.Playlists_S)) { switchScreen(context, fragmentManager, containerId, Playlists()) },
            TopMenuButton(context.getString(R.string.Recommendations_S)) { switchScreen(context, fragmentManager, containerId, Recommendations()) },
            TopMenuButton(context.getString(R.string.Settings_S)) { switchScreen(context, fragmentManager, containerId, Settings()) },
            TopMenuButton(context.getString(R.string.Tools_S)) { switchScreen(context, fragmentManager, containerId, Tools()) },
            TopMenuButton(context.getString(R.string.Account_S)) { switchScreen(context, fragmentManager, containerId, Account()) }
        )

    }

    private fun switchScreen(
        context: Context,
        fragmentManager: FragmentManager,
        containerId: Int,
        fragment: Fragment
    ) {
        fragmentManager.beginTransaction()
            .replace(containerId, fragment)
            .addToBackStack(null)
            .commit()
    }

}
