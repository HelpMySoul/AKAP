package screens.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import builders.SetNameBuilder
import com.example.akap.R
import broadcast.BroadcastManagerController
import builders.SelectionBuilder
import player.controllers.PlaylistController
import player.controllers.SongSearchController
import player.interfaces.IPlaylist
import player.interfaces.ISong
import global.GlobalManager
import player.adapters.SongPlayerAdapter
import player.managers.FrameLayoutManager
import player.managers.PlaylistManager
import player.managers.SongManager
import screens.AppFragmentManager
import settings.player.PlayerSettingsManager

class CurrentPlaylist (
    private var onSongClick: ((ISong) -> Unit)? = null
) : Fragment() {

    private lateinit var songsRecyclerView:      RecyclerView
    private lateinit var songPlayerAdapter:      SongPlayerAdapter
    private lateinit var playlistController:     PlaylistController
    private lateinit var songSearchController:   SongSearchController

    private lateinit var playlistNameText:       TextView

    private var playlist:               IPlaylist?
        get() {
        return playlistController.getPlaylist(GlobalManager.getDisplayedPlaylistName())
    }
        set(value) {
            GlobalManager.updateDisplayedPlaylistName(value!!.name)
    }

    private lateinit var playlistLayout:         FrameLayout
    private lateinit var editLayout:             LinearLayout
    private lateinit var searchInPlaylistButton: ImageButton
    private lateinit var shufflePlaylistButton:  ImageButton
    private lateinit var settingsPlaylistButton: ImageButton

    private lateinit var searchLayout:           FrameLayout
    private lateinit var searchText:             EditText
    private lateinit var songFilterButton:       ImageButton
    private lateinit var closeSearchTextButton:  ImageButton

    private lateinit var settingsLayout:          FrameLayout
    private lateinit var editPlaylistNameButton:  ImageButton
    private lateinit var addSongsButton:          ImageButton
    private lateinit var deleteSongsButton:       ImageButton
    private lateinit var closeSettingsTextButton: ImageButton

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_current_playlist, container, false)

        songsRecyclerView       = view.findViewById(R.id.songsRecyclerView)
        playlistNameText        = view.findViewById(R.id.playlistNameTextView)
        searchText              = view.findViewById(R.id.searchText)
        searchInPlaylistButton  = view.findViewById(R.id.searchInPlaylistButton)
        deleteSongsButton       = view.findViewById(R.id.deleteSongsButton)
        editPlaylistNameButton  = view.findViewById(R.id.editPlaylistNameButton)
        addSongsButton          = view.findViewById(R.id.addSongsButton)
        closeSearchTextButton   = view.findViewById(R.id.closeSearchTextButton)
        editLayout              = view.findViewById(R.id.editLayout)
        playlistLayout          = view.findViewById(R.id.playlistLayout)
        searchLayout            = view.findViewById(R.id.searchLayout)
        shufflePlaylistButton   = view.findViewById(R.id.shufflePlaylistButton)
        settingsPlaylistButton  = view.findViewById(R.id.settingsPlaylistButton)
        settingsLayout          = view.findViewById(R.id.settingsLayout)
        closeSettingsTextButton = view.findViewById(R.id.closeSettingsTextButton)
        songFilterButton        = view.findViewById(R.id.songFilterButton)

        songsRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        FrameLayoutManager.addLayouts(settingsLayout, searchLayout, playlistLayout)

        playlistController   = PlaylistController(requireContext())
        songSearchController = SongSearchController(requireContext(), playlistController)

        if (playlist == null) {
            playlistNameText.text = context?.getString(R.string.no_playlist) ?: ""

            editLayout.visibility = View.GONE

            return view
        }
        else {
            editLayout.visibility = View.VISIBLE
        }

        playlistNameText.text = playlist!!.name

        if (onSongClick == null) {
            onSongClick = { song -> playSong(song) }
        }

        songPlayerAdapter = SongPlayerAdapter(
            playlist!!,
            onSongClick     = { song -> onSongClick?.invoke(song) },
            onLongSongClick = { song -> showSongSettingCard(song) }
        )

        songsRecyclerView.adapter = songPlayerAdapter

        playlist!!.getSongAt(playlist!!.getIndex()).let { song ->
            if (song != null) {
                songPlayerAdapter.setCurrentSong(song)
            }
        }

        Log.d("CurrentPlaylist", "${GlobalManager.getSongID()} ${GlobalManager.getSongID() != (-1).toLong()} ${(-1).toLong()} $context")

        searchText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (playlist == null) {
                    return
                }

                val query = s?.toString() ?: ""
                playlist  = songSearchController.search(query, playlist)
                updatePlaylist()
            }

            override fun afterTextChanged(s: Editable?) { }
        })

        setButtonListeners()

        if (GlobalManager.getDisplayedPlaylistName() == (context?.getString(R.string.all_songs)
                ?: "All songs")
        ) {
            settingsPlaylistButton.visibility = View.GONE
        }
        else {
            settingsPlaylistButton.visibility = View.VISIBLE
        }

        return view
    }

    private fun showSongSettingCard(song: ISong) {
        AppFragmentManager.addMenuFragment(parentFragmentManager, SongSettingsCard(song) { refresh() }, this)
    }

    @SuppressLint("NotifyDataSetChanged", "ResourceType")
    private fun setButtonListeners() {
        deleteSongsButton.setOnClickListener {
            showSongSelectorFragment(
                playlist!!,
                playlist!!,
                playlistController,
                requireContext().getString(R.string.delete_songs_button_text)) { playlist, selectedSongs ->
                playlistController.deleteSongsFromPlaylist(playlist.name, selectedSongs)
            }
        }

        editPlaylistNameButton.setOnClickListener {
            SetNameBuilder(
                context     = requireContext(),
                currentName = playlistNameText.text.toString(),
                onChange    = { newName ->
                    playlistNameText.text = newName

                    playlist?.let { playlist ->
                        playlistController.updatePlaylistName(playlist.name, newName)
                    }

                    songPlayerAdapter.notifyDataSetChanged()

                    GlobalManager.updateDisplayedPlaylistName(newName)
                }
            ).built()
        }

        addSongsButton.setOnClickListener {
            showSongSelectorFragment(
                PlaylistManager.getAllSongsPlaylist(),
                playlist!!,
                playlistController,
                requireContext().getString(R.string.add_songs_button_text)) { playlist, selectedSongs ->
                playlistController.addSongsToPlaylist(playlist.name, selectedSongs)
            }
        }

        shufflePlaylistButton.setOnClickListener {

            if (playlist == null) {
                Toast.makeText(requireContext(), requireContext().getString(R.string.no_playlist), Toast.LENGTH_SHORT).show()
            }
            else {
                if (GlobalManager.getPlayedPlaylistName() == "") {
                    GlobalManager.updatePlayedPlaylistName(GlobalManager.getDisplayedPlaylistName())
                }
                BroadcastManagerController(requireContext()).sendBroadcast("SHUFFLE_PLAYLIST")
            }
        }

        searchInPlaylistButton.setOnClickListener {
            FrameLayoutManager.openLayout(searchLayout)
        }

        songFilterButton.setOnClickListener {
            val vars: List<String>? = context?.resources?.getStringArray(R.array.filter_rules)?.toList()

            context?.let { context ->
                if (vars != null) {
                    SelectionBuilder(
                        context,
                        context.getString(R.string.filter_rule),
                        vars
                    ) { rule ->
                        songPlayerAdapter.sort(vars.indexOf(rule))
                    }.built()
                }
            }
        }

        closeSearchTextButton.setOnClickListener {
            FrameLayoutManager.openLayout(playlistLayout)
            BroadcastManagerController(requireContext()).sendBroadcast("REFRESH_PLAYLIST")
        }

        settingsPlaylistButton.setOnClickListener {
            FrameLayoutManager.openLayout(settingsLayout)
        }

        closeSettingsTextButton.setOnClickListener {
            FrameLayoutManager.openLayout(playlistLayout)
        }
    }

    private fun showSongSelectorFragment(
        selectorPlaylist:   IPlaylist,
        playlistToEdit:     IPlaylist,
        playlistController: PlaylistController,
        buttonText:         String,
        action:             (IPlaylist, MutableList<ISong>) -> Unit) {
            val newFragment = SongSelector.instance(selectorPlaylist, playlistController, buttonText)
            newFragment.setOnFragmentResultListener(object : SongSelector.OnFragmentResultListener {
                override fun onResult(data: MutableList<ISong>?) {
                    data?.let { selectedSongs ->
                        action(playlistToEdit, selectedSongs)
                    }
                    refresh()
                }
            })

            AppFragmentManager.addMenuFragment(parentFragmentManager, newFragment, this)
    }

    private fun updatePlaylist() {
        songPlayerAdapter.updatePlaylist(playlist)

        songsRecyclerView.adapter = songPlayerAdapter

        BroadcastManagerController(requireContext()).sendBroadcast("REFRESH_PLAYLIST")
    }

    private fun playSong(song: ISong) {
        playlist?.findSong(song)?.let {
            GlobalManager.updatePlayedPlaylistName(playlist!!.name)
            GlobalManager.updateSongID(song.id, requireContext())
            PlayerSettingsManager.saveSettings(requireContext())

            playlist!!.findSong(song)

            songPlayerAdapter.refresh()

            context?.let { context ->
                BroadcastManagerController(context).sendBroadcast("UPDATE_SONG")
                BroadcastManagerController(context).sendBroadcast("PLAY_SONG")
            }
        }
    }

    fun playSong() {
        playlist?.getCurrentSong()?.let { playSong(it) }
    }

    fun playNextSong() {
        songPlayerAdapter.refresh()
    }

    fun prevSong() {
        songPlayerAdapter.refresh()
    }

    fun repeatSong() {
        songPlayerAdapter.refresh()
    }

    fun refresh() {
        playlistNameText.text = playlist?.name ?: context?.getString(R.string.no_playlist) ?: ""

        if (onSongClick == null) {
            onSongClick = { song -> playSong(song) }
        }

        if (playlist == null) {
            songsRecyclerView.adapter = null
            return
        }

        songPlayerAdapter = SongPlayerAdapter(
            playlist!!,
            onSongClick     = { song -> onSongClick?.invoke(song) },
            onLongSongClick = { song -> showSongSettingCard(song) }
        )

        songsRecyclerView.adapter = songPlayerAdapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        onSongClick = null
    }

    fun shufflePlaylist() {

        if (SongManager.canPlay) {
            BroadcastManagerController(requireContext()).sendBroadcast("STOP_SONG")

            playlist?.shuffle()

            BroadcastManagerController(requireContext()).sendBroadcast("UPDATE_SONG")
        }
        else {
            playlist?.shuffle()
        }


    }

    fun showCurrentSong() {
        playlist?.let { songsRecyclerView.scrollToPosition(it.getIndex()) }
    }
}
