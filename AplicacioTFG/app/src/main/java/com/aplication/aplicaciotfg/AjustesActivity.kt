package com.aplication.aplicaciotfg

import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import java.text.SimpleDateFormat
import java.util.*

class AjustesActivity : NavegadorActivity() {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var btnMenu: AppCompatButton
    private lateinit var titol: TextView
    private lateinit var horaSeleccionada: Calendar
    private lateinit var hora: EditText
    private lateinit var guardar: AppCompatButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        layoutInflater.inflate(R.layout.activity_ajustes, findViewById(R.id.container))

        // Obtenir els diferents elements del layout
        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        btnMenu = findViewById(R.id.btnMenu)
        titol = findViewById(R.id.titol)
        hora = findViewById(R.id.hora)
        guardar = findViewById(R.id.guardar)
        horaSeleccionada = Calendar.getInstance()

        // Titol de la pantalla que rep per parametre
        val titolExtra: String = intent.extras?.getString("titol").orEmpty()
        titol.text = titolExtra

        // Mostrar seleccionador de la hora
        hora.setOnClickListener {
            mostrarSeleccionador()
        }

        // Guardar nova hora d'avis
        guardar.setOnClickListener{
            val horaS = hora.text.toString()
            if (horaS.isNotEmpty()) {
                val novaHora = horaS.split(":")
                Usuari.hora = novaHora[0].toInt()
                Usuari.minut = novaHora[1].toInt()
                BaseDades.emmagatzemarHora(Usuari.email, novaHora[0].toInt(), novaHora[1].toInt())

                // Canviar de pantalla a la pagina principal
                val intent = Intent(this, PaginaPrincipalActivity::class.java)
                intent.putExtra("titol", "Training Control")
                startActivity(intent)
            } else {
                Toast.makeText(this, "Selecciona una nueva hora", Toast.LENGTH_LONG).show()
            }
        }

        btnMenu.setOnClickListener {
            // Obrir tancar el menu lateral
            if (drawerLayout.isDrawerOpen(navView)) {
                drawerLayout.closeDrawer(navView)
            } else {
                drawerLayout.openDrawer(navView)
            }
        }
    }

    // Funcio per mostrar el seleccionador de la nova hora d'avis
    fun mostrarSeleccionador() {
        val timePickerDialog = TimePickerDialog(
            this,
            { _: TimePicker, hourOfDay: Int, minute: Int ->
                horaSeleccionada.set(Calendar.HOUR_OF_DAY, hourOfDay)
                horaSeleccionada.set(Calendar.MINUTE, minute)
                val format = SimpleDateFormat("HH:mm", Locale.getDefault())
                val horaFormatejada = format.format(horaSeleccionada.time)
                hora.setText(horaFormatejada)
            },
            horaSeleccionada.get(Calendar.HOUR_OF_DAY),
            horaSeleccionada.get(Calendar.MINUTE),
            true
        )

        timePickerDialog.show()
    }
}