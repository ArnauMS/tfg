package com.aplication.aplicaciotfg

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView

open class NavegadorActivity : AppCompatActivity() {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.navegator_layout)

        // Obtenir els diferents elements del layout
        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)

        // Administra l'obertura i tancament del menu
        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open_drawer, R.string.close_drawer)

        // Per poder respondre als events d'obertura i tancament
        drawerLayout.addDrawerListener(toggle)

        // Per sincronitzar ActionBar i el DrawerLayout
        toggle.syncState()

        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.inicio -> {
                    // Canviar de pantalla a la pagina principal
                    val intent = Intent(this, PaginaPrincipalActivity::class.java)
                    intent.putExtra("titol", "Training Control")
                    startActivity(intent)
                    true
                }
                R.id.perfil -> {
                    // Canviar de pantalla a la d'editar perfil
                    val intent = Intent(this, EditarPerfilActivity::class.java)
                    intent.putExtra("titol", "Editar Perfil")
                    startActivity(intent)
                    true
                }
                R.id.historial -> {
                    // Canviar de pantalla a la del historial
                    val intent = Intent(this, HistorialActivity::class.java)
                    intent.putExtra("titol", "Historial")
                    startActivity(intent)
                    true
                }
                R.id.calcular_ent -> {
                    // Canviar de pantalla a la de precalcular entrenament
                    val intent = Intent(this, PlanificadorActivity::class.java)
                    intent.putExtra("titol", "Planificador")
                    startActivity(intent)
                    true
                }
                R.id.control_ent -> {
                    // Canviar de pantalla a la de control entrenament
                    val intent = Intent(this, ControlEntrenamentActivity::class.java)
                    intent.putExtra("titol", "Control")
                    startActivity(intent)
                    true
                }
                R.id.introducir_datos -> {
                    // Canviar de pantalla a la d'introduir dades
                    val intent = Intent(this, IntroduirDadesActivity::class.java)
                    intent.putExtra("titol", "Introducir Datos")
                    startActivity(intent)
                    true
                }
                R.id.ajustes -> {
                    // Canviar de pantalla a la de configuracio
                    val intent = Intent(this, AjustesActivity::class.java)
                    intent.putExtra("titol", "Ajustes")
                    startActivity(intent)
                    true
                }
                R.id.tancar_sessio -> {
                    // Funcio que obre el dialeg de tancar sessio
                    popUpLogout()
                    true
                }
                else -> false
            }
        }
    }

    // Funcio per mostrar un dialeg per tancar la sessio
    private fun popUpLogout() {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("Cerrar Sesión")
        alertDialogBuilder.setMessage("¿Estás seguro de que deseas cerrar sesión?")
        alertDialogBuilder.setPositiveButton("Sí") { _, _ ->
            // Tancar sessio i tornar a la pantalla de login
            Usuari.logout()
            Toast.makeText(this, "Has cerrado sesión con exito!", Toast.LENGTH_LONG).show();
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
        alertDialogBuilder.setNegativeButton("No") { dialog, _ ->
            // Tancar dialeg
            dialog.dismiss()
        }
        val alertDialog: AlertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }
}