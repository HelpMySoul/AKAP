package notification.services

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
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

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()
        Log.d("MediaSessionService", "Service created")

        notificationController  = NotificationController(applicationContext)
        intentController        = IntentController(applicationContext)

        startForegroundService()

        val player   = Media3Player(applicationContext)
        mediaSession = MediaSession.Builder(this, player)
            .setId("MediaSession")
            .build()
    }

    @SuppressLint("ForegroundServiceType")
    private fun startForegroundService() {
        val channelId = "media_session_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Media Session",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }

        val notification = buildNotification(channelId)
        startForeground(1, notification)
    }

    private fun buildNotification(channelId: String): Notification {
        val remoteViewsSmall = notificationController.createRemoteViews(R.layout.notification_small)
        val remoteViewsBig = notificationController.createRemoteViews(R.layout.notification_big)

        notificationController.setRemoteViewsText(
            remoteViewsSmall,
            R.id.title,
            R.id.artist,
            applicationContext.getString(R.string.now_playing_title),
            applicationContext.getString(R.string.now_playing_artist)
        )
        notificationController.setRemoteViewsText(
            remoteViewsBig,
            R.id.title,
            R.id.artist,
            applicationContext.getString(R.string.now_playing_title),
            applicationContext.getString(R.string.now_playing_artist)
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
        stopForeground(STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession {
        return mediaSession!!
    }
}
