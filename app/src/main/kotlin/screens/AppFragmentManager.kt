package screens

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.akap.R
import screens.fragments.SongSettingsCard

object AppFragmentManager {

    fun openFragment(fragmentManager: FragmentManager,
                     containerId: Int,
                     fragmentTag: String,
                     fragmentFactory: () -> Fragment) {
        val existingFragment = fragmentManager.findFragmentByTag(fragmentTag)
        if (existingFragment != null) {
            fragmentManager.beginTransaction().remove(existingFragment).commit()
        }
        val newFragment = fragmentFactory()
        fragmentManager.beginTransaction()
            .replace(containerId, newFragment, fragmentTag)
            .commit()
    }

    fun addMenuFragment(fragmentManager: FragmentManager,
                        newFragment: Fragment,
                        oldFragment: Fragment) {
        fragmentManager.beginTransaction()
            .add(R.id.songContainerFragment, newFragment)
            .addToBackStack(null)
            .hide(oldFragment)
            .commit()
    }
}