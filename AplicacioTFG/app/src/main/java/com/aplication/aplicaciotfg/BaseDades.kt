package com.aplication.aplicaciotfg

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.KeyPairGenerator
import java.security.Security
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.security.KeyFactory
import javax.crypto.Cipher
import java.io.File
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

object BaseDades {
    private const val PREF_KEY_EJECUCION = "execucio_realitzada"
    private var dataFile = File("/data/user/0/com.aplication.aplicaciotfg/files/claus/data.json")

    private lateinit var clauPublica: PublicKey
    private lateinit var clauPrivada: PrivateKey

    init {
        Security.addProvider(BouncyCastleProvider())
    }

    // Funció per inicialitzar les claus
    fun inicialitzarClaus(context: Context) {
        if (!dataFile.exists()) {
            dataFile.parentFile?.mkdirs()
            dataFile.createNewFile()
        }

        println("Entra")
        val arxiuClauPublica = File(context.filesDir, "claus/clau_publica.der")
        val arxiuClauPrivada = File(context.filesDir, "claus/clau_privada.der")
        if (!arxiuClauPublica.exists() || !arxiuClauPrivada.exists()) {
            generarClaus(context)
        }

        val clauPublicaBytes = arxiuClauPublica.readBytes()
        val clauPrivadaBytes = arxiuClauPrivada.readBytes()

        val clauPublicaSpec = X509EncodedKeySpec(clauPublicaBytes)
        val clauPrivadaSpec = PKCS8EncodedKeySpec(clauPrivadaBytes)

        val keyFactory = KeyFactory.getInstance("RSA")
        clauPublica = keyFactory.generatePublic(clauPublicaSpec)
        clauPrivada = keyFactory.generatePrivate(clauPrivadaSpec)
    }

    // Funcio per generar claus
    private fun generarClaus(context: Context) {
        val generador = KeyPairGenerator.getInstance("RSA")
        generador.initialize(2048)
        val claus = generador.generateKeyPair()

        // Acceder al directorio de archivos privados de la actividad
        val directori = File(context.filesDir, "claus")  // Directorio específico de tu aplicación

        // Asegurarse de que el directorio exista
        if (!directori.exists()) {
            directori.mkdirs()
        }

        val arxiuClauPublica = File(directori, "clau_publica.der")
        if (!arxiuClauPublica.exists()) {
            arxiuClauPublica.parentFile?.mkdirs()
            arxiuClauPublica.createNewFile()
            arxiuClauPublica.writeBytes(claus.public.encoded)
        }

        val arxiuClauPrivada = File(directori, "clau_privada.der")
        if (!arxiuClauPrivada.exists()) {
            arxiuClauPrivada.parentFile?.mkdirs()
            arxiuClauPrivada.createNewFile()
            arxiuClauPrivada.writeBytes(claus.private.encoded)
        }
    }

    // Funció per xifrar dades amb clau pública
    fun encriptarDades(dades: String, clauPublica: PublicKey): String {
        val cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding")
        cipher.init(Cipher.ENCRYPT_MODE, clauPublica)
        val dadesEncriptades = cipher.doFinal(dades.toByteArray())
        return Base64.getEncoder().encodeToString(dadesEncriptades)
    }

    // Funció per desxifrar dades amb clau privada
    fun desencriptarDades(dades: String, clauPrivada: PrivateKey): String {
        val dadesEncriptades = Base64.getDecoder().decode(dades)
        val cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding")
        cipher.init(Cipher.DECRYPT_MODE, clauPrivada)
        val dadesDesencriptades = cipher.doFinal(dadesEncriptades)
        return String(dadesDesencriptades)
    }

    // Funció per desar un usuari al fitxer JSON
    fun registrarse(nom: String, email: String, contrasenya: String, imatge: Uri) {
        println("Entra registrar")
        // Comprovar que existeix el fitxer i en cas positiu carregar-lo
        val json = if (dataFile.exists() && dataFile.length() > 0) {
            JSONObject(dataFile.readText())
        } else {
            JSONObject()
        }

        // Comprovar que existeix la variable usuaris
        val arrayUsuaris = if (json.has("usuaris")) {
            json.getJSONArray("usuaris")
        } else {
            JSONArray()
        }

        // Creem un objecte JSON per poder adjuntarlo al fitxer JSON
        val usuari = JSONObject()
        usuari.put("nom", nom)
        val emailEncriptat = encriptarDades(email, clauPublica)
        usuari.put("email", emailEncriptat)
        val contrasenyaEncriptada = encriptarDades(contrasenya, clauPublica)
        usuari.put("contrasenya", contrasenyaEncriptada)
        usuari.put("imatge", imatge.toString())

        arrayUsuaris.put(usuari)
        json.put("usuaris", arrayUsuaris)

        // Guardem l'usuari al fitxer JSON
        dataFile.writeText(json.toString())
    }

