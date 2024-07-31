package com.aplication.aplicaciotfg

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView

class HistorialActivity : NavegadorActivity() {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var btnMenu: AppCompatButton
    private lateinit var titol: TextView
    private lateinit var llistaBuida: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var divider: DividerItemDecoration

    interface OnItemClickListener {
        fun onDeleteClick(position: Int, context: Context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        layoutInflater.inflate(R.layout.activity_historial, findViewById(R.id.container))

        // Obtenir els diferents elements del layout
        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        btnMenu = findViewById(R.id.btnMenu)
        titol = findViewById(R.id.titol)
        recyclerView = findViewById(R.id.recyclerView)
        llistaBuida = findViewById(R.id.llistaBuida)

        // Titol de la pantalla que rep per parametre
        val titolExtra: String = intent.extras?.getString("titol").orEmpty()
        titol.text = titolExtra

        if (Usuari.entrenos.isNotEmpty()) {
            llistaBuida.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE

            // Configuracio de l'adaptador de l'activitat per mostrar el llistat
            val adapter = MyAdapter(Usuari.entrenos.reversed() as MutableList<List<String>>)
            recyclerView.layoutManager = LinearLayoutManager(this)
            recyclerView.adapter = adapter
            divider = DividerItemDecoration(this, LinearLayoutManager.VERTICAL)
            recyclerView.addItemDecoration(divider)

            // Configuracio del OnItemClickListener
            adapter.setOnItemClickListener(object : OnItemClickListener {
                override fun onDeleteClick(position: Int, context: Context) {
                    val posicioEliminar = Usuari.entrenos.size - position - 1

                    // Si la posicio a eliminar és la primera del llistat d'entrenaments i el tamany es
                    // mes gran que 1 es canvia l'EWMA inicial
                    if (posicioEliminar == Usuari.entrenos.size - 1 && Usuari.entrenos.size > 1) {
                        Usuari.carregaAgudaAhir = Usuari.entrenos[1][0].toDouble()
                        Usuari.carregaCronicaAhir = Usuari.entrenos[1][0].toDouble()
                        BaseDades.emmagatzemarEwma(Usuari.email, Usuari.entrenos[1][0].toDouble(), Usuari.entrenos[1][0].toDouble())
                    } else if (Usuari.entrenos.size == 1) { // Si el tamany es 1 i s'elimina es canvia l'EWMA inicial
                        Usuari.carregaAgudaAhir = 0.0
                        Usuari.carregaCronicaAhir = 0.0
                        BaseDades.emmagatzemarEwma(Usuari.email, 0.0, 0.0)
                    }

                    // Eliminar element de la llista en posicio especifica
                    Usuari.entrenos.removeAt(Usuari.entrenos.size - position - 1)
                    adapter.notifyItemRemoved(position)
                    BaseDades.actualitzarEntrenos(Usuari.email, Usuari.entrenos)

                    // Comprovem que la llista te elements per mostrar un missatge o mostrar el llistat
                    if (Usuari.entrenos.isEmpty()) {
                        llistaBuida.visibility = View.VISIBLE
                        recyclerView.visibility = View.GONE
                    }

                    // Canviar de pantalla a la pagina principal
                    val intent = Intent(context, PaginaPrincipalActivity::class.java)
                    intent.putExtra("titol", "Training Control")
                    startActivity(intent)
                }
            })
        } else {
            llistaBuida.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
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

    private inner class MyAdapter(private val llistaDeLlistes: MutableList<List<String>>) : RecyclerView.Adapter<MyAdapter.ViewHolder>() {
        private var onItemClickListener: OnItemClickListener? = null
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            // Carreguem la vista per cada element de la llista
            val vista = LayoutInflater.from(parent.context).inflate(R.layout.llistat_items, parent, false)
            return ViewHolder(vista)
        }

        // Funcio per obtenir la subllista de la posicio actual
        override fun onBindViewHolder(holder: ViewHolder, pos: Int) {
            val item = llistaDeLlistes[pos]
            holder.bind(item)
        }

        // Funcio per obtenir el tamany de la llista
        override fun getItemCount(): Int {
            return llistaDeLlistes.size
        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val item: TextView = itemView.findViewById(R.id.item)
            val subItem: TextView = itemView.findViewById(R.id.subItem)
            val basura: ImageView = itemView.findViewById(R.id.basura)

            init {
                basura.setOnClickListener {
                    onItemClickListener?.onDeleteClick(adapterPosition, itemView.context)
                }
            }

            // Funcio per mostrar la subllista. Tenim l'element principal i un subitem
            fun bind(llista: List<String>) {
                item.text = llista[0] + " km"
                subItem.text = llista[1] + " min" + "\n" + llista[2]
            }
        }

        fun setOnItemClickListener(listener: OnItemClickListener) {
            this.onItemClickListener = listener
        }
    }
}