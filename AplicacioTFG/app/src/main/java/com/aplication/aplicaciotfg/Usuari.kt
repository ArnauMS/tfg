package com.aplication.aplicaciotfg

import android.net.Uri

object Usuari {
    lateinit var nom: String
    lateinit var email: String
    lateinit var contrasenya: String
    var imatge: Uri? = null
    lateinit var entrenos: List<List<String>>

    // Dades de quan s'enviara la notificacio en format 24 hores (per defecte a les 21:00)
    var hora = 21
    var minut = 0
    const val titol = "Introducir datos"
    const val contingut = "Recuerda introducir tus datos del entrenamiento!"

    fun logout() {
        nom = "";
        email = "";
        contrasenya = "";
        imatge = null
        entrenos = emptyList();
    }

    fun modificarAvis(novaHora: Int?, nouMinut: Int?) {
        if (novaHora != null) {
            hora = novaHora
        }

        if (nouMinut != null) {
            minut = nouMinut
        }
    }
}