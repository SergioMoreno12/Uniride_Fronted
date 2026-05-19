package com.example.uniride.model.dto

data class ViajeDTO(
    val origen: String,
    val destino: String,
    val fechaHora: String,
    val costo: Double,
    val estado: String,
    val idVehiculo: Long,
    val idSede: Long
)