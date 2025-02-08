package playlistMenu.managers

import android.content.Context
import com.example.akap.R
import playlistMenu.classes.Playlist
import playlistMenu.interfaces.IPlaylist
import playlistMenu.interfaces.ISong
import playlistMenu.services.MusicFinderService

object PlaylistManager {
    private val playlists: MutableList<IPlaylist> = mutableListOf()
    private var isInitialized = false

    fun initialize(context: Context) {

        if (isInitialized) return

        val musicService = MusicFinderService(context)
        createPlaylist(context.getString(R.string.all_songs), musicService.findAllMusic())

        isInitialized = true
    }

    fun getPlaylists(): MutableList<IPlaylist> {
        return playlists
    }

    fun getPlaylistByName(name: String): IPlaylist? {
        return playlists.find { it.name == name }
    }

    fun createPlaylist(name: String, songs: List<ISong>) {
        val existingPlaylist = playlists.find { it.name == name }

        if (existingPlaylist != null) {
            playlists.remove(existingPlaylist)
        }

        val playlist = Playlist(name)
        songs.forEach { playlist.addSong(it) }
        playlists.add(playlist)
    }

    fun addSongToPlaylist(playlistName: String, song: ISong) {
        getPlaylistByName(playlistName)?.addSong(song)
    }

    fun getNextSong(playlist: IPlaylist): ISong? {
        return playlist.getNext()
    }

    fun getSongFromPlaylist(song: ISong, playlist: IPlaylist): ISong? {
        return playlist.findSong(song)
    }

    fun deletePlaylist(playlistName: String) {
        playlists.remove(getPlaylistByName(playlistName))
    }
}

