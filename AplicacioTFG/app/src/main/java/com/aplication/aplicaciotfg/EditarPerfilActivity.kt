package com.aplication.aplicaciotfg

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView

class EditarPerfilActivity : NavegadorActivity() {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var btnMenu: AppCompatButton
    private lateinit var titol: TextView
    private lateinit var guardar: AppCompatButton
    private lateinit var nom: EditText
    private lateinit var contra: EditText
    private lateinit var contra_repetida: EditText
    private lateinit var logo: ImageView
    private lateinit var seleccioImatge: TextView
    private var uriImatge: Uri? = null
    private var imatgeSeleccionada: Boolean = false
    private val codiPermis = 123
    private val codiImatge = 456

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        layoutInflater.inflate(R.layout.activity_editar_perfil, findViewById(R.id.container))

        // Obtenir els diferents elements del layout
        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        btnMenu = findViewById(R.id.btnMenu)
        titol = findViewById(R.id.titol)
        guardar = findViewById(R.id.guardar)
        nom = findViewById(R.id.nom)
        contra = findViewById(R.id.contra)
        contra_repetida = findViewById(R.id.contra_repetida)
        logo = findViewById(R.id.logo)
        seleccioImatge = findViewById(R.id.seleccioImatge)

        // Titol de la pantalla que rep per parametre
        val titolExtra: String = intent.extras?.getString("titol").orEmpty()
        titol.text = titolExtra

        // Comprovem els permissos i en cas de tenir-los carreguem la imatge
        if (Usuari.imatge != Uri.EMPTY) {
            comprovarPermissos()
        }

        // Carregar nom del usuari
        nom.hint = Usuari.nom

        // Al clicar per seleccionar la imatge ens permet seleccionar imatges del nostre dispossitiu
        seleccioImatge.setOnClickListener {
            seleccionadorImatge()
        }

        guardar.setOnClickListener {
            val nomS = nom.text.toString()
            val contraS = contra.text.toString()
            val contra_repetidaS = contra_repetida.text.toString()

            if (imatgeSeleccionada || nomS.isNotEmpty() || (contraS.isNotEmpty() && contra_repetidaS.isNotEmpty())) {
                // Comprovem que la imatge s'ha modificat
                if (imatgeSeleccionada) {
                    BaseDades.actualitzarUsuari(Usuari.email, nomS, contraS, uriImatge)
                    Usuari.imatge = uriImatge!!
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
                intent.putExtra("titol", "Training Control")
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

    // Funcio per comprovar el permissos
    private fun comprovarPermissos() {
        val versionSDK = Build.VERSION.SDK_INT

        // Si sdk del dispositiu >= 33
        if (versionSDK >= 33) {
            // Comprovar si tinc permissos
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_MEDIA_IMAGES), codiPermis)
            } else { // Si ja tinc permissos crido la funcio per seleccionar la imatge
                Glide.with(this).load(Usuari.imatge).into(logo)
            }
        } else {
            // Comprovar si tinc permissos
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), codiPermis)
            } else { // Si ja tinc permissos crido la funcio per seleccionar la imatge
                Glide.with(this).load(Usuari.imatge).into(logo)
            }
        }
    }

    // Funcio per selecionar la imatge
    private fun seleccionadorImatge() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "image/*"
        startActivityForResult(intent, codiImatge)
    }

    // Funcio per demanar permissos
    override fun onRequestPermissionsResult(codiPeticio: Int, permmissos: Array<out String>, resultat: IntArray) {
        super.onRequestPermissionsResult(codiPeticio, permmissos, resultat)
        when (codiPeticio) {
            codiPermis -> {
                // Si accepta els permissos
                if (resultat.isNotEmpty() && resultat[0] == PackageManager.PERMISSION_GRANTED) {
                    seleccionadorImatge()
                } else {
                    // Mostrar missatge que s'han d'acceptar els permissos
                    Toast.makeText(this, "Acepta los permisos para continuar", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // Funcio que emmagatzema el valor seleccionat al seleccionar una imatge
    override fun onActivityResult(codiPeticio: Int, codiResultat: Int, data: Intent?) {
        super.onActivityResult(codiPeticio, codiResultat, data)
        if (codiPeticio == codiImatge && codiResultat == RESULT_OK) {
            uriImatge = data?.data!!
            Glide.with(this).load(uriImatge).into(logo)
            seleccioImatge.isVisible = false
            imatgeSeleccionada = true
        }
    }
}