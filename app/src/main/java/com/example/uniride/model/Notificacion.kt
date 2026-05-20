package com.example.uniride.model

data class Notificacion(
    val idNotificacion: Long = 0,
    val titulo: String = "",
    val mensaje: String = "",
    val destinatarios: String = "",
    val idUsuario: Long? = null,
    val idViaje: Long? = null,
    val leida: Boolean = false,
    val fechaEnvio: String = ""
)