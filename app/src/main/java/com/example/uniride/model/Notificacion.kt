package com.example.uniride.model

data class Notificacion(
    val idNotificacion: Long = 0,
    val titulo: String = "",
    val mensaje: String = "",
    val destinatarios: String = "",
    val fechaEnvio: String = ""
)