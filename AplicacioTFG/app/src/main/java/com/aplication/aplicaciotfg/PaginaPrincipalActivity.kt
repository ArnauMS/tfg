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
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var btnMenu: AppCompatButton
    private lateinit var titol: TextView
    private val idCanal = "36543941516516549665214684168415056985552852841"
    private lateinit var nom: TextView
    private lateinit var botoAdd: Button
    private lateinit var fotoPerfil: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        layoutInflater.inflate(R.layout.activity_pagina_principal, findViewById(R.id.container))

        // Obtenir els diferents elements del layout
        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        btnMenu = findViewById(R.id.btnMenu)
        titol = findViewById(R.id.titol)
        nom = findViewById(R.id.nom)
        botoAdd = findViewById(R.id.botoAdd)
        fotoPerfil = findViewById(R.id.fotoPerfil)

        // Carregar nom i foto de perfil del usuari
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

        // Crida a crearCanalNotificacio per crear el canal de notificacions
        NotificationUtils.crearCanalNotificacio(this, idCanal)
        NotificationScheduler.programarNotificacio(this, Usuari.hora, Usuari.minut, Usuari.titol, Usuari.contingut, idCanal)
    }
}