package notification.services

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import androidx.annotation.OptIn
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.example.akap.R
import notification.classes.Media3Player
import notification.controllers.IntentController
import notification.controllers.NotificationController


class MediaSessionService : MediaSessionService() {

    private var mediaSession: MediaSession? = null
    private lateinit var notificationController: NotificationController
    private lateinit var intentController: IntentController
    private val notificationId = 1

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()
        Log.d("MediaSessionService", "Service created")

        notificationController = NotificationController(applicationContext)
        intentController = IntentController(applicationContext)

        val player = Media3Player(applicationContext)
        mediaSession = MediaSession.Builder(this, player)
            .setId("MediaSession")
            .build()
    }

    @OptIn(UnstableApi::class)
    fun showNotification(title: String, artist: String) {
        try {
            val channelId = "media_session_channel"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId,
                    "Media Session",
                    NotificationManager.IMPORTANCE_LOW
                )
                val notificationManager = getSystemService(NotificationManager::class.java)
                notificationManager.createNotificationChannel(channel)
            }

            val notification = buildNotification(channelId, title, artist)
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.notify(notificationId, notification)
        } catch (e: Exception) {
            Log.e("MSSErrors", "Error showing notification", e)
        }
    }

    fun hideNotification() {
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.cancel(notificationId)
    }

    private fun buildNotification(channelId: String, title: String, artist: String): Notification {
        val remoteViewsSmall = notificationController.createRemoteViews(R.layout.notification_small)
        val remoteViewsBig = notificationController.createRemoteViews(R.layout.notification_big)

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

        return notificationController.buildNotification(
            channelId,
            remoteViewsSmall,
            remoteViewsBig
        )
    }

    override fun onDestroy() {
        mediaSession?.release()
        hideNotification()
        super.onDestroy()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession {
        return mediaSession!!
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        val title = intent?.getStringExtra("title") ?: "Default Title"
        val artist = intent?.getStringExtra("artist") ?: "Default Artist"
        showNotification(title, artist)
        return START_NOT_STICKY
    }
}
