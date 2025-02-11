package notification.controllers

import android.app.Notification
import android.content.Context
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat

class NotificationController(private val context: Context) {

    fun buildNotification(
        channelId: String,
        smallContentView: RemoteViews,
        bigContentView: RemoteViews
    ): Notification {
        return NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_media_play)
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
