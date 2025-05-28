package playlistMenu.classes

import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import playlistMenu.controllers.PlaylistController
import playlistMenu.interfaces.IPlaylist
import playlistMenu.interfaces.ISong
import playlistMenu.managers.FilterManager

class Playlist(override var name: String, val isTemporary: Boolean = false) : IPlaylist {
    override var songs:     MutableList<ISong> = mutableListOf()
    private  var songIndex: Int = 0

    val size: Int
        get() {
            return songs.size
        }

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
        val keywords = keyword.split("\\s+".toRegex())

        val (filterRules, excludedIndices) = FilterManager.extractFilterRules(keywords)

        val filteredSongs = if (filterRules.isNotEmpty()) {
            songs.filter { song ->
                filterRules.all { (tag, operator, valueStr) ->
                    val value = valueStr.toLongOrNull() ?: return@all false
                    when (tag) {
                        "duration"      -> FilterManager.compareValues(song.duration,      operator, value*1000)
                        "introduration" -> FilterManager.compareValues(song.introDuration, operator, value*1000)
                        "outroduration" -> FilterManager.compareValues(song.outroDuration, operator, value*1000)
                        else            -> false
                    }
                }
            }
        } else {
            songs.toList()
        }

        val remainingKeywords = keywords.filterIndexed { index, _ ->
            !excludedIndices.contains(index)
        }

        return filteredSongs.filter { song ->
            remainingKeywords.isEmpty() || remainingKeywords.all { word ->
                song.title.contains(word, ignoreCase = true) || song.artist.contains(word, ignoreCase = true)
            }
        }.toMutableList()
    }


    override fun findSongByID(songID: Long): ISong? {
        return songs.firstOrNull { song ->
            song.id == songID
        }
    }

    fun toJson(): JSONObject {
        val json = JSONObject()
        json.put("name", name)

        val songsArray = JSONArray()
        songs.forEach { song ->
            songsArray.put((song as? Song)?.toJson())
        }

        json.put("songs", songsArray)
        return json
    }

    companion object {
        fun fromJson(json: JSONObject): Playlist {
            val playlist   = Playlist(json.getString("name"))
            val songsArray = json.getJSONArray("songs")

            for (i in 0 until songsArray.length()) {
                val songJson = songsArray.getJSONObject(i)
                playlist.addSong(Song.fromJson(songJson))
            }

            return playlist
        }
    }
}
