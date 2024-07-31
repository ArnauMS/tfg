package com.aplication.aplicaciotfg

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class IntroduirDadesActivity : NavegadorActivity() {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var btnMenu: AppCompatButton
    private lateinit var titol: TextView
    private lateinit var esforcPercebut: EditText
    private lateinit var temps: EditText
    private lateinit var kilometres: EditText
    private lateinit var dataEntreno: EditText
    private var comprovacio: Boolean = false
    private val calendari = Calendar.getInstance()
    private lateinit var guardar: AppCompatButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        layoutInflater.inflate(R.layout.activity_introduir_dades, findViewById(R.id.container))

        // Obtenir els diferents elements del layout
        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        btnMenu = findViewById(R.id.btnMenu)
        titol = findViewById(R.id.titol)
        esforcPercebut = findViewById(R.id.esforcPercebut)
        temps = findViewById(R.id.temps)
        kilometres = findViewById(R.id.kilometres)
        dataEntreno = findViewById(R.id.dataEntreno)
        guardar = findViewById(R.id.guardar)

        // Comprovacio de que s'introdueixen numeros del 1 al 10 o nomes 0
        esforcPercebut.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                s?.let {
                    val input = it.toString()
                    if (input.isNotEmpty()) {
                        val number = input.toIntOrNull()
                        if (number == null || (number < 0 || number > 10)) {
                            Toast.makeText(applicationContext,"Ingresa 0 o un número del 1 al 10",Toast.LENGTH_LONG).show()
                            it.clear()
                        } else if (input.length > 1 && input.startsWith("0")) {
                            Toast.makeText(applicationContext,"No se pueden añadir más números con el 0 delante",Toast.LENGTH_LONG).show()
                            it.replace(1, input.length, "")
                        }
                    }
                }
            }
        })

        // Comprovacio de que s'introdueixen numeros positius o nomes 0
        temps.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                s?.let {
                    val input = it.toString()
                    if (input.isNotEmpty()) {
                        val number = input.toIntOrNull()
                        if (number == null || number < 0) {
                            Toast.makeText(applicationContext,"Ingresa un número positivo",Toast.LENGTH_LONG).show()
                            it.clear()
                        } else if (input.length > 1 && input.startsWith("0")) {
                            Toast.makeText(applicationContext,"No se pueden añadir más números con el 0 delante",Toast.LENGTH_LONG).show()
                            it.replace(1, input.length, "")
                        }
                    }
                }
            }
        })

        // Comprovacio de que s'ingressin numeros positius amb com a maxim 2 decimals o nomes 0
        kilometres.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                s?.let {
                    val input = it.toString()
                    if (input.isNotEmpty()) {
                        val number = input.toDoubleOrNull()
                        if (number == null || number < 0) {
                            Toast.makeText(applicationContext,"Ingresa un número positivo",Toast.LENGTH_LONG).show()
                            it.clear()
                        } else if (input.contains(".") && input.substringAfter(".").length > 2) {
                            Toast.makeText(applicationContext,"No más de dos decimales permitidos", Toast.LENGTH_SHORT).show()
                            it.replace(it.indexOfLast { it == '.' } + 3, it.length, "")
                        } else if (input.length > 1 && input.startsWith("0")) {
                            Toast.makeText(applicationContext,"No se pueden añadir más números con el 0 delante",Toast.LENGTH_LONG).show()
                            it.replace(1, input.length, "")
                        }
                    }
                }
            }
        })

        guardar.setOnClickListener{
            val esforcS = esforcPercebut.text.toString()
            val tempsS = temps.text.toString()
            val kilometreS = kilometres.text.toString()
            var data = obtenirData()

            if(esforcS.isNotEmpty() || tempsS.isNotEmpty() || kilometreS.isNotEmpty()) {
                // Si l'usuari selecciona una data
                if (dataEntreno.text.toString().isNotEmpty()) {
                    val datesEntrenos = Usuari.entrenos.map { it[2].substring(0, 10) }
                    comprovacio = comprovacioData(datesEntrenos, dataEntreno.text.toString())

                    // Si no s'ha afegit cap entreno aquell dia es permet que l'introdueixi
                    if (comprovacio) {
                        data = dataEntreno.text.toString()
                        val format = DateTimeFormatter.ofPattern("dd-MM-yyyy")
                        val dataEntrenoFormatejada = LocalDate.parse(dataEntreno.text.toString(), format)

                        // Si no hi han entrenos s'afegeix a la llista i s'actualitza les EWMA inicials
                        if (Usuari.entrenos.size == 0) {
                            val llista: List<String> = listOf(kilometreS, tempsS, data + " 00:00:00", esforcS)
                            Usuari.entrenos.add(llista)
                            Usuari.carregaAgudaAhir = kilometreS.toDouble()
                            Usuari.carregaCronicaAhir = kilometreS.toDouble()
                            BaseDades.emmagatzemarEwma(Usuari.email, kilometreS.toDouble(), kilometreS.toDouble())
                        } else {
                            // Comprovem a quina posicio s'ha d'afegir mirant si la data es anterior o no a la proporcionada
                            val index = Usuari.entrenos.indexOfFirst { dataEntrenoFormatejada.isBefore(LocalDate.parse(it[2].substring(0, 10), format)) }
                            val llista: List<String> = listOf(kilometreS, tempsS, data + " 00:00:00", esforcS)

                            // Si la data es més gran que l'ultima que hi ha a la llista
                            if (index < 0) {
                                Usuari.entrenos.add(Usuari.entrenos.size, llista)
                            } else if (index == 0) { // Si no hi ha cap data mes petita que la del entreno s'actualitza les EWMA inicials
                                Usuari.entrenos.add(index, llista)
                                Usuari.carregaAgudaAhir = kilometreS.toDouble()
                                Usuari.carregaCronicaAhir = kilometreS.toDouble()
                                BaseDades.emmagatzemarEwma(Usuari.email, kilometreS.toDouble(), kilometreS.toDouble())
                            } else { // Sino l'afegeixes a la posicio indicada
                                Usuari.entrenos.add(index, llista)
                            }
                        }

                        // Cridem la funcio actualitzarEntrenos per guardar-ho al JSON
                        BaseDades.actualitzarEntrenos(Usuari.email, Usuari.entrenos)

                        Toast.makeText(this, "Datos guardados correctamente!", Toast.LENGTH_LONG).show()

                        // Canviar de pantalla a la pagina principal
                        val intent = Intent(this, PaginaPrincipalActivity::class.java)
                        intent.putExtra("titol", "Training Control")
                        startActivity(intent)
                    } else {
                        Toast.makeText(this, "Ya has añadido un entrenamiento en esta fecha", Toast.LENGTH_LONG).show()
                    }
                } else {
                    if (Usuari.entrenos.size == 0) {
                        val llista: List<String> = listOf(kilometreS, tempsS, data, esforcS)
                        // Afegim les noves dades
                        Usuari.entrenos.add(llista)
                        Usuari.carregaAgudaAhir = kilometreS.toDouble()
                        Usuari.carregaCronicaAhir = kilometreS.toDouble()
                        // Cridem la funcio actualitzarEntrenos i emmagatzemarEwma per guardar-ho tot al JSON
                        BaseDades.actualitzarEntrenos(Usuari.email, Usuari.entrenos)
                        BaseDades.emmagatzemarEwma(Usuari.email, kilometreS.toDouble(), kilometreS.toDouble())

                        Toast.makeText(this, "Datos guardados correctamente!", Toast.LENGTH_LONG).show()

                        // Canviar de pantalla a la pagina principal
                        val intent = Intent(this, PaginaPrincipalActivity::class.java)
                        intent.putExtra("titol", "Training Control")
                        startActivity(intent)
                    } else {
                        val dataEntreno = Usuari.entrenos[Usuari.entrenos.size - 1][2].split(" ")
                        val dataActual = data.split(" ")

                        // Si avui encara no s'ha introduit cap data
                        if (dataEntreno[0] != dataActual[0]) {
                            val llista: List<String> = listOf(kilometreS, tempsS, data, esforcS)
                            // Afegim les noves dades
                            Usuari.entrenos.add(llista)
                            // Cridem la funcio actualitzarEntrenos per guardar-ho al JSON
                            BaseDades.actualitzarEntrenos(Usuari.email, Usuari.entrenos)

                            Toast.makeText(this, "Datos guardados correctamente!", Toast.LENGTH_LONG).show()

                            // Canviar de pantalla a la pagina principal
                            val intent = Intent(this, PaginaPrincipalActivity::class.java)
                            intent.putExtra("titol", "Training Control")
                            startActivity(intent)
                        } else {
                            Toast.makeText(this, "Hoy ya has añadido un entrenamiento", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Alguno de los campos esta incompleto", Toast.LENGTH_LONG).show()
            }
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

    // Funcio per obtenir la data actual
    private fun obtenirData(): String {
        val zonaHorariaEspanya = ZoneId.of("Europe/Madrid")
        val dataHoraEspanyola = ZonedDateTime.now(zonaHorariaEspanya)
        val format = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")

        return dataHoraEspanyola.format(format)
    }

    // Funcio per comprovar que no s'ha afegit cap entreno en un dia concret
    private fun comprovacioData(datesEntrenos: List<String>, dataEntreno: String): Boolean {
        val format = DateTimeFormatter.ofPattern("dd-MM-yyyy")
        val datesFormatejades = datesEntrenos.map { LocalDate.parse(it, format) }
        val dataEntrenoFormatejada = LocalDate.parse(dataEntreno, format)

        if (!datesFormatejades.contains(dataEntrenoFormatejada)) {
            return true
        }
        return false
    }

    // Funcio per seleccionar una data en el calendari DatePicker
    fun onClick(v: View?) {
        val any = calendari.get(Calendar.YEAR)
        val mes = calendari.get(Calendar.MONTH)
        val dia = calendari.get(Calendar.DAY_OF_MONTH)
        val listener = DatePickerDialog.OnDateSetListener{ datePicker, a, m, d ->
            calendari.set(a, m ,d)
            var month = m
            month += 1
            val dS = d.toString().padStart(2, '0')
            val mS = month.toString().padStart(2, '0')
            dataEntreno.setText("$dS-$mS-$a")
        }
        DatePickerDialog(this, listener, any, mes, dia).show()
    }
}