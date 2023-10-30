package com.aplication.aplicaciotfg

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView

class PaginaPrincipalActivity : NavegadorActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        layoutInflater.inflate(R.layout.activity_pagina_principal, findViewById(R.id.container))

        val channelId = "36543941516516549665214684168415056985552852841"
        val nom = findViewById<TextView>(R.id.nom)
        val titol = findViewById<TextView>(R.id.titol)
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        val navView = findViewById<NavigationView>(R.id.nav_view)
        val btnMenu = findViewById<AppCompatButton>(R.id.btnMenu)
        val botoAdd = findViewById<Button>(R.id.botoAdd)
        val fotoPerfil = findViewById<ImageView>(R.id.fotoPerfil)

        nom.text = Usuari.nom
        fotoPerfil.setImageURI(Usuari.imatge)

        // Titol de la pantalla que rep per parametre
        val titolExtra: String = intent.extras?.getString("titol").orEmpty()
        titol.text = titolExtra

        btnMenu.setOnClickListener {
            // Obrir tancar el menu lateral
            if (drawerLayout.isDrawerOpen(navView)) {
                drawerLayout.closeDrawer(navView)
            } else {
                drawerLayout.openDrawer(navView)
            }
        }

        botoAdd.setOnClickListener {
            // Canviar de pantalla a la d'introduir dades
            val intent = Intent(this, IntroduirDadesActivity::class.java)
            intent.putExtra("titol", "Introducir Datos")
            startActivity(intent)
        }

        // Llama a createNotificationChannel para crear el canal de notificaciones
        NotificationUtils.createNotificationChannel(this, channelId)

        val hour = 14 // Hora de la notificación (en formato de 24 horas)
        val minute = 19 // Minuto de la notificación
        val title = "Introducir datos"
        val content = "Recuerda introducir tus datos del entrenamiento!"

        NotificationScheduler.scheduleNotification(this, hour, minute, title, content, channelId)
    }
}