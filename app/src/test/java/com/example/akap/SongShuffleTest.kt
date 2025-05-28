package com.example.akap

import org.junit.Test

import org.junit.Assert.*
import player.classes.Playlist
import player.classes.Song


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