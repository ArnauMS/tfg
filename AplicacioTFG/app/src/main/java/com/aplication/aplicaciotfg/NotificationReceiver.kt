package com.aplication.aplicaciotfg

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import java.text.SimpleDateFormat
import java.util.*

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
        val intent = Intent(context, LoginActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        val requestCodi = codiUnic()
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            requestCodi,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val builder = NotificationCompat.Builder(context, idCanal)
            .setContentTitle(titol)
            .setContentText(contingut)
            .setSmallIcon(R.drawable.logo_rodo)
            .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            notify(requestCodi, builder.build())
        }
    }

    private fun codiUnic(): Int {
        val format = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val dataActual = Date()
        val dataActualFormatejada = format.format(dataActual)
        return dataActualFormatejada.toInt()
    }
}
