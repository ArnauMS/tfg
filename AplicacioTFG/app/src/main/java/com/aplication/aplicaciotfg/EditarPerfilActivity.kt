package com.aplication.aplicaciotfg

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import java.util.regex.Matcher
import java.util.regex.Pattern

class EditarPerfilActivity : NavegadorActivity() {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var btnMenu: AppCompatButton
    private lateinit var guardar: AppCompatButton
    private lateinit var nom: EditText
    private lateinit var contra: EditText
    private lateinit var contra_repetida: EditText
    private lateinit var logo: ImageView
    private lateinit var seleccioImatge: TextView
    private var uriImatge: Uri? = null
    private var imatgeSeleccionada: Boolean = false

    val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            logo.setImageURI(uri)
            uriImatge = uri
            seleccioImatge.isVisible = false
            imatgeSeleccionada = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        layoutInflater.inflate(R.layout.activity_editar_perfil, findViewById(R.id.container))
        val titol = findViewById<TextView>(R.id.titol)
        // Titol de la pantalla que rep per parametre
        val titolExtra: String = intent.extras?.getString("titol").orEmpty()
        titol.text = titolExtra

        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        btnMenu = findViewById(R.id.btnMenu)
        guardar = findViewById(R.id.guardar)
        nom = findViewById(R.id.nom)
        contra = findViewById(R.id.contra)
        contra_repetida = findViewById(R.id.contra_repetida)
        logo = findViewById(R.id.logo)
        seleccioImatge = findViewById(R.id.seleccioImatge)

        nom.hint = Usuari.nom
        logo.setImageURI(Usuari.imatge)

        seleccioImatge.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        guardar.setOnClickListener {
            val nomS = nom.text.toString()
            val contraS = contra.text.toString()
            val contra_repetidaS = contra_repetida.text.toString()

            if (imatgeSeleccionada || nomS.isNotEmpty() || (contraS.isNotEmpty() && contra_repetidaS.isNotEmpty())) {
                // Comprovem que la imatge s'ha modificat
                if (imatgeSeleccionada) {
                    BaseDades.actualitzarUsuari(Usuari.email, nomS, contraS, uriImatge)
                    Usuari.imatge = uriImatge
                }

                // Comprovem que el nom s'ha modificat
                if (nomS.isNotEmpty()) {
                    BaseDades.actualitzarUsuari(Usuari.email, nomS, contraS, uriImatge)
                    Usuari.nom = nomS
                }

                // Comprovem que la contrasenya s'ha modificat
                if (contraS.isNotEmpty() && contra_repetidaS.isNotEmpty()) {
                    // Si les contrasenyes son iguals
                    if (contraS == contra_repetidaS) {
                        BaseDades.actualitzarUsuari(Usuari.email, nomS, contraS, uriImatge)
                        Usuari.contrasenya = contraS
                    }
                }

                Toast.makeText(this, "Cambios guardados", Toast.LENGTH_LONG).show()

                // Canviar de pantalla a la pagina principal
                val intent = Intent(this, PaginaPrincipalActivity::class.java)
                intent.putExtra("titol", "Nom aplicacio")
                startActivity(intent)
            } else {
                Toast.makeText(this, "Modifica algun dato", Toast.LENGTH_LONG).show()
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

    private fun comprovarEmail(user: String): Boolean {
        val expresions = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$"
        val pattern: Pattern = Pattern.compile(expresions, Pattern.CASE_INSENSITIVE)
        val matcher: Matcher = pattern.matcher(user)
        return matcher.matches()
    }
}