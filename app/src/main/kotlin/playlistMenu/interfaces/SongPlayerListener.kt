package playlistMenu.interfaces

import playlistMenu.adapters.SongAdapter
import playlistMenu.interfaces.ISong

interface SongPlayerListener {
    fun updateSong(song: ISong, playlist: IPlaylist)
    fun onNextSongClicked()
}