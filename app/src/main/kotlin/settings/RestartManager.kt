package settings

import akap.MainActivity
import android.app.Activity
import android.content.Context
import android.content.Intent
import broadcast.BroadcastManagerController
import kotlinx.coroutines.channels.BroadcastChannel

object RestartManager {
    fun restartApplication(context: Context) {
        BroadcastManagerController(context).sendBroadcast("RESTART_APP")
    }

    fun restartActivity(context: Context) {
        BroadcastManagerController(context).sendBroadcast("RESTART_ACTIVITY")
    }
}