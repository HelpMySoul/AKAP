package com.example.akap

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import player.classes.Playlist
import player.classes.Song
import player.controllers.PlaylistController
import player.controllers.SongSearchController

@RunWith(AndroidJUnit4::class)
class SongSearchTest {

    private lateinit var context:              Context
    private lateinit var playlistController:   PlaylistController
    private lateinit var songSearchController: SongSearchController

    @Before
    fun setUp() {
        context              = InstrumentationRegistry.getInstrumentation().targetContext
        playlistController   = PlaylistController(context)
        songSearchController = SongSearchController(context, playlistController)
    }

    @Test
    fun positiveSearchTest() {
        val playlist = Playlist("Test")

        for (i in 0..5) {
            val song = Song(i.toLong(), 10, 0, 0, "title $i", "artist", "path")
            playlist.addSong(song)
        }

        val foundSongs: Playlist = songSearchController.search("title", playlist) as Playlist

        assertEquals(6, foundSongs.size)
    }
}