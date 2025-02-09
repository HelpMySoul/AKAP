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

    fun createPlaylist(name: String, songs: List<ISong>) {
        PlaylistManager.createPlaylist(name, songs)
    }

    fun addSongToPlaylist(playlistName: String, song: ISong) {
        PlaylistManager.addSongToPlaylist(playlistName, song)
    }

    fun deletePlaylist(playlistName: String) {
        PlaylistManager.deletePlaylist(playlistName)
    }

}
