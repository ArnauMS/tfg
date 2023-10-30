package com.aplication.aplicaciotfg

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val titol = intent?.getStringExtra("titol")
        val contingut = intent?.getStringExtra("contingut")
        val idCanal = intent?.getStringExtra("idCanal")

        if (context != null && titol != null && contingut != null && idCanal != null) {
            mostrarNotificacio(context, titol, contingut, idCanal)
        }
    }

    // Funcio per mostrar la notificacio
    private fun mostrarNotificacio(context: Context, titol: String, contingut: String, idCanal: String) {
        val builder = NotificationCompat.Builder(context, idCanal)
            .setSmallIcon(R.drawable.avatar)
            .setContentTitle(titol)
            .setContentText(contingut)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(context)) {
            notify(1, builder.build())
        }
    }
}
