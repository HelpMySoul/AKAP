package notification.controllers

import akap.MainActivity
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.example.akap.R

class NotificationController(private val context: Context) {

    fun buildNotification(
        channelId: String,
        smallContentView: RemoteViews,
        bigContentView: RemoteViews
    ): Notification {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        }

        val pendingIntent =
            PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT  or PendingIntent.FLAG_IMMUTABLE
            )

        return NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.logo)
            .setCustomContentView(smallContentView)
            .setCustomBigContentView(bigContentView)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setContentIntent(pendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(2)
            .setSound(null)
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
