package com.aplication.aplicaciotfg

import android.net.Uri

object Usuari {
    lateinit var nom: String
    lateinit var email: String
    lateinit var contrasenya: String
    var imatge: Uri = Uri.EMPTY
    var entrenos: MutableList<List<String>> = mutableListOf()
    var carregaAgudaAhir = 0.0
    var carregaCronicaAhir = 0.0

    // Dades de quan s'enviara la notificacio en format 24 hores (per defecte a les 20:59)
    var hora = 20
    var minut = 59
    const val titol = "Introducir datos"
    const val contingut = "Recuerda introducir tus datos del entrenamiento!"

    fun logout() {
        nom = ""
        email = ""
        contrasenya = ""
        imatge = Uri.EMPTY
        entrenos = mutableListOf()
        carregaAgudaAhir = 0.0
        carregaCronicaAhir = 0.0
    }
}