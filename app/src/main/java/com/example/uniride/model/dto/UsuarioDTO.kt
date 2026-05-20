package com.example.uniride.model.dto

data class UsuarioDTO(
    val nombre: String,
    val correo: String,
    val contrasena: String,
    val telefono: String? = null,
    val rol: String = "pasajero"
)