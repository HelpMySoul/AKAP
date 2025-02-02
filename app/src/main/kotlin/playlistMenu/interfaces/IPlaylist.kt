package playlistMenu.interfaces

interface IPlaylist {
    val name: String
    val songs: MutableList<ISong>

    fun addSong(song: ISong)
    fun removeSong(song: ISong)
    fun play()
    fun shuffle()
}