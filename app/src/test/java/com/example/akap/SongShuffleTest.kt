package com.example.akap

import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Test

import org.junit.Assert.*
import playlistMenu.classes.Playlist
import playlistMenu.classes.Song


class SongShuffleTest {

    @Test
    fun songShuffleTest() {
        val currentPlaylist = Playlist("Test")

        for (i in 0..10) {
            val song = Song(i.toLong(),0,0,0,"song: $i", "artist: $i", "path: $i")
            currentPlaylist.addSong(song)
        }

        val shuffledPlaylist = currentPlaylist.shuffle()

        assertNotEquals(shuffledPlaylist, currentPlaylist)
    }
}