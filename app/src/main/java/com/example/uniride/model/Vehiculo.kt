package com.example.uniride.model

data class Vehiculo(
    val idVehiculo: Long = 0,
    val placa: String = "",
    val marca: String = "",
    val modelo: String = "",
    val capacidad: Int = 0,
    val usuario: Usuario? = null
)