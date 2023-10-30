package com.aplication.aplicaciotfg

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val title = intent?.getStringExtra("title")
        val content = intent?.getStringExtra("content")
        val channelId = intent?.getStringExtra("channelId")

        if (context != null && title != null && content != null && channelId != null) {
            showNotification(context, title, content, channelId)
        }
    }

    private fun showNotification(context: Context, title: String, content: String, channelId: String) {
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.avatar)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(context)) {
            notify(1, builder.build())
        }
    }
}
