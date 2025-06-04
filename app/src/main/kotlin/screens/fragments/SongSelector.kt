package screens.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.akap.R
import player.adapters.SongChooseAdapter
import player.controllers.PlaylistController
import player.controllers.SongSearchController
import player.interfaces.IPlaylist
import player.interfaces.ISong
import player.managers.PlaylistManager

class SongSelector (
    private var playlistController: PlaylistController
): Fragment() {

    private lateinit var songsSelectorRecyclerView: RecyclerView
    private lateinit var songAdapter:               SongChooseAdapter
    private lateinit var addSongsButton:            Button
    private lateinit var searchSongSelectorText:    EditText
    private lateinit var songSearchController:      SongSearchController

    private var playlist:       IPlaylist? = PlaylistManager.getAllSongsPlaylist()
    private var resultListener: OnFragmentResultListener? = null

    interface OnFragmentResultListener {
        fun onResult(data: MutableList<ISong>?)
    }

    fun setOnFragmentResultListener(listener: OnFragmentResultListener) {
        this.resultListener = listener
    }

    override fun onCreateView(
        inflater:           LayoutInflater,
        container:          ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view                  = inflater.inflate(R.layout.fragment_song_selector, container, false)
        songsSelectorRecyclerView = view.findViewById(R.id.songsSelectorRecyclerView)
        addSongsButton            = view.findViewById(R.id.addSongButton)
        searchSongSelectorText    = view.findViewById(R.id.searchSongSelectorText)
        songSearchController      = SongSearchController(requireContext(), playlistController)
        songAdapter               = SongChooseAdapter(playlist!!)

        songsSelectorRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        songsSelectorRecyclerView.adapter = songAdapter

        searchSongSelectorText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                if (playlist == null) {
                    return
                }

                val query = s?.toString() ?: ""
                playlist  = songSearchController.search(query, playlist)!!

                songAdapter.updatePlaylist(playlist)

                songAdapter = SongChooseAdapter(playlist!!)

                songsSelectorRecyclerView.adapter = songAdapter

                songAdapter.refresh()
            }

            override fun afterTextChanged(s: Editable?) { }
        })

        addSongsButton.setOnClickListener {

            resultListener?.onResult(songAdapter.getSelected())

            parentFragmentManager.popBackStack()
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        songAdapter.refresh()
    }

    companion object {
        fun instance(playlistController: PlaylistController) = SongSelector(playlistController)
    }
}