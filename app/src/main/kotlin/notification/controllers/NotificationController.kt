package notification.controllers

import android.app.Notification
import android.content.Context
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.example.akap.R
import playlistMenu.managers.SongManager

class NotificationController(private val context: Context) {

    private fun updatePlayPauseButton() : Int {
        val iconResId = if (SongManager.isPaused) {
            android.R.drawable.ic_media_pause
        } else {
            android.R.drawable.ic_media_play
        }
        return iconResId
    }

    fun buildNotification(
        channelId: String,
        smallContentView: RemoteViews,
        bigContentView: RemoteViews
    ): Notification {
        return NotificationCompat.Builder(context, channelId)
            .setSmallIcon(updatePlayPauseButton())
            .setCustomContentView(smallContentView)
            .setCustomBigContentView(bigContentView)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .build()
    }

    fun createRemoteViews(layoutId: Int): RemoteViews {
        return RemoteViews(context.packageName, layoutId)
    }

    fun setRemoteViewsText(
        remoteViews:    RemoteViews,
        titleResId:     Int,
        artistResId:    Int,
        titleText:      String,
        artistText:     String
    ) {
        remoteViews.setTextViewText(titleResId, titleText)
        remoteViews.setTextViewText(artistResId, artistText)
    }
}
