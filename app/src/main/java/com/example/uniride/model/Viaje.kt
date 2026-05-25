package com.example.uniride.model

data class Viaje(
    val idViaje: Long = 0L,
    val origen: String,
    val destino: String,
    val fechaHora: String,
    val horaLlegada: String? = null,
    val costo: Double,
    val estado: String = "disponible",
    val descripcionPunto: String? = null,
    val tipoViaje: String? = "ida",
    val cuposDisponibles: Int? = null,   // ✅ NUEVO
    val vehiculo: Vehiculo? = null,
    val sede: Sede? = null
)