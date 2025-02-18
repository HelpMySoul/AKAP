package tools.controllers

import android.util.Log
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.LifecycleOwner
import com.example.akap.R
import screens.CurrentPlaylist

class SongPickerController(var fragment: Fragment, private var parentFragment: FragmentManager) {

   fun openSongPicker(string: String) {
       val songPickerFragment = CurrentPlaylist { song ->
           fragment.setFragmentResult(
               "SONG_PICKER_REQUEST_KEY",
               bundleOf(string to song.filePath)
           )
           parentFragment.popBackStack()
       }

       parentFragment.beginTransaction()
           .replace(R.id.songContainerFragment, songPickerFragment)
           .addToBackStack(null)
           .commit()
   }

    fun setListeners(viewLifecycleOwner: LifecycleOwner, vararg listeners: Pair<String, (String) -> Unit>) {
        listeners.forEach { (key, function) ->
            parentFragment.setFragmentResultListener("SONG_PICKER_REQUEST_KEY", viewLifecycleOwner) { _, bundle ->
                val selectedSongPath = bundle.getString(key)

                Log.d("SongPicker", "Path: $selectedSongPath")

                if (selectedSongPath != null) {
                    function.invoke(selectedSongPath)
                }
            }
        }
    }
}