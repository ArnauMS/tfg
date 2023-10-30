package com.aplication.aplicaciotfg

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.aplication.aplicaciotfg.R

object NotificationUtils {

    fun crearCanalNotificacio(context: Context, idCanal: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nomCanal = context.getString(R.string.notification_channel_name)
            val importancia = NotificationManager.IMPORTANCE_DEFAULT

            val canal = NotificationChannel(idCanal, nomCanal, importancia)

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(canal)
        }
    }
}
