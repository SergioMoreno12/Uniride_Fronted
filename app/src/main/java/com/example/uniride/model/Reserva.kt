package com.example.uniride.model

data class Reserva(
    val idReserva: Long = 0,
    val fechaReserva: String = "",
    val confirmada: Boolean = false,
    val calificada: Boolean = false,
    val usuario: Usuario? = null,
    val viaje: Viaje? = null
)