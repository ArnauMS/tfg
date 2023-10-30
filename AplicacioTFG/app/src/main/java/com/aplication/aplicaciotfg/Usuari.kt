package com.aplication.aplicaciotfg

import android.net.Uri

object Usuari {
    lateinit var nom: String;
    lateinit var email: String;
    lateinit var contrasenya: String;
    var imatge: Uri? = null
    lateinit var entrenos: List<List<String>>;

    fun logout() {
        nom = "";
        email = "";
        contrasenya = "";
        imatge = null
        entrenos = emptyList();
    }
}