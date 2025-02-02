package playlistMenu.interfaces

import playlistMenu.interfaces.ISong

interface SongPlayerListener {
    fun updateSong(song: ISong)
}