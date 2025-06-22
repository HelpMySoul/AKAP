package notification.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import player.managers.SongManager

class DismissReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        SongManager.stop()
    }
}
