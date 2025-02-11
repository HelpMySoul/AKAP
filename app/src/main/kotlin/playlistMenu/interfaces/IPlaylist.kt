package playlistMenu.interfaces

import java.io.Serializable


interface IPlaylist {
    val name:   String
    var songs:  MutableList<ISong>

    fun addSong(song: ISong)
    fun removeSong(song: ISong)

    fun shuffle()

    fun getIndex():             Int

    fun findSong(song: ISong):  ISong?
    fun getSongAt(index: Int):  ISong?
    fun getNext():              ISong?
    fun getBefore():            ISong?
    fun getFirstSong():         ISong?
    fun getCurrentSong():       ISong?

    fun findSongByKeyword(keyword: String): MutableList<ISong>

    fun findSongByID(songID: Long): ISong?

}