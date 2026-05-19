package com.example.uniride.model

data class Reporte(
    val idReporte: Long = 0,
    val titulo: String = "",
    val descripcion: String = "",
    val estado: String = "pendiente",
    val fechaReporte: String = "",
    val usuario: Usuario? = null
)