package com.example.akap

import org.junit.Assert.assertEquals
import org.junit.Test
import player.classes.Song
import kotlin.random.Random

class SongParamTest {

    private val song        = Song(0,10,0,0, "title", "artist", "path")
    private val introTime   = Random.nextLong(1, 4)
    private val outroTime   = Random.nextLong(1, 4)
    private val localVolume = Random.nextInt(1, 100)


    // Intro set tests
    @Test
    fun positiveIntroSetTest() {

        song.introDuration = introTime

        assertEquals(introTime, song.introDuration)
    }

    @Test
    fun belowZeroTimeIntroSetTest() {

        song.introDuration = -introTime

        assertEquals(0, song.introDuration)
    }

    @Test
    fun afterEndTimeIntroSetTest() {

        song.introDuration = song.duration + introTime

        assertEquals(song.duration, song.introDuration)
    }

    // Outro set tests
    @Test
    fun positiveOutroSetTest() {

        song.outroDuration = outroTime

        assertEquals(outroTime, song.outroDuration)
    }

    @Test
    fun belowZeroTimeOutroSetTest() {

        song.outroDuration = -outroTime

        assertEquals(0, song.outroDuration)
    }

    @Test
    fun afterEndTimeOutroSetTest() {

        song.outroDuration = song.duration + outroTime

        assertEquals(song.duration, song.outroDuration)
    }

    // Intro and outro conflict tests
    @Test
    fun positiveOutroAndIntroSetTest() {
        song.introDuration = introTime
        song.outroDuration = outroTime

        assertEquals(introTime, song.introDuration)
        assertEquals(outroTime, song.outroDuration)
    }

    @Test
    fun negativeIntroAndOutroSetTest() {
        song.introDuration = song.duration - outroTime + 1
        song.outroDuration = outroTime

        assertEquals(0, song.outroDuration)
    }

    @Test
    fun negativeOutroAndIntroSetTest() {
        song.outroDuration = introTime
        song.introDuration = outroTime + introTime + 1

        assertEquals(introTime, song.outroDuration)
    }

    // Local volume change tests
    @Test
    fun positiveSongLocalVolumeChangeTest(){
        song.localVolume = localVolume

        assertEquals(localVolume, song.localVolume)
    }

    @Test
    fun belowZeroSongLocalVolumeChangeTest(){
        song.localVolume = -localVolume

        assertEquals(0, song.localVolume)
    }

    @Test
    fun outOfRangeSongLocalVolumeChangeTest(){
        song.localVolume = 100 + localVolume

        assertEquals(100, song.localVolume)
    }

}