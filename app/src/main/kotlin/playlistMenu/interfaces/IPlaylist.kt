package playlistMenu.interfaces

interface IPlaylist {
    val name: String
    var songs: MutableList<ISong>

    fun addSong(song: ISong)
    fun removeSong(song: ISong)
    fun getFirstSong(): ISong?
    fun getSongAt(index: Int): ISong?
    fun shuffle()
    fun getNext(): ISong?
    fun getBefore(): ISong?
    fun findSong(song: ISong): ISong?
    fun getIndex(): Int
    fun getCurrentSong(): ISong?
}