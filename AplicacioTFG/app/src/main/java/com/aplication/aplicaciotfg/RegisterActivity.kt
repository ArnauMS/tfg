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
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import java.util.regex.Matcher
import java.util.regex.Pattern


class RegisterActivity : AppCompatActivity() {
    private lateinit var guardar: AppCompatButton
    private lateinit var nom: EditText
    private lateinit var email: EditText
    private lateinit var contra: EditText
    private lateinit var contra_repetida: EditText
    private lateinit var logo: ImageView
    private lateinit var seleccioImatge: TextView
    private lateinit var uriImatge: Uri
    private var imatgeSeleccionada: Boolean = false
    private val codiPermis = 123
    private val codiImatge = 456

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Obtenir els diferents elements del layout
        guardar = findViewById(R.id.guardar)
        nom = findViewById(R.id.nom)
        email = findViewById(R.id.email)
        contra = findViewById(R.id.contra)
        contra_repetida = findViewById(R.id.contra_repetida)
        logo = findViewById(R.id.logo)
        seleccioImatge = findViewById(R.id.seleccioImatge)

        // Al clicar per seleccionar la imatge ens permet seleccionar imatges del nostre dispossitiu
        seleccioImatge.setOnClickListener {
            comprovarPermissos()
        }

        guardar.setOnClickListener{
            val nomS = nom.text.toString()
            val emailS = email.text.toString()
            val contraS = contra.text.toString()
            val contra_repetidaS = contra_repetida.text.toString()

            // Si estan tots els camps omplerts
            if (nomS.isNotEmpty() && emailS.isNotEmpty() && contraS.isNotEmpty() && contra_repetidaS.isNotEmpty()) {
                // Si el email es valid
                if (comprovarEmail(emailS)) {
                    // Si les contrasenyes son iguals
                    if (contraS == contra_repetidaS) {
                        val usuari = BaseDades.carregarUsuari(emailS)
                        // Si el email no ha estat registrat es guarda l'usuari
                        if (usuari == null) {
                            if (imatgeSeleccionada) {
                                BaseDades.registrarse(nomS, emailS, contraS, uriImatge)
                                Usuari.imatge = uriImatge
                            } else {
                                BaseDades.registrarse(nomS, emailS, contraS, Uri.EMPTY)
                            }

                            Usuari.nom = nomS
                            Usuari.email = emailS
                            Usuari.contrasenya = contraS

                            Toast.makeText(this, "Te has registrado con exito!", Toast.LENGTH_LONG).show()

                            // Canviar de pantalla a la pagina principal
                            val intent = Intent(this, PaginaPrincipalActivity::class.java)
                            intent.putExtra("titol", "Training Control")
                            startActivity(intent)
                        } else {
                            // Email ja registrat
                            Toast.makeText(this, "Email ya registrado", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        // Mostrar missatge error
                        Toast.makeText(this, "Las contraseñas no coinciden!", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(this, "Introduzca un email valido", Toast.LENGTH_LONG).show()
                }
            } else {
                // Mostrar missatge que falten dades
                Toast.makeText(this, "Alguno de los campos esta incompleto", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Funcio per comprovar que s'introdueix un email valid
    private fun comprovarEmail(user: String): Boolean {
        val expresions = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$"
        val pattern: Pattern = Pattern.compile(expresions, Pattern.CASE_INSENSITIVE)
        val matcher: Matcher = pattern.matcher(user)
        return matcher.matches()
    }

    // Funcio per comprovar el permissos
    private fun comprovarPermissos() {
        val versionSDK = android.os.Build.VERSION.SDK_INT

        // Si sdk del dispositiu >= 33
        if (versionSDK >= 33) {
            // Comprovar si tinc permissos
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_MEDIA_IMAGES), codiPermis)
            } else { // Si ja tinc permissos crido la funcio per seleccionar la imatge
                seleccionadorImatge()
            }
        } else {
            // Comprovar si tinc permissos
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), codiPermis)
            } else { // Si ja tinc permissos crido la funcio per seleccionar la imatge
                seleccionadorImatge()
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
    override fun onRequestPermissionsResult(codiPeticio: Int, permissos: Array<out String>, resultat: IntArray) {
        super.onRequestPermissionsResult(codiPeticio, permissos, resultat)
        when (codiPeticio) {
            codiPermis -> {
                // Si accepta els permissos
                if (resultat.isNotEmpty() && resultat[0] == PackageManager.PERMISSION_GRANTED) {
                    seleccionadorImatge()
                } else {
                    // Mostrar missatge que s'han d'acceptar els permissos
                    Toast.makeText(this, "Para continuar permite el contenido multimedia de la aplicación desde configuración", Toast.LENGTH_LONG).show()
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