package playlistMenu.controllers

import android.content.Context
import android.os.Bundle
import com.example.akap.R
import playlistMenu.interfaces.IPlaylist
import playlistMenu.managers.GlobalManager
import screens.CurrentPlaylist

class SongSearchController(private val context: Context, private val playlistController: PlaylistController) {
    private var origPlaylist: IPlaylist?            = null
    private val tempPlaylists: MutableList<String>  = mutableListOf()

    fun search(query: String, currentPlaylist: IPlaylist?): IPlaylist? {
        if (query.isEmpty()) {
            return restoreOriginalPlaylist()
        }

        if (origPlaylist == null) {
            origPlaylist = currentPlaylist
        }

        val searchPlaylistName = "${context.getString(R.string.found_by)} $query"
        removeUnusedPlaylistsExcept(searchPlaylistName)

        val foundSongs = origPlaylist?.findSongByKeyword(query) ?: mutableListOf()

        if (foundSongs.isEmpty()) {
            return null
        }

        playlistController.createPlaylist(searchPlaylistName, foundSongs)
        tempPlaylists.add(searchPlaylistName)
        return playlistController.getPlaylist(searchPlaylistName)
    }

    private fun restoreOriginalPlaylist(): IPlaylist? {
        removeUnusedPlaylistsExcept(null)

        val restored = origPlaylist
        origPlaylist = null

        return restored
    }

    private fun removeUnusedPlaylistsExcept(except: String?) {
        val iterator = tempPlaylists.iterator()

        while (iterator.hasNext()) {
            val playlistName = iterator.next()

            if (playlistName != except) {
                playlistController.deletePlaylist(playlistName)
                iterator.remove()
            }
        }
    }
}
