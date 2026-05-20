package com.example.uniride.model

data class Usuario(
    val idUsuario: Long = 0,
    val nombre: String = "",
    val correo: String = "",
    val telefono: String? = null,
    val fechaRegistro: String = "",
    val rol: String = "pasajero",
    val activo: Boolean = true,
    val fotoPerfil: String? = null
)