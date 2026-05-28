package com.example.uniride.model.dto

data class CalificacionRequest(
    val puntuacion: Int,
    val comentario: String,
    val idReserva: Long,
    val idConductor: Long,
    val idPasajero: Long
)