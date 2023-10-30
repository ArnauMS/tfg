package com.aplication.aplicaciotfg

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import java.util.regex.Matcher
import java.util.regex.Pattern

class LoginActivity : AppCompatActivity() {
    lateinit var entrar: AppCompatButton
    lateinit var email: EditText
    lateinit var password: EditText
    lateinit var registrarse: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val bd = BaseDades
        bd.apply {
            inicialitzarClaus(this@LoginActivity)
        }

        entrar = findViewById(R.id.entrar)
        email = findViewById(R.id.email)
        password = findViewById(R.id.contra)
        registrarse = findViewById(R.id.registrarse)

        entrar.setOnClickListener {
            val emailS = email.text.toString()
            val pass = password.text.toString()

            // Si tots els camps estan omplerts
            if (emailS.isNotEmpty() && pass.isNotEmpty()) {
                // Si el email es valid
                if (comprovarEmail(emailS)) {
                    val usuari = BaseDades.carregarUsuari(emailS)
                    if (usuari != null) {
                        if (usuari.contrasenya == pass) {
                            Usuari.nom = usuari.nom
                            Usuari.email = usuari.email
                            Usuari.contrasenya = usuari.contrasenya
                            Usuari.imatge = usuari.imatge
                            Usuari.entrenos = usuari.entrenos

                            // El usuari ha iniciat sessio amb exit
                            Toast.makeText(this, "Te has logeado con exito!", Toast.LENGTH_LONG).show()

                            // Canviar de pantalla a la pagina principal
                            val intent = Intent(this, PaginaPrincipalActivity::class.java)
                            intent.putExtra("titol", "Nom aplicacio")
                            startActivity(intent)
                        } else {
                            // Contrasenya incorrecte
                            Toast.makeText(this, "Contraseña incorrecta", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        // El usuari no esta registrat
                        Toast.makeText(this, "Usuario no registrado", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(this, "Introduzca un email valido", Toast.LENGTH_LONG).show()
                }
            } else {
                // Mostrar missatge que falten dades
                Toast.makeText(this, "Alguno de los campos esta incompleto", Toast.LENGTH_LONG).show()
            }
        }
        registrarse.setOnClickListener {
            // Canviar de pantalla a la pagina de registre
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun comprovarEmail(user: String): Boolean {
        val expresions = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$"
        val pattern: Pattern = Pattern.compile(expresions, Pattern.CASE_INSENSITIVE)
        val matcher: Matcher = pattern.matcher(user)
        return matcher.matches()
    }
}