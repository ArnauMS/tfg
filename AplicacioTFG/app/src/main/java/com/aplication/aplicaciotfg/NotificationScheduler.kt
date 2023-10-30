package com.aplication.aplicaciotfg

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.*

object NotificationScheduler {
    fun programarNotificacio(context: Context, hora: Int, minut: Int, titol: String, contingut: String, idCanal: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificationReceiver::class.java)
        intent.putExtra("titol", titol)
        intent.putExtra("contingut", contingut)
        intent.putExtra("idCanal", idCanal)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Obtenim la hora i minut actual
        val horaActual = System.currentTimeMillis()
        val calendari = Calendar.getInstance()
        calendari.timeInMillis = horaActual
        calendari.set(Calendar.HOUR_OF_DAY, hora)
        calendari.set(Calendar.MINUTE, minut)
        calendari.set(Calendar.SECOND, 0)

        // Si la hora programada ja ha passat sumem un día per programar-la pel dia seguent
        if (calendari.timeInMillis <= horaActual) {
            calendari.add(Calendar.DAY_OF_YEAR, 1)
        }

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendari.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }
}
