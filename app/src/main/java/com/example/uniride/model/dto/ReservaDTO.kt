package com.example.uniride.model.dto

data class ReservaDTO(
    val idUsuario: Long,
    val idViaje: Long,
    val fechaReserva: String,
    val confirmada: Boolean = false
)