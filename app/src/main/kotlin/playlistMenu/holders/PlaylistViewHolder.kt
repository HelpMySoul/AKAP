package playlistMenu.holders

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.akap.R

class PlaylistViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val playlistName:   TextView = view.findViewById(R.id.playlist_name)
    val songCount:      TextView = view.findViewById(R.id.song_count)
}