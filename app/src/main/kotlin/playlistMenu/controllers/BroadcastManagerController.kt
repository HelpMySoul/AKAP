package playlistMenu.controllers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import playlistMenu.managers.PlayerSettingsManager

class BroadcastManagerController(context: Context) {
    private val localBroadcastManager = LocalBroadcastManager.getInstance(context)
    private val receivers             = mutableMapOf<String, () -> Unit>()

    fun unregisterAll() {
        receivers.keys.forEach { _ ->
            localBroadcastManager.unregisterReceiver(object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {}
            })
        }
        receivers.clear()
    }

    fun sendBroadcast(action: String) {
        Log.d("BroadcastManager", "Sending broadcast: $action")
        localBroadcastManager.sendBroadcast(Intent(action))
    }

    fun registerReceivers(vararg actions: Pair<String, () -> Unit>) {
        actions.forEach { (action, handler) ->
            val receiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    Log.d("BroadcastManager", "Received broadcast: $action")
                    handler.invoke()
                }
            }
            receivers[action] = handler
            localBroadcastManager.registerReceiver(receiver, IntentFilter(action))
        }
    }
}
