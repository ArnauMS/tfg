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

        // Accedir al directori d'arxius privats de l'activitat
        val directori = File(context.filesDir, "claus")

        // Comprovem que el directori existeix
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
    private fun encriptarDades(dades: String, clauPublica: PublicKey): String {
        val cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding")
        cipher.init(Cipher.ENCRYPT_MODE, clauPublica)
        val dadesEncriptades = cipher.doFinal(dades.toByteArray())
        return Base64.getEncoder().encodeToString(dadesEncriptades)
    }

    // Funció per desxifrar dades amb clau privada
    private fun desencriptarDades(dades: String, clauPrivada: PrivateKey): String {
        val dadesEncriptades = Base64.getDecoder().decode(dades)
        val cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding")
        cipher.init(Cipher.DECRYPT_MODE, clauPrivada)
        val dadesDesencriptades = cipher.doFinal(dadesEncriptades)
        return String(dadesDesencriptades)
    }

    // Funció per desar un usuari al fitxer JSON
    fun registrarse(nom: String, email: String, contrasenya: String, imatge: Uri?) {
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

        if (imatge.toString() != "") {
            val imatgeEncriptada = encriptarDades(imatge.toString(), clauPublica)
            usuari.put("imatge", imatgeEncriptada)
        }

        arrayUsuaris.put(usuari)
        json.put("usuaris", arrayUsuaris)

        // Guardem l'usuari al fitxer JSON
        dataFile.writeText(json.toString())
    }

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
                        var imatge: Uri = Uri.EMPTY
                        if (usuari.has("imatge")) {
                            val imatgeEncriptada = usuari.getString("imatge")
                            imatge = desencriptarDades(imatgeEncriptada, clauPrivada).toUri()
                        }


                        // Si l'usuari te entrenos els carreguem, sino enviem una llista buida
                        val entrenos: MutableList<List<String>> = if (usuari.has("entrenos")) {
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
                        }.toMutableList()

                        val carregaAgudaAhir = if (usuari.has("carregaAgudaAhir")) {
                            usuari.getDouble("carregaAgudaAhir")
                        } else {
                            0.0
                        }
                        val carregaCronicaAhir = if (usuari.has("carregaCronicaAhir")) {
                            usuari.getDouble("carregaCronicaAhir")
                        } else {
                            0.0
                        }

                        val hora = if (usuari.has("hora")) {
                            usuari.getInt("hora")
                        } else {
                            20
                        }

                        val minut = if (usuari.has("minut")) {
                            usuari.getInt("minut")
                        } else {
                            59
                        }

                        return DadesUsuari(nom, emailDesencriptat, contrasenya, imatge, entrenos, carregaAgudaAhir, carregaCronicaAhir, hora, minut)
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
                        if (nouNom != "") {
                            usuari.put("nom", nouNom)
                        }
                        if (novaContrasenya != "") {
                            val novaContrasenyaEncriptada = novaContrasenya?.let { encriptarDades(it, clauPublica) }
                            usuari.put("contrasenya", novaContrasenyaEncriptada)
                        }
                        if (novaImatge.toString() != "") {
                            val novaImatgeEncriptada = novaImatge?.let { encriptarDades(it.toString(), clauPublica) }
                            usuari.put("imatge", novaImatgeEncriptada)
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

    fun actualitzarEntrenos(email: String, llistaActualitzada: MutableList<List<String>>): Boolean {
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
                        usuari.put("entrenos", JSONArray(llistaActualitzada.map { JSONArray(it) }))
                        // Guarda la informacio actualizada a l'arxiu JSON
                        dataFile.writeText(json.toString())
                        return true
                    }
                }
            }
        }
        return false
    }

    // Funcio per actualitzar el perfil de l'usuari
    fun emmagatzemarEwma(email: String, carregaAgudaAhir: Double, carregaCronicaAhir: Double): Boolean {
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
                        usuari.put("carregaAgudaAhir", carregaAgudaAhir)
                        usuari.put("carregaCronicaAhir", carregaCronicaAhir)

                        // Guarda la informacio actualizada a l'arxiu JSON
                        dataFile.writeText(json.toString())
                        return true
                    }
                }
            }
        }
        return false
    }

    fun emmagatzemarHora(email: String, hora: Int, minut: Int): Boolean {
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
                        usuari.put("hora", hora)
                        usuari.put("minut", minut)

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

data class DadesUsuari(val nom: String, val email: String, val contrasenya: String, val imatge: Uri, val entrenos: MutableList<List<String>>, val carregaAgudaAhir: Double, val carregaCronicaAhir: Double, val hora: Int, val minut: Int)
