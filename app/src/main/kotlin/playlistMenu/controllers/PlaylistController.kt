package playlistMenu.controllers

import android.content.Context
import android.widget.Toast
import com.example.akap.R
import playlistMenu.interfaces.IPlaylist
import playlistMenu.interfaces.ISong
import playlistMenu.managers.PlaylistManager

class PlaylistController(var context: Context) {

    init {
        PlaylistManager.initialize(context)
    }

    fun getAllPlaylists(): List<IPlaylist> {
        return PlaylistManager.getPlaylists()
    }

    fun getPlaylist(name: String): IPlaylist? {
        return PlaylistManager.getPlaylistByName(name)
    }

    fun createPlaylist(name: String, songs: List<ISong>, isTemporary: Boolean = false) {
        PlaylistManager.createPlaylist(name, songs, isTemporary)
    }

    fun createPlaylist(name: String, playlist: IPlaylist?, isTemporary: Boolean = false) {
        if (playlist != null) {
            PlaylistManager.createPlaylist(name, playlist, isTemporary)
        }
    }

    fun addSongToPlaylist(playlistName: String, song: ISong) {
        PlaylistManager.addSongToPlaylist(playlistName, song)
    }

    fun deletePlaylist(playlistName: String) {
        if (playlistName != context.getString(R.string.all_songs)) {
            PlaylistManager.deletePlaylist(playlistName)
        } else {
            Toast.makeText(context, context.getString(R.string.try_to_delete_playlist), Toast.LENGTH_SHORT).show()
        }
    }

    fun updatePlaylistName(name: String, newName: String) {
        PlaylistManager.updatePlaylistName(name, newName)
    }
}
