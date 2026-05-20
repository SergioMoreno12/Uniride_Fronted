package com.example.uniride.model.dto

data class ViajeDTO(
    val origen: String,
    val destino: String,
    val fechaHora: String,
    val horaLlegada: String?,
    val costo: Double,
    val estado: String,
    val descripcionPunto: String?,
    val idVehiculo: Long,
    val idSede: Long
)