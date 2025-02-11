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
            "ACTION_PREV" -> broadcastManager.sendBroadcast("ACTION_PREV")
            "ACTION_PLAY_PAUSE" -> broadcastManager.sendBroadcast("ACTION_PLAY")
            "ACTION_NEXT" -> broadcastManager.sendBroadcast("NEXT_SONG")
        }
    }
}
