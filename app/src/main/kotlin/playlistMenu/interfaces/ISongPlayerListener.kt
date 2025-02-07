package playlistMenu.interfaces

interface ISongPlayerListener {
    fun updateSong(song: ISong, playlist: IPlaylist)
    fun onNextSong()
    fun onRepeatSong()
}