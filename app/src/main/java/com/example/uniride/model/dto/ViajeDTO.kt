package com.example.uniride.model.dto

data class ViajeDTO(
    val origen: String,
    val destino: String,
    val fechaHora: String,
    val horaLlegada: String? = null,
    val costo: Double,
    val estado: String = "disponible",
    val descripcionPunto: String? = null,
    val tipoViaje: String = "ida",
    val idVehiculo: Long,
    val idSede: Long
)