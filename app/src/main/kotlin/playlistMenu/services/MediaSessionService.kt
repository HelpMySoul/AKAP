package playlistMenu.services

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import playlistMenu.classes.Media3Player
import playlistMenu.controllers.BroadcastManagerController
import playlistMenu.controllers.PlaylistController
import playlistMenu.controllers.SongController
import playlistMenu.managers.GlobalManager
import playlistMenu.managers.SongManager
import playlistMenu.receivers.MediaReceiver
import screens.CurrentPlaylist

class MediaSessionService : MediaSessionService() {
    private var mediaSession: MediaSession? = null

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()
        Log.d("MediaSessionService", "Service created")
        startForegroundService()

        val player = Media3Player(applicationContext)
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
        val context = this

        val prevIntent = Intent(context, MediaReceiver::class.java).apply {
            action = "ACTION_PREV"
        }
        val playIntent = Intent(context, MediaReceiver::class.java).apply {
            action = "ACTION_PLAY"
        }
        val nextIntent = Intent(context, MediaReceiver::class.java).apply {
            action = "ACTION_NEXT"
        }

        val prevPendingIntent = PendingIntent.getBroadcast(
            context, 0, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val playPendingIntent = PendingIntent.getBroadcast(
            context, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val nextPendingIntent = PendingIntent.getBroadcast(
            context, 0, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        var currentSong = PlaylistController(context).getPlaylist(GlobalManager.getPlaylistName())?.getCurrentSong()

        return NotificationCompat.Builder(context, channelId)
            .setContentTitle("Сейчас играет")
            .setContentText("${currentSong?.title}\n${currentSong?.artist}") // Отображение в две строки
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setShowActionsInCompactView(0, 1, 2)
            )
            .setStyle(NotificationCompat.BigTextStyle().bigText("${currentSong?.title}\n${currentSong?.artist}"))
            .addAction(android.R.drawable.ic_media_previous, "Назад", prevPendingIntent)
            .addAction(android.R.drawable.ic_media_play, "Пауза", playPendingIntent)
            .addAction(android.R.drawable.ic_media_next, "Вперёд", nextPendingIntent)
            .build()

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
