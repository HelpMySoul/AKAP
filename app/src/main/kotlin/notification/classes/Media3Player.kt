package notification.classes

import android.content.Context
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.common.Player
import playlistMenu.controllers.BroadcastManagerController

@UnstableApi
class Media3Player(var context: Context) : Player by ExoPlayer.Builder(context).build() {

    override fun seekToNext() {
        BroadcastManagerController(context).sendBroadcast("NEXT_SONG")
    }
}