    // Funció per carregar tots els usuaris
//    fun carregarUsuaris(): List<DadesUsuari> {
//        val usuaris = mutableListOf<DadesUsuari>()
//
//        if (dataFile.exists()) {
//            val jsonText = dataFile.readText()
//            val json = JSONObject(jsonText)
//
//            if (json.has("usuaris")) {
//                val arrayUsuaris = json.getJSONArray("usuaris")
//                for (i in 0 until arrayUsuaris.length()) {
//                    val usuari = arrayUsuaris.getJSONObject(i)
//                    val nom = usuari.getString("nom")
//                    val emailEncriptat = usuari.getString("email")
//                    val contrasenyaEncriptada = usuari.getString("contrasenya")
//                    val email = desencriptarDades(emailEncriptat, clauPrivada)
//                    val contrasenya = desencriptarDades(contrasenyaEncriptada, clauPrivada)
//                    // Si l'usuari te entrenos els carreguem, sino enviem una llista buida
//                        val entrenos: List<List<String>> = if (usuari.has("entrenos")) {
//                            val entrenosJson = usuari.getJSONArray("entrenos")
//                            // Convertir un JSONArray de llistes en una llista a Kotlin
//                            (0 until entrenosJson.length()).map { i ->
//                                val llistatDades = entrenosJson.getJSONArray(i)
//                                (0 until llistatDades.length()).map { j ->
//                                    llistatDades.getString(j)
//                                }
//                            }
//                        } else {
//                            emptyList()
//                        }
//                    usuaris.add(DadesUsuari(nom, email, contrasenya, entrenos))
//                }
//            }
//        }
//
//        return usuaris
//    }

    // Funció per carregar un usuari pel seu email
    fun carregarUsuari(email: String): DadesUsuari? {
        // Comprovem que el fitxer JSON existeix
        if (dataFile.exists() && dataFile.length() > 0) {
            val jsonText = dataFile.readText()
            val json = JSONObject(jsonText)

            // Comprovar que existeix la variable usuaris
            if (json.has("usuaris")) {
                val arrayUsuaris = json.getJSONArray("usuaris")
                // Recorrem tots els usuaris
                for (i in 0 until arrayUsuaris.length()) {
                    val usuari = arrayUsuaris.getJSONObject(i)
                    val emailEncriptat = usuari.getString("email")
                    val emailDesencriptat = desencriptarDades(emailEncriptat, clauPrivada)

                    // Si els emails coincideixen retornem tota la informacio de l'usuari
                    if (emailDesencriptat == email) {
                        val nom = usuari.getString("nom")
                        val contrasenyaEncriptada = usuari.getString("contrasenya")
                        val contrasenya = desencriptarDades(contrasenyaEncriptada, clauPrivada)
                        val imatge = usuari.getString("imatge").toUri()

                        // Si l'usuari te entrenos els carreguem, sino enviem una llista buida
                        val entrenos: List<List<String>> = if (usuari.has("entrenos")) {
                            val entrenosJson = usuari.getJSONArray("entrenos")
                            // Convertir un JSONArray de llistes en una llista a Kotlin
                            (0 until entrenosJson.length()).map { i ->
                                val llistatDades = entrenosJson.getJSONArray(i)
                                (0 until llistatDades.length()).map { j ->
                                    llistatDades.getString(j)
                                }
                            }
                        } else {
                            emptyList()
                        }
                        DadesUsuari(nom, emailDesencriptat, contrasenya, imatge, entrenos)
                    }
                }
            }
        }
        return null
    }

    // Funcio per actualitzar el perfil de l'usuari
    fun actualitzarUsuari(email: String, nouNom: String?, novaContrasenya: String?, novaImatge: Uri?): Boolean {
        // Comprovem que el fitxer JSON existeix
        if (dataFile.exists() && dataFile.length() > 0) {
            val jsonText = dataFile.readText()
            val json = JSONObject(jsonText)

            // Comprovar que existeix la variable usuaris
            if (json.has("usuaris")) {
                val arrayUsuaris = json.getJSONArray("usuaris")
                // Recorrem tots els usuaris
                for (i in 0 until arrayUsuaris.length()) {
                    val usuari = arrayUsuaris.getJSONObject(i)
                    val emailEncriptat = usuari.getString("email")
                    val emailDesencriptat = desencriptarDades(emailEncriptat, clauPrivada)

                    // Si els emails coincideixen
                    if (emailDesencriptat == email) {
                        // Si s'ha modificat el valor sera actualizat, sino es deixara el que hi havia
                        nouNom?.let {
                            usuari.put("nom", nouNom)
                        }
                        novaContrasenya?.let {
                            val novaContrasenyaEncriptada = encriptarDades(novaContrasenya, clauPublica)
                            usuari.put("contrasenya", novaContrasenyaEncriptada)
                        }
                        novaImatge?.let {
                            usuari.put("imatge", novaImatge.toString())
                        }

                        // Guarda la informacio actualizada a l'arxiu JSON
                        dataFile.writeText(json.toString())
                        return true
                    }
                }
            }
        }
        return false
    }

    fun actualitzarEntrenos(email: String, updatedList: List<List<String>>): Boolean {
        // Comprovem que el fitxer JSON existeix
        if (dataFile.exists() && dataFile.length() > 0) {
            val jsonText = dataFile.readText()
            val json = JSONObject(jsonText)

            // Comprovar que existeix la variable usuaris
            if (json.has("usuaris")) {
                val arrayUsuaris = json.getJSONArray("usuaris")
                // Recorrem tots els usuaris
                for (i in 0 until arrayUsuaris.length()) {
                    val usuari = arrayUsuaris.getJSONObject(i)
                    val emailEncriptat = usuari.getString("email")
                    val emailDesencriptat = desencriptarDades(emailEncriptat, clauPrivada)

                    // Si els emails coincideixen actualitzem els entrenos
                    if (emailDesencriptat == email) {
                        // Usuari trobat, actualitza la llista
                        usuari.put("entrenos", JSONArray(updatedList.map { JSONArray(it) }))
                        // Guarda la informacio actualizada a l'arxiu JSON
                        dataFile.writeText(json.toString())
                        return true
                    }
                }
            }
        }
        return false
    }

}

data class DadesUsuari(val nom: String, val email: String, val contrasenya: String, val imatge: Uri, val entrenos: List<List<String>>)
