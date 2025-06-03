package player.managers

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.util.Log
import com.example.akap.R
import org.json.JSONArray
import player.classes.Playlist
import player.interfaces.IPlaylist
import player.interfaces.ISong
import player.services.MusicFinderService
import java.util.Locale

object PlaylistManager {
    private lateinit var playlists:         MutableList<IPlaylist>
    private lateinit var allSongs:          IPlaylist
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var allPlaylistsNames: Set<String>
    private var          initialized:       Boolean = false

    fun initialize(context: Context) {
        if (initialized)
        {
            return
        }
        playlists = mutableListOf()

        sharedPreferences = context.getSharedPreferences("playlists", Context.MODE_PRIVATE)
        loadPlaylistsFromPreferences()

        val musicService = MusicFinderService(context)

        val allSongsName = context.getString(R.string.all_songs)

        Log.e("Language", allSongsName)

        allSongs = Playlist(allSongsName)

        allSongs.songs.addAll(musicService.findAllMusic())

        playlists.add(allSongs)

        allPlaylistsNames = getAllPlaylistsNames(context)
    }

    private fun getAllPlaylistsNames(context: Context): Set<String> {
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
            if (playlist is Playlist && !playlist.isTemporary && allPlaylistsNames.any { allSongsName -> playlist.name != allSongsName }) {
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

