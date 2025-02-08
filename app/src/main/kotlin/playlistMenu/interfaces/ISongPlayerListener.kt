package playlistMenu.interfaces

interface ISongPlayerListener {
    fun updateUI(song: ISong?, playlist: IPlaylist)
    fun onNextSong()
    fun onRepeatSong()
}