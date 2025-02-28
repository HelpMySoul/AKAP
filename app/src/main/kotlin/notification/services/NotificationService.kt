package notification.services

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.akap.R
import notification.controllers.IntentController
import notification.controllers.NotificationController

class NotificationService : Service() {

    private lateinit var notificationController: NotificationController
    private lateinit var intentController:       IntentController

    private val notificationId = 1

    override fun onCreate() {
        super.onCreate()
        Log.d("NotificationService", "Service created")

        notificationController = NotificationController(applicationContext)
        intentController       = IntentController(applicationContext)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    @SuppressLint("ForegroundServiceType")
    fun showNotification(title: String, artist: String) {
        try {
            val channelId = "NotificationService"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId,
                    "Notification Service",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Music playback controls"
                }

                val notificationManager = getSystemService(NotificationManager::class.java)
                notificationManager.createNotificationChannel(channel)
            }

            val notification = buildNotification(channelId, title, artist)

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.notify(notificationId, notification)

            startForeground(notificationId, notification)
        } catch (e: Exception) {
            Log.e("NotificationServiceErrors", "Error showing notification", e)
        }
    }


    private fun buildNotification(channelId: String, title: String, artist: String): Notification {
        val remoteViewsSmall = notificationController.createRemoteViews(R.layout.notification_small)
        val remoteViewsBig   = notificationController.createRemoteViews(R.layout.notification_big)

        notificationController.setRemoteViewsText(
            remoteViewsSmall,
            R.id.title,
            R.id.artist,
            title,
            artist
        )
        notificationController.setRemoteViewsText(
            remoteViewsBig,
            R.id.title,
            R.id.artist,
            title,
            artist
        )

        remoteViewsBig.setOnClickPendingIntent(
            R.id.btn_prev,
            intentController.createPendingIntent("ACTION_PREV", 0)
        )
        remoteViewsBig.setOnClickPendingIntent(
            R.id.btn_play_pause,
            intentController.createPendingIntent("ACTION_PLAY_PAUSE", 1)
        )
        remoteViewsBig.setOnClickPendingIntent(
            R.id.btn_next,
            intentController.createPendingIntent("ACTION_NEXT", 2)
        )

        var notificationController = NotificationController(applicationContext)

        return notificationController.buildNotification(channelId, remoteViewsSmall, remoteViewsBig)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val title  = intent?.getStringExtra("title") ?: "Default Title"
        val artist = intent?.getStringExtra("artist") ?: "Default Artist"
        val show   = intent?.getBooleanExtra("show", false)

        if (show == true) {
            showNotification(title, artist)
        }
        else {
            stopForeground(true)
            stopSelf()
            Log.e("NotificationServiceErrors", "Service destroyed")
        }
        return START_NOT_STICKY
    }
}