package com.aplication.aplicaciotfg

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context

object NotificationUtils {

    fun crearCanalNotificacio(context: Context, idCanal: String) {
        val nomCanal = context.getString(R.string.notification_channel_name)
        val importancia = NotificationManager.IMPORTANCE_HIGH

        val canal = NotificationChannel(idCanal, nomCanal, importancia)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(canal)
    }
}
