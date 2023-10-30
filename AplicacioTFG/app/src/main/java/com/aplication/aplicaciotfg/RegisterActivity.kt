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
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.isVisible
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

    // Ens permet obtenir la uri de la imatge seleccionada
    val fotoTriada = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            logo.setImageURI(uri)
            uriImatge = uri
            seleccioImatge.isVisible = false
            imatgeSeleccionada = true
        }
    }

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
            fotoTriada.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        guardar.setOnClickListener{
            val nomS = nom.text.toString()
            val emailS = email.text.toString()
            val contraS = contra.text.toString()
            val contra_repetidaS = contra_repetida.text.toString()

            // Si estan tots els camps omplerts
            if (nomS.isNotEmpty() && emailS.isNotEmpty() && contraS.isNotEmpty() && contra_repetidaS.isNotEmpty() && imatgeSeleccionada) {
                // Si el email es valid
                if (comprovarEmail(emailS)) {
                    // Si les contrasenyes son iguals
                    if (contraS == contra_repetidaS) {
                        val usuari = BaseDades.carregarUsuari(emailS)
                        if (usuari == null) {
                            // Si el email no ha estat registrat es guarda l'usuari
                            BaseDades.registrarse(nomS, emailS, contraS, uriImatge)
                            Usuari.nom = nomS
                            Usuari.email = emailS
                            Usuari.contrasenya = contraS
                            Usuari.imatge = uriImatge

                            Toast.makeText(this, "Te has registrado con exito!", Toast.LENGTH_LONG).show()

                            // Canviar de pantalla a la pagina principal
                            val intent = Intent(this, PaginaPrincipalActivity::class.java)
                            intent.putExtra("titol", "Nom aplicacio")
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

    private fun comprovarEmail(user: String): Boolean {
        val expresions = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$"
        val pattern: Pattern = Pattern.compile(expresions, Pattern.CASE_INSENSITIVE)
        val matcher: Matcher = pattern.matcher(user)
        return matcher.matches()
    }
}