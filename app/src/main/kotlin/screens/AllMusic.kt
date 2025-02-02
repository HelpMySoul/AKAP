package com.example.akap

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import playlistMenu.songControl.MusicFinder
import playlistMenu.songControl.SongAdapter
import playlistMenu.interfaces.ISong
import playlistMenu.interfaces.SongPlayerListener

class AllMusic : Fragment() {

    private lateinit var songsRecyclerView: RecyclerView
    private lateinit var songAdapter: SongAdapter

    private var songPlayerListener: SongPlayerListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_allmusic, container, false)

        songsRecyclerView = view.findViewById(R.id.songsRecyclerView)
        songsRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        val musicFinder = MusicFinder(requireContext())
        val songs = musicFinder.findMusic()

        songAdapter = SongAdapter(songs) { song ->
            playSong(song)
        }

        songsRecyclerView.adapter = songAdapter

        return view
    }

    private fun playSong(song: ISong) {
        songPlayerListener?.updateSong(song)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is SongPlayerListener) {
            songPlayerListener = context
        } else {
            throw RuntimeException("Activity must implement SongPlayerListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        songPlayerListener = null
    }
}