package playlistMenu.controllers

import android.content.Context
import playlistMenu.interfaces.IPlaylist
import playlistMenu.interfaces.ISong
import playlistMenu.managers.PlaylistManager

class PlaylistController(context: Context) {

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
        PlaylistManager.deletePlaylist(playlistName)
    }

}
