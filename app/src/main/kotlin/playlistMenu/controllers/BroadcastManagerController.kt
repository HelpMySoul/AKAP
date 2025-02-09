package playlistMenu.controllers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import playlistMenu.managers.PlayerSettingsManager

class BroadcastManagerController(context: Context) {
    private val localBroadcastManager   = LocalBroadcastManager.getInstance(context)
    private val receivers               = mutableMapOf<String, BroadcastReceiver>()

    fun registerReceiver(action: String, receiver: BroadcastReceiver) {
        receivers[action] = receiver
        localBroadcastManager.registerReceiver(receiver, IntentFilter(action))
    }

    fun unregisterReceiver(action: String) {
        receivers[action]?.let {
            localBroadcastManager.unregisterReceiver(it)
            receivers.remove(action)
        }
    }

    fun sendBroadcast(action: String) {
        localBroadcastManager.sendBroadcast(Intent(action))
    }
}
