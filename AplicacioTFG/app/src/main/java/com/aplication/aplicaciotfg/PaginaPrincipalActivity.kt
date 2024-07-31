package com.aplication.aplicaciotfg

import CalendarAdapter
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.*
import androidx.appcompat.widget.AppCompatButton
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import java.text.SimpleDateFormat
import java.util.*

class PaginaPrincipalActivity : NavegadorActivity() {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var btnMenu: AppCompatButton
    private lateinit var titol: TextView
    private val idCanal = "36543941516516549665214684168415056985552852841"
    private lateinit var nom: TextView
    private lateinit var botoAdd: Button
    private lateinit var fotoPerfil: ImageView
    private lateinit var textMes: TextView
    private lateinit var taula: GridView
    private val codiPermis = 123
    private val codiPermisNotificacions = 456
    private lateinit var fletxaEsquerre: TextView
    private lateinit var fletxaDreta: TextView
    private lateinit var calendarAdapter: CalendarAdapter
    private lateinit var mes: String
    private lateinit var dataActual: Calendar
    private lateinit var dataCalendari: Calendar
    private lateinit var formatMes: SimpleDateFormat

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
        textMes = findViewById(R.id.textMes)
        taula = findViewById(R.id.taula)
        fletxaEsquerre = findViewById(R.id.fletxaEsquerre)
        fletxaDreta = findViewById(R.id.fletxaDreta)

        // Comprovem els permissos i en cas de tenir-los carreguem la imatge
        if (Usuari.imatge != Uri.EMPTY) {
            comprovarPermissos()
        }

        // Carregar nom
        nom.text = Usuari.nom

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

        dataActual = Calendar.getInstance()
        dataCalendari = Calendar.getInstance()
        formatMes = SimpleDateFormat("MMMM yyyy", Locale("es", "ES"))
        mes = formatMes.format(dataCalendari.time).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
        textMes.text = mes

        // Fletxa per veure mesos anteriors
        fletxaEsquerre.setOnClickListener {
            dataCalendari.add(Calendar.MONTH, -1)
            mes = formatMes.format(dataCalendari.time).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
            textMes.text = mes
            calendarAdapter = CalendarAdapter(this, dataCalendari)
            taula.adapter = calendarAdapter
        }

        // Fletxa per veure els mesos fins a l'actual
        fletxaDreta.setOnClickListener {
            if (dataActual.get(Calendar.MONTH) != dataCalendari.get(Calendar.MONTH) || dataActual.get(Calendar.YEAR) != dataCalendari.get(Calendar.YEAR)) {
                dataCalendari.add(Calendar.MONTH, 1)
                mes = formatMes.format(dataCalendari.time).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
                textMes.text = mes
                calendarAdapter = CalendarAdapter(this, dataCalendari)
                taula.adapter = calendarAdapter
            }
        }

        // Crida al calendari
        calendarAdapter = CalendarAdapter(this, dataCalendari)
        taula.adapter = calendarAdapter

        val versionSDK = Build.VERSION.SDK_INT

        // Si sdk del dispositiu >= 33
        if (versionSDK >= 33) {
            // Si no tinc el permis per enviar notificacions el solicito
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), codiPermisNotificacions)
            } else {
                // Crida a crearCanalNotificacio per crear el canal de notificacions
                NotificationUtils.crearCanalNotificacio(this, idCanal)
                NotificationScheduler.programarNotificacio(this, Usuari.hora, Usuari.minut, Usuari.titol, Usuari.contingut, idCanal)
            }
        } else {
            // Si no tinc el permis per enviar notificacions el solicito
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WAKE_LOCK) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WAKE_LOCK), codiPermisNotificacions)
            } else {
                // Crida a crearCanalNotificacio per crear el canal de notificacions
                NotificationUtils.crearCanalNotificacio(this, idCanal)
                NotificationScheduler.programarNotificacio(this, Usuari.hora, Usuari.minut, Usuari.titol, Usuari.contingut, idCanal)
            }
        }
    }

    // Funcio per comprovar el permissos
    private fun comprovarPermissos() {
        val versionSDK = Build.VERSION.SDK_INT

        // Si sdk del dispositiu >= 33
        if (versionSDK >= 33) {
            // Comprovar si tinc permissos
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_MEDIA_IMAGES), codiPermis)
            } else { // Si ja tinc permissos carrego la imatge
                Glide.with(this).load(Usuari.imatge).into(fotoPerfil)
            }
        } else {
            // Comprovar si tinc permissos
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), codiPermis)
            } else { // Si ja tinc permissos carrego la imatge
                Glide.with(this).load(Usuari.imatge).into(fotoPerfil)
            }
        }
    }

    // Funcio per demanar permissos
    override fun onRequestPermissionsResult(codiPeticio: Int, permissos: Array<out String>, resultat: IntArray) {
        super.onRequestPermissionsResult(codiPeticio, permissos, resultat)
        when (codiPeticio) {
            codiPermis -> {
                // Si accepta els permissos de multimedia
                if (resultat.isNotEmpty() && resultat[0] == PackageManager.PERMISSION_GRANTED) {
                    Glide.with(this).load(Usuari.imatge).into(fotoPerfil)
                } else {
                    // Mostrar missatge que s'han d'acceptar els permissos
                    Toast.makeText(this, "Para mostrar la imagen de perfil permite el contenido multimedia de la aplicación desde configuración", Toast.LENGTH_LONG).show()
                }
            }
            codiPermisNotificacions -> {
                // Si accepta els permissos de notificacions
                if (resultat.isNotEmpty() && resultat[0] == PackageManager.PERMISSION_GRANTED) {
                    // Crida a crearCanalNotificacio per crear el canal de notificacions
                    NotificationUtils.crearCanalNotificacio(this, idCanal)
                    NotificationScheduler.programarNotificacio(this, Usuari.hora, Usuari.minut, Usuari.titol, Usuari.contingut, idCanal)
                } else {
                    // Mostrar missatge que s'han d'acceptar els permissos
                    Toast.makeText(this, "Para recibir notificaciones otorga los permisos necesarios", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}