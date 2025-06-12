package notification.controllers

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import notification.receivers.MediaReceiver

class IntentController(private val context: Context) {

    fun createPendingIntent(action: String, requestCode: Int): PendingIntent {
        val intent = Intent(context, MediaReceiver::class.java).apply {
            this.action = action
        }

        Log.e("IntentController", action)

        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT  or PendingIntent.FLAG_IMMUTABLE
        )
    }
}