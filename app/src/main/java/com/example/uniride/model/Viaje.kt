package com.example.uniride.model

data class Viaje(
    val idViaje: Long = 0,
    val origen: String = "",
    val destino: String = "",
    val fechaHora: String = "",
    val horaLlegada: String? = null,
    val costo: Double = 0.0,
    val estado: String = "disponible",
    val descripcionPunto: String? = null,
    val vehiculo: Vehiculo? = null,
    val sede: Sede? = null,
    val cuposDisponibles: Int? = null
)