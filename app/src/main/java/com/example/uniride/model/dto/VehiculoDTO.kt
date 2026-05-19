package com.example.uniride.model.dto

data class VehiculoDTO(
    val placa: String,
    val marca: String,
    val modelo: String,
    val capacidad: Int,
    val idUsuario: Long
)