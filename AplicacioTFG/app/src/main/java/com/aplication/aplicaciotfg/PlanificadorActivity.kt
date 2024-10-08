package com.aplication.aplicaciotfg

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class PlanificadorActivity : NavegadorActivity() {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var btnMenu: AppCompatButton
    private lateinit var titol: TextView
    private lateinit var cercle: ImageView
    private lateinit var rang_km: TextView
    private lateinit var frase: TextView
    private lateinit var frase2: TextView
    private lateinit var senseEntrenos: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        layoutInflater.inflate(R.layout.activity_planificador, findViewById(R.id.container))

        // Obtenir els diferents elements del layout
        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        btnMenu = findViewById(R.id.btnMenu)
        titol = findViewById(R.id.titol)
        cercle = findViewById(R.id.cercle)
        rang_km = findViewById(R.id.rang_km)
        frase = findViewById(R.id.frase)
        frase2 = findViewById(R.id.frase2)
        senseEntrenos = findViewById(R.id.senseEntrenos)

        // Funcio per calcular EWMA ahir
        val (carregaAgudaAhir, carregaCronicaAhir) = calcularEwmaAhir()
        if (carregaAgudaAhir != 0.0 && carregaCronicaAhir != 0.0) {
            // Cridem a la funcio que calcula els km
            val (minim, maxim) = planificador(carregaAgudaAhir, carregaCronicaAhir)
            rang_km.text = "${minim} - ${maxim} Km"
        }

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

    // Funcio per predir els kilometres que pot recorrer l'usuari
    private fun planificador(carregaAgudaAhir: Double, carregaCronicaAhir: Double): Pair<Double,Double> {
        val quatreSetmanes = 2.0 / 29
        val unaSetmana = 2.0 / 8
        var i = 0.0
        var minim = 0.0
        var maxim = 0.0

        if (Usuari.entrenos.size > 0) {
            // Obtenim els km a recorrer comprovan que el valor estigui entre 0.8 i 1.3
            while (minim == 0.0 || maxim == 0.0) {
                val tolerancia = 0.01
                val cargaAguda = i * unaSetmana + ((1 - unaSetmana) * carregaAgudaAhir)
                val cargaCronica = i * quatreSetmanes + ((1 - quatreSetmanes) * carregaCronicaAhir)
                val ratio = cargaAguda / cargaCronica

                if ((Math.abs(ratio - 0.80) <= tolerancia) && minim == 0.0) {
                    minim = i
                } else if ((Math.abs(ratio - 1.3) <= tolerancia) && maxim == 0.0) {
                    maxim = i
                }
                i += 0.1
                if (ratio >= 1.32) {
                    break
                }
            }
        }
        return Pair(String.format(Locale.US, "%.2f", minim).toDouble(), String.format(Locale.US, "%.2f", maxim).toDouble())
    }

    // Funcio per calcular les EWMA per poder predir els km a recorrer
    private fun calcularEwmaAhir(): Pair<Double,Double> {
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
            carregaAgudaAhir = datesValides[0][0].toDouble()
            carregaCronicaAhir = datesValides[0][0].toDouble()
            val data = datesValides[0][2].split(" ")
            val dataActual = LocalDate.now()
            val dataActualFormatejada = dataActual.format(format)
            // Si hi ha 1 entreno i la data es avui
            if (tamanyEntrenos == 1 && data[0] == dataActualFormatejada) {
                carregaAgudaAhir = datesValides[0][0].toDouble()
                carregaCronicaAhir = datesValides[0][0].toDouble()

                cercle.visibility = View.VISIBLE
                rang_km.visibility = View.VISIBLE
                frase.visibility = View.VISIBLE
                senseEntrenos.visibility = View.GONE

                // Emmagatzemem les EWMA per un futur us
                BaseDades.emmagatzemarEwma(Usuari.email, carregaAgudaAhir, carregaCronicaAhir)
            } else if (tamanyEntrenos >= 1) { // Si hi ha mes entrenos calculem les ultimes EWMA
                carregaAgudaAhir = datesValides[0][0].toDouble()
                carregaCronicaAhir = datesValides[0][0].toDouble()
                // Comprovem que estan totes les dates fins la data actual
                val datesEntrenos = datesValides.map { it[2].substring(0, 10) } // substracción para obtener solo la fecha
                val datesFaltants = comprovacioDates(datesEntrenos)

                // Si hi han dates que falten recorrem la llista i afegim als entrenos les dates faltants
                // per poder calcular les EWMA despres
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

                // Fem els calculs de les EWMA per posteriorment poder predir els km que s'han de recorrer
                for (i in 1 until datesValides.size) {
                    val km = datesValides[i][0].toDouble()
                    val carregaAguda = km * unaSetmana + ((1 - unaSetmana) * carregaAgudaAhir)
                    val carregaCronica = km * quatreSetmanes + ((1 - quatreSetmanes) * carregaCronicaAhir)
                    carregaAgudaAhir = carregaAguda
                    carregaCronicaAhir = carregaCronica
                }
                ratioCalculat = carregaAgudaAhir / carregaCronicaAhir

                if (ratioCalculat > 1.25) {
                    frase2.visibility = View.VISIBLE
                }

                cercle.visibility = View.VISIBLE
                rang_km.visibility = View.VISIBLE
                frase.visibility = View.VISIBLE
                senseEntrenos.visibility = View.GONE
            }
        }
        return Pair(carregaAgudaAhir, carregaCronicaAhir)
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