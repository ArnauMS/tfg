package com.aplication.aplicaciotfg

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ControlEntrenamentActivity : NavegadorActivity() {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var btnMenu: AppCompatButton
    private lateinit var titol: TextView
    private lateinit var info: TextView
    private lateinit var info2: TextView
    private lateinit var ratio: TextView
    private lateinit var senseEntrenos: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        layoutInflater.inflate(R.layout.activity_control_entrenament, findViewById(R.id.container))

        // Obtenir els diferents elements del layout
        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        btnMenu = findViewById(R.id.btnMenu)
        titol = findViewById(R.id.titol)
        info = findViewById(R.id.info)
        info2 = findViewById(R.id.info2)
        ratio = findViewById(R.id.ratio)
        senseEntrenos = findViewById(R.id.senseEntrenos)

        val ratioCalculat = calcularRatio()
        ratio.text = "Actualmente tu ratio es de " + String.format("%.3f", ratioCalculat)

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
    }

    // Funcio per calcular el ratio utilitzant els km
    private fun calcularRatio(): Double {
        val quatreSetmanes = 2.0 / 29
        val unaSetmana = 2.0 / 8
        var ratioCalculat = 0.0
        val format = DateTimeFormatter.ofPattern("dd-MM-yyyy")

        // Mirem quines dates son valides durant els ultim 30 dies
        val datesValides = datesValides(Usuari.entrenos)
        val tamanyEntrenos = datesValides.size
        var carregaAgudaAhir = 0.0
        var carregaCronicaAhir = 0.0

        // Si hi han entrenos
        if (tamanyEntrenos > 0) {
            val data = datesValides[0][2].split(" ")
            val dataActual = LocalDate.now()
            val dataActualFormatejada = dataActual.format(format)
            // Si hi ha 1 entreno i la data es avui
            if (tamanyEntrenos == 1 && data[0] == dataActualFormatejada) {
                ratioCalculat = 1.0

                info.visibility = View.VISIBLE
                ratio.visibility = View.VISIBLE
                senseEntrenos.visibility = View.GONE
            } else if (tamanyEntrenos >= 1) {
                carregaAgudaAhir = datesValides[0][0].toDouble()
                carregaCronicaAhir = datesValides[0][0].toDouble()
                // Comprovem que estan totes les dates fins la data actual
                val datesEntrenos = datesValides.map { it[2].substring(0, 10) }
                val datesFaltants = comprovacioDates(datesEntrenos)

                // Si hi han dates que falten recorrem la llista i afegim als entrenos les dates faltants
                // per poder calcular les EWMA i el ratio despres
                if (datesFaltants.isNotEmpty()) {
                    for (dataFaltant in datesFaltants) {
                        val dataFormatejada = dataFaltant.format(format)
                        val llistaAfegir = listOf("0", "0",dataFormatejada + " 00:00:00",  "0")

                        // Comprovem a quina posicio s'ha d'afegir mirant si la data es anterior o no a la proporcionada
                        val index = datesValides.indexOfFirst { dataFaltant.isBefore(LocalDate.parse(it[2].substring(0, 10), format)) }
                        if (index < 0) {
                            datesValides.add(datesValides.size, llistaAfegir)
                        } else {
                            datesValides.add(index, llistaAfegir)
                        }
                    }
                }

                // Fem els calculs de les EWMA per posteriorment calcular el ratio actual
                for (i in 1 until datesValides.size) {
                    val km = datesValides[i][0].toInt()
                    val carregaAguda = km * unaSetmana + ((1 - unaSetmana) * carregaAgudaAhir)
                    val carregaCronica = km * quatreSetmanes + ((1 - quatreSetmanes) * carregaCronicaAhir)
                    carregaAgudaAhir = carregaAguda
                    carregaCronicaAhir = carregaCronica
                }
                ratioCalculat = carregaAgudaAhir / carregaCronicaAhir

                if (ratioCalculat > 1.25) {
                    info2.visibility = View.VISIBLE
                }

                info.visibility = View.VISIBLE
                ratio.visibility = View.VISIBLE
                senseEntrenos.visibility = View.GONE
            }
        }
        return ratioCalculat
    }

    // Funcio per comprovar que totes les dates des del primer dia d'entreno fins la data actual
    // en el llistat d'entrenos emmagatzemat
    private fun comprovacioDates(datesEntrenos: List<String>): List<LocalDate> {
        val dataActual = LocalDate.now()
        val datesFaltants = mutableListOf<LocalDate>()
        val format = DateTimeFormatter.ofPattern("dd-MM-yyyy")
        val datesFormatejades = datesEntrenos.map { LocalDate.parse(it, format) }

        // Mirem quin es el primer dia d'entreno
        var dataMinima = datesFormatejades.min()
        // Fem un bucle per recorrer les dates fins la data actual i a dins comprovem que existeix
        // un entreno amb aquella data. En cas negatiu afegim la data que falta a una llista
        while (!dataMinima.isAfter(dataActual)) {
            if (!datesFormatejades.contains(dataMinima)) {
                datesFaltants.add(dataMinima)
            }
            dataMinima = dataMinima.plusDays(1)
        }
        return datesFaltants
    }

    // Funcio per obtenir les dates de fa 30 dies
    private fun datesValides(llista: List<List<String>>): MutableList<List<String>> {
        val data30Dies = LocalDate.now().minusDays(30)
        val format = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")
        // Filtra las sublistas que contienen fechas más recientes que la fecha de hace 30 días
        return llista.filterTo(mutableListOf()) { subllista ->
            val dataSubllista = subllista.firstOrNull { it.matches(Regex("\\d{2}-\\d{2}-\\d{4} \\d{2}:\\d{2}:\\d{2}")) }
            dataSubllista?.let {
                val dataFormatejada = LocalDate.parse(it, format)
                return@filterTo dataFormatejada.isAfter(data30Dies)
            } ?: false
        }
    }
}