package com.example.uniride.model

data class Calificacion(
    val idCalificacion: Long = 0,
    val puntuacion: Int = 0,
    val comentario: String? = null,
    val fechaCalificacion: String = "",
    val conductor: Usuario? = null,
    val pasajero: Usuario? = null
)