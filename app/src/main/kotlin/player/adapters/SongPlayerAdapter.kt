package player.adapters

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.akap.R
import player.holders.SongHolder
import player.interfaces.IPlaylist
import player.interfaces.ISong
import global.GlobalManager

class SongPlayerAdapter(
    private var playlist:        IPlaylist,
    private val onSongClick:     (ISong) -> Unit,
    private val onLongSongClick: (ISong) -> Unit
                 ) : RecyclerView.Adapter<SongHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_song, parent, false)
        return SongHolder(view)
    }

    override fun onBindViewHolder(holder: SongHolder, position: Int) {
        val song = playlist.songs[position]

        holder.titleText.text  = song.title
        holder.artistText.text = song.artist

        selectionCheck(holder, position)

        holder.itemView.setOnClickListener {
            onSongClick(song)
        }

        holder.itemView.setOnLongClickListener {
            onLongSongClick(song)
            true
        }
    }

    override fun getItemCount(): Int {
        return playlist.songs.size
    }

    fun updatePlaylist(playlist: IPlaylist?) {
        if (playlist != null) {
            this.playlist = playlist
        }
        refresh()
    }

    fun setCurrentSong(song: ISong) {
        playlist.findSong(song)
        refresh()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun refresh() {
        notifyDataSetChanged()
    }

    private fun selectionCheck(holder: SongHolder, position: Int) {
        val selectedColor = ContextCompat.getColor(holder.itemView.context, R.color.colorSongSelected)
        val defaultColor  = ContextCompat.getColor(holder.itemView.context, R.color.colorSongDefault)

        val isSelected = (position == playlist.getIndex() &&
                GlobalManager.getPlayedPlaylistName() == playlist.name)

        holder.itemView.setBackgroundColor(if (isSelected) selectedColor else defaultColor)
    }

    fun sort(ruleId: Int) {
        Log.d("Filter", ruleId.toString())

        when (ruleId) {
            0 -> playlist.songs.sortByDescending { song -> song.dateAdded }
            1 -> playlist.songs.sortBy           { song -> song.dateAdded }
            2 -> playlist.songs.sortBy           { song -> song.title }
            3 -> playlist.songs.sortByDescending { song -> song.title }
            4 -> playlist.songs.sortBy           { song -> song.artist }
            5 -> playlist.songs.sortByDescending { song -> song.artist }
            6 -> playlist.songs.sortBy           { song -> song.duration }
            7 -> playlist.songs.sortByDescending { song -> song.duration}
        }

        refresh()
    }
}
