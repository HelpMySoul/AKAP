package playlistMenu.managers

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import com.example.akap.R
import org.json.JSONArray
import playlistMenu.classes.Playlist
import playlistMenu.interfaces.IPlaylist
import playlistMenu.interfaces.ISong
import playlistMenu.services.MusicFinderService
import java.io.File
import java.io.IOException
import java.util.Locale

object PlaylistManager {
    private val playlists: MutableList<IPlaylist> = mutableListOf()
    private var isInitialized = false

    private lateinit var sharedPreferences: SharedPreferences

    fun initialize(context: Context) {
        if (isInitialized) return

        sharedPreferences = context.getSharedPreferences("playlists", Context.MODE_PRIVATE)
        loadPlaylistsFromPreferences()

        val musicService = MusicFinderService(context)

        val allSongsName = context.getString(R.string.all_songs)

        val allNames = getAllNames(context)

        for (playlistName in allNames) {
            if (playlistName != allSongsName) {
                deletePlaylist(playlistName)
            }
        }

        val allSongs = getPlaylistByName(allSongsName)

        if (allSongs == null) {
            createPlaylist(allSongsName, musicService.findAllMusic(), true)
        } else {
            allSongs.songs.clear()
            allSongs.songs.addAll(musicService.findAllMusic())
        }
        isInitialized = true
    }

    private fun getAllNames(context: Context): Set<String> {
        val names   = mutableSetOf<String>()
        val locales = context.resources.assets.locales

        for (locale in locales) {
            val config = Configuration(context.resources.configuration)
            config.setLocale(Locale(locale))

            val contextLocale = context.createConfigurationContext(config)

            val name = contextLocale.getString(R.string.all_songs)
            names.add(name)
        }
        return names
    }

    private fun loadPlaylistsFromPreferences() {
        val playlistsJson = sharedPreferences.getString("playlists", null)
        if (playlistsJson != null) {
            val playlistsArray = JSONArray(playlistsJson)
            for (i in 0 until playlistsArray.length()) {
                val playlistJson = playlistsArray.getJSONObject(i)
                playlists.add(Playlist.fromJson(playlistJson))
            }
        }
    }

    fun savePlaylistsToPreferences() {
        val playlistsArray = JSONArray()
        playlists.forEach { playlist ->
            if (playlist is Playlist && !playlist.isTemporary) {
                playlistsArray.put(playlist.toJson())
            }
        }
        sharedPreferences.edit().putString("playlists", playlistsArray.toString()).apply()
    }

    fun getPlaylists(): MutableList<IPlaylist> {
        return playlists
    }

    fun getPlaylistByName(name: String): IPlaylist? {
        return playlists.find { it.name == name }
    }

    fun createPlaylist(name: String, songs: List<ISong>, isTemporary: Boolean = false) {
        val existingPlaylist = playlists.find { it.name == name }

        if (existingPlaylist != null) {
            playlists.remove(existingPlaylist)
        }

        val playlist = Playlist(name, isTemporary)
        songs.forEach { playlist.addSong(it) }
        playlists.add(playlist)

        if (!isTemporary) {
            savePlaylistsToPreferences()
        }
    }

    fun createPlaylist(name: String, playlist: IPlaylist, isTemporary: Boolean = false) {
        val existingPlaylist = playlists.find { it.name == name }

        if (existingPlaylist != null) {
            playlists.remove(existingPlaylist)
        }

        val newPlaylist: IPlaylist = Playlist(name, isTemporary)

        playlist.songs.let { songs ->
            songs.forEach { song ->
                newPlaylist.addSong(song)
            }
        }

        playlists.add(newPlaylist)

        if (!isTemporary) {
            savePlaylistsToPreferences()
        }
    }

    fun addSongToPlaylist(playlistName: String, song: ISong) {
        getPlaylistByName(playlistName)?.addSong(song)
        savePlaylistsToPreferences()
    }

    fun getNextSong(playlist: IPlaylist): ISong? {
        return playlist.getNext()
    }

    fun getSongFromPlaylist(song: ISong, playlist: IPlaylist): ISong? {
        return playlist.findSong(song)

    }

    fun deletePlaylist(playlistName: String) {
        if (getPlaylistByName(playlistName) == null) {
            return
        }

        playlists.remove(getPlaylistByName(playlistName))
        savePlaylistsToPreferences()
    }

    fun updatePlaylistName(name: String, newName: String) {
        getPlaylistByName(name)?.name = newName
        savePlaylistsToPreferences()
    }
}

