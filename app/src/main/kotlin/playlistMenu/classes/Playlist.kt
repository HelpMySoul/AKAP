package playlistMenu.classes

import android.util.Log
import playlistMenu.controllers.PlaylistController
import playlistMenu.interfaces.IPlaylist
import playlistMenu.interfaces.ISong

class Playlist(override val name: String) : IPlaylist {
    override var songs: MutableList<ISong> = mutableListOf()
    private var songIndex: Int = 0

    override fun addSong(song: ISong) {
        songs.add(song)
    }

    override fun removeSong(song: ISong) {
        songs.remove(song)
        if (songIndex >= songs.size) {
            songIndex = (songs.size - 1).coerceAtLeast(0)
        }
    }

    override fun getFirstSong(): ISong? {
        return if (songs.isNotEmpty()) {
            songIndex = 0
            songs[0]
        } else {
            null
        }
    }

    override fun getSongAt(index: Int): ISong? {
        return if (index in songs.indices) {
            songIndex = index
            songs[index]
        } else {
            null
        }
    }

    override fun shuffle() {
        if (songs.size > 1) {
            songs.shuffle()
            songIndex = 0
        }
    }

    override fun getNext(): ISong? {
        Log.e("Playlist", "index: $songIndex : ${songs.size}")
        return if (songIndex + 1 < songs.size) {
            songIndex++
            songs[songIndex]
        } else {
            null
        }
    }


    override fun getBefore(): ISong? {
        return if (songIndex > 0) {
            songIndex--
            songs[songIndex]
        } else {
            null
        }
    }

    override fun findSong(song: ISong): ISong? {
        val foundSong = songs.find { it == song }
        if (foundSong != null) {
            songIndex = songs.indexOf(foundSong)
        }
        return foundSong
    }

    override fun getIndex(): Int {
        return songIndex
    }

    override fun getCurrentSong(): ISong? {
        return getSongAt(songIndex)
    }

    override fun findSongByKeyword(keyword: String): MutableList<ISong> {
        val foundedSongs: List<ISong> = songs.filter { song ->
            song.title.contains(keyword, ignoreCase = true) || song.artist.contains(keyword, ignoreCase = true)
        }

        return foundedSongs.toMutableList()
    }
}
