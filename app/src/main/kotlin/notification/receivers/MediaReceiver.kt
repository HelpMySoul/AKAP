package notification.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import playlistMenu.controllers.BroadcastManagerController

class MediaReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d("MediaReceiver", "Received action: $action")
        val broadcastManager = BroadcastManagerController(context)

        when (action) {
            "ACTION_PREV"       -> broadcastManager.sendBroadcast("PREVIOUS_SONG")
            "ACTION_PLAY_PAUSE" -> broadcastManager.sendBroadcast("PAUSE_OR_PLAY_SONG")
            "ACTION_NEXT"       -> broadcastManager.sendBroadcast("NEXT_SONG")
        }
    }
}
