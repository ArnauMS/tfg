import android.content.Context
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.aplication.aplicaciotfg.R
import com.aplication.aplicaciotfg.Usuari
import java.util.*

class CalendarAdapter(private val context: Context, private var dataCalendari: Calendar) : BaseAdapter() {

    private val calendari = Calendar.getInstance()
    private val dataActual = Calendar.getInstance()
    private val diesSetmana = arrayOf("Lun", "Mar", "Mie", "Jue", "Vie", "Sab", "Dom", "Km")
    private val llistatDiesMes = mutableListOf<Int>()

    init {
        calendari.time = dataCalendari.time
        calendari.set(Calendar.DAY_OF_MONTH, 1)
        generarCalendari()
    }

    // Funcio per saber el tamany total de la taula
    override fun getCount(): Int {
        return diesSetmana.size + llistatDiesMes.size
    }

    // Funcio per obtenir el dia de la setmana, el numero del mes o els km d'una setmana
    // col·locats en una posicio en concret
    override fun getItem(posicio: Int): Any {
        val numColumnes = diesSetmana.size
        val casellesTotals = llistatDiesMes.size

        // Si la posicio es mes petit que 8, significa que es un dia de la setmana
        // i el retornem. Si es més petit que el tamany de la taula i més gran que 8,
        // és un dia o els km recorreguts i ho retornem. Si no es res de lo anterior,
        // significa que està fora del rang
        return if (posicio < numColumnes) {
            diesSetmana[posicio]
        } else if (posicio - numColumnes < casellesTotals) {
            llistatDiesMes[posicio - numColumnes]
        } else {
            -1
        }
    }

    override fun getItemId(posicio: Int): Long {
        return posicio.toLong()
    }

