package playlistMenu.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.akap.R
import playlistMenu.holders.SongHolder
import playlistMenu.interfaces.IPlaylist
import playlistMenu.interfaces.ISong
import playlistMenu.managers.GlobalManager

class SongAdapter(
    private var playlist:    IPlaylist,
    private val onSongClick: (ISong) -> Unit
                 ) : RecyclerView.Adapter<SongHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_song, parent, false)
        return SongHolder(view)
    }

    override fun onBindViewHolder(holder: SongHolder, position: Int) {
        val song = playlist.songs[position]

        holder.titleText.text  = song.title
        holder.artistText.text = song.artist

        val selectedColor = ContextCompat.getColor(holder.itemView.context, R.color.colorSongSelected)
        val defaultColor  = ContextCompat.getColor(holder.itemView.context, R.color.colorSongDefault)

        val isSelected = (position == playlist.getIndex() &&
                          GlobalManager.getPlaylistName() == playlist.name)

        holder.itemView.setBackgroundColor(if (isSelected) selectedColor else defaultColor)

        holder.itemView.setOnClickListener {
            onSongClick(song)
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
}
