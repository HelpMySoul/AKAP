package playlistMenu.managers

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

object MenuFragmentManager {

    fun openFragment(fragmentManager: FragmentManager, containerId: Int, fragmentTag: String, fragmentFactory: () -> Fragment) {
        val existingFragment = fragmentManager.findFragmentByTag(fragmentTag)
        if (existingFragment != null) {
            fragmentManager.beginTransaction().remove(existingFragment).commit()
        }

        val newFragment = fragmentFactory()

        fragmentManager.beginTransaction()
            .replace(containerId, newFragment, fragmentTag)
            .addToBackStack(fragmentTag)
            .commit()
    }
}