package screens.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import builders.SetNameBuilder

import com.example.akap.R
import player.adapters.PlaylistAdapter
import player.controllers.PlaylistController
import player.interfaces.IPlaylist
import global.GlobalManager
import player.interfaces.ISong
import screens.AppFragmentManager

class Playlists : Fragment() {

    private lateinit var playlistController:    PlaylistController
    private lateinit var playlistAdapter:       PlaylistAdapter
    private lateinit var createPlaylistButton:  Button
    private lateinit var deletePlaylistsButton: ImageButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        playlistController = PlaylistController(requireContext())

        val view = inflater.inflate(R.layout.fragment_playlists, container, false)

        val playlists = playlistController.getAllPlaylists()

        playlistAdapter = PlaylistAdapter(
            playlists,
            onPlaylistClick     = { playlist -> openCurrentPlaylist(playlist) },
            onPlaylistLongClick = { true }
        )

        createPlaylistButton  = view.findViewById(R.id.createPlaylistButton)
        deletePlaylistsButton = view.findViewById(R.id.deletePlaylistsButton)

        deletePlaylistsButton.setOnClickListener {
            if (playlistAdapter.getSelectedPlaylists().isEmpty()) {
                Toast.makeText(context, context?.getString(R.string.no_playlists_to_delete), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            playlistAdapter.getSelectedPlaylists().forEach { playlist ->
                if (playlist == playlistController.getPlaylist(GlobalManager.getDisplayedPlaylistName())){
                    Toast.makeText(context, context?.getString(R.string.delete_current_playlist), Toast.LENGTH_SHORT).show()
                }
                else {
                    playlistController.deletePlaylist(playlist.name)
                }
            }
            playlistAdapter.clearSelection()
            refresh()
        }

        createPlaylistButton.setOnClickListener {
            SetNameBuilder(
                context     = requireContext(),
                currentName = requireContext().getString(R.string.new_playlist),
                onChange    = { newName ->
                    val songs: List<ISong> = listOf()
                    playlistController.createPlaylist(newName, songs)
                    refresh()
                }
            ).built()
        }
        return view
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun refresh() {
        playlistAdapter.notifyDataSetChanged()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewPlaylists)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter       = playlistAdapter

        createPlaylistButton = view.findViewById(R.id.createPlaylistButton)

    }

    private fun openCurrentPlaylist(playlist: IPlaylist) {
        GlobalManager.updateDisplayedPlaylistName(playlist.name)
        val playlistName = GlobalManager.getDisplayedPlaylistName()

        val currentPlaylistFragment = CurrentPlaylist().apply {
            arguments = Bundle().apply {
                putString("playlist_name", playlistName)
            }
        }

        parentFragmentManager.let {
            AppFragmentManager.openFragment(
                it,
                R.id.songContainerFragment,
                playlistName
            ) { currentPlaylistFragment }
        }
    }

}