    // Funcio encarregada de printar per pantalla tota la informacio de la taula
    override fun getView(posicio: Int, convertView: View?, parent: ViewGroup?): View {
        val textView = TextView(context)
        val item = getItem(posicio)

        // Si es un dia de la setmana
        if (item is String) {
            textView.text = item
            textView.gravity = Gravity.CENTER
            textView.setTextColor(ContextCompat.getColor(context, R.color.black))
            textView.setBackgroundResource(R.drawable.dia_normal)
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20f)
        } else if (item is Int) { // Si es un dia del mes o els km recorreguts
            val dayOfMonth = item
            // Comprovem que es un dia del mes
            textView.text = if (dayOfMonth > -1) dayOfMonth.toString() else ""
            textView.textAlignment = View.TEXT_ALIGNMENT_CENTER

            if (diaActual(posicio)) { // Si es el dia actual el fons es d'un color
                textView.setBackgroundResource(R.drawable.dia_actual)
                textView.setTextColor(ContextCompat.getColor(context, R.color.black))
                textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20f)
            } else if (diaEntrenat(posicio)) { // Si es un dia entrenat el fons es d'un altre color
                textView.setBackgroundResource(R.drawable.dia_entrenat)
                textView.setTextColor(ContextCompat.getColor(context, R.color.black))
                textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20f)
            } else { // Sino el fons es transparent
                textView.setBackgroundResource(R.drawable.dia_normal)
                textView.setTextColor(ContextCompat.getColor(context, R.color.black))
                textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20f)
            }
        }
        return textView
    }

    // Funcio per generar la llista que anira a la taula
    private fun generarCalendari() {
        llistatDiesMes.clear()
        var aux = 0
        val mesActual = (dataCalendari.get(Calendar.MONTH) + 1).toString()
        val anyActual = dataCalendari.get(Calendar.YEAR).toString()

        // Asegurar que el primer día de la setmana sigui dilluns per aixi calcular
        // les caselles buides a afegir
        calendari.firstDayOfWeek = Calendar.MONDAY
        val casellesExtras = (calendari.get(Calendar.DAY_OF_WEEK) - calendari.firstDayOfWeek + 7) % 7

        val ultimDiaMes = calendari.getActualMaximum(Calendar.DAY_OF_MONTH)
        val totalCaselles = ultimDiaMes + casellesExtras
        val numeroSetmanes = (totalCaselles + 6) / 7

        // Calcular el numero de caselles tenint en conte que hi ha una columna extra
        // per mostrar els km recorreguts
        var totalCasellesFinal = totalCaselles + (8 - (totalCaselles + numeroSetmanes) % 8) % 8

        // Assegurar-se de mostrar una taula amb la ultima fila completada
        if (totalCasellesFinal % 8 != 0) {
            val casellesAfegir = 8 - totalCasellesFinal % 8
            totalCasellesFinal += casellesAfegir
        }

        // Bucle per omplir la llista llistatDiesMes amb els dies i kilometres
        for (i in 1..totalCasellesFinal) {
            // Per cada setmana sumem 1
            if (i % 8 == 0) {
                aux++
            }

            // Si es un dia del mes
            if (i > casellesExtras && i <= ultimDiaMes + casellesExtras + aux && i % 8 != 0) {
                llistatDiesMes.add(i - casellesExtras - aux)
            } else if (i % 8 == 0) { // Si es una casella per mostrar km
                var km = 0
                // Recorrem tots els entrenos per calcular el total recorregut aquella setmana
                for (entreno in Usuari.entrenos) {
                    // Per separar els diferents elements dins d'una data
                    val llista1 = entreno[2].split(" ")
                    val data = llista1[0].split("-")

                    // Comprovem que no comenci per 0 i en cas positiu eliminem el 0
                    val dia = if (data[0].startsWith("0")) {
                        data[0].removePrefix("0").toInt()
                    } else {
                        data[0].toInt()
                    }

                    // Comprovem que el mes no comenci per 0 i en cas positiu eliminem el 0
                    val mes = if (data[1].startsWith("0")) {
                        data[1].removePrefix("0").toInt()
                    } else {
                        data[1].toInt()
                    }

                    // Variables per saber el rang de la setmana
                    val petit = i - 6 - aux - casellesExtras
                    val gran = i - aux - casellesExtras

                    // Si hi han caselles extres abans del dia 1 s'ha de tenir en conta
                    // els km recorreguts l'ultima setmana del mes anterior. Si hi han
                    // caselles extres, son d'aquest mes i de la primera setmana sumem
                    // els km i si son de la ultima setmana del mes anterior tambe els sumem
                    if (petit > 0) {
                        if (mesActual.toInt() == mes && anyActual == data[2]) {
                            if (dia >= petit && dia <= gran) {
                                km += entreno[0].toInt()
                            }
                        }
                    } else {
                        if (mesActual.toInt() == mes && anyActual == data[2]) {
                            if (dia >= petit && dia <= gran) {
                                km += entreno[0].toInt()
                            }
                        } else if ((mesActual.toInt() - 1) == mes && anyActual == data[2]) {
                            val calendariAux = calendari
                            calendariAux.add(Calendar.MONTH, -1)
                            val ultimDiaMes = calendariAux.getActualMaximum(Calendar.DAY_OF_MONTH)
                            val petitAux = ultimDiaMes + petit
                            if (dia >= petitAux && dia <= ultimDiaMes) {
                                km = km + entreno[0].toInt()
                            }
                        }
                    }
                }
                llistatDiesMes.add(km)
            } else { // Si no es res de lo anterior afegim un -1 per indicar que ha d'estar buit
                llistatDiesMes.add(-1)
            }
        }
    }

    // Funcio per mirar si es el dia actual
    private fun diaActual(posicio: Int): Boolean {
        val item = getItem(posicio)
        // Si el item i el dia actual coincideixen retornem true
        if (item is Int && item > 0 && (posicio + 1) % 8 != 0) {
            val mesActual = (dataActual.get(Calendar.MONTH) + 1).toString()
            val anyActual = dataActual.get(Calendar.YEAR).toString()
            val mesCalendari = (dataCalendari.get(Calendar.MONTH) + 1).toString()
            val anyCalendari = dataCalendari.get(Calendar.YEAR).toString()
            val diaActual = dataActual.get(Calendar.DAY_OF_MONTH)

            // Comprovacio que es el dia actual
            if (item == diaActual && mesActual == mesCalendari && anyActual == anyCalendari && (posicio + 1) % 8 != 0) {
                return true
            }
        }
        return false
    }

    // Funcio per mirar quins dies s'ha entrenat
    private fun diaEntrenat(posicio: Int): Boolean {
        val item = getItem(posicio)
        val mesCalendari = (dataCalendari.get(Calendar.MONTH) + 1).toString()
        val anyCalendari = dataCalendari.get(Calendar.YEAR).toString()
        if (item is Int && item > 0) {
            // Recorrem tots els entrenos per mirar si el item coincideix amb el dia
            // entrenat. Si es que si retornem true
            for (entreno in Usuari.entrenos) {
                val llista1 = entreno[2].split(" ")
                val data = llista1[0].split("-")

                // Comprovem que el dia no comenci per 0 i en cas positiu eliminem el 0
                val dia = if (data[0].startsWith("0")) {
                    data[0].removePrefix("0").toInt()
                } else {
                    data[0].toInt()
                }

                // Comprovem que el mes no comenci per 0 i en cas positiu eliminem el 0
                val mes = if (data[1].startsWith("0")) {
                    data[1].removePrefix("0").toInt()
                } else {
                    data[1].toInt()
                }

                // Comprovacio de que aquell dia s'ha entrenat
                if (item == dia && mesCalendari.toInt() == mes && anyCalendari == data[2] && (posicio + 1) % 8 != 0 && entreno[0].toInt() != 0) {
                    return true
                }
            }
        }
        return false
    }
}